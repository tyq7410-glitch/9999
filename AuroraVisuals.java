package dev.auroravisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class AuroraVisuals implements ClientModInitializer {
    private static KeyBinding menuKey;
    public static KeyBinding zoomKey, copyKey, panicKey, hudKey, fxKey;
    private static final ArrayDeque<Long> clicks = new ArrayDeque<>();
    private static boolean wasAttack = false;
    private static int hitTimer = 0;

    @Override
    public void onInitializeClient() {
        Config.load();
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> { Extras.restore(mc); Config.save(); });
        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("auroravisuals", "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.auroravisuals.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.auroravisuals.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, cat));

        copyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.auroravisuals.copy", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, cat));

        panicKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.auroravisuals.panic", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_END, cat));
        hudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.auroravisuals.hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, cat));
        fxKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.auroravisuals.fx", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, cat));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.wasPressed()) mc.setScreen(new MainMenuScreen(mc.currentScreen));
            boolean atk = mc.options.attackKey.isPressed();
            if (atk && !wasAttack) clicks.add(System.currentTimeMillis());
            wasAttack = atk;
            long now = System.currentTimeMillis();
            while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000) clicks.pollFirst();
            while (copyKey.wasPressed()) {
                if (mc.player != null) {
                    String xyz = String.format("%.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
                    mc.keyboard.setClipboard(xyz);
                    mc.player.sendMessage(Text.literal("Copied coords: " + xyz), true);
                }
            }
            if (hitTimer > 0) hitTimer--;
            while (panicKey.wasPressed()) {
                Perf.panic = !Perf.panic;
                if (mc.player != null)
                    mc.player.sendMessage(Text.literal("Aurora Visuals: " + (Perf.panic ? "hidden (press the key again to show)" : "visible")), true);
            }
            while (hudKey.wasPressed()) {
                Perf.hudHidden = !Perf.hudHidden;
                if (mc.player != null) mc.player.sendMessage(Text.literal("Aurora Visuals HUD: " + (Perf.hudHidden ? "hidden" : "visible")), true);
            }
            while (fxKey.wasPressed()) {
                Perf.fxOff = !Perf.fxOff;
                if (mc.player != null) mc.player.sendMessage(Text.literal("Aurora Visuals effects: " + (Perf.fxOff ? "off" : "on")), true);
            }
            Perf.update(mc);
            Safe.run("Extras.tick", () -> Extras.tick(mc));
            Safe.run("More.tick", () -> More.tick(mc));
            Safe.run("More2.tick", () -> More2.tick(mc));
            Safe.run("More3.tick", () -> More3.tick(mc));
        });

        AttackEntityCallback.EVENT.register((p, w, h, e, hr) -> {
            if (w.isClient()) { hitTimer = Config.I.hitTicks; Safe.run("onHit", () -> Extras.onHit(e)); }
            return ActionResult.PASS;
        });

        // button on title screen and pause menu
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof TitleScreen || screen instanceof GameMenuScreen)
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Aurora Visuals"),
                    b -> client.setScreen(new MainMenuScreen(screen))).dimensions(6, 6, 100, 20).build());
        });

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
            Identifier.of("auroravisuals", "hud"), (ctx, tick) -> renderHud(ctx));

        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, vanilla -> (ctx, tick) -> {
            if (Config.I.crosshair && !Perf.panic) Safe.run("crosshair", () -> renderCrosshair(ctx)); else vanilla.render(ctx, tick);
        });
    }

    // ---- helpers shared with Extras / editor
    public static void begin(DrawContext ctx, Config.Pos p, int sw, int sh) {
        Config c = Config.I;
        float sc = c.hudScale * (c.autoShrink ? Math.min(1f, sh / 240f) : 1f);
        float x = p.x * sw, y = p.y * sh;
        if (c.keepOnScreen) {
            x = Math.max(0f, Math.min(x, sw - Math.max(10f, p.w)));
            y = Math.max(0f, Math.min(y, sh - Math.max(10f, p.h)));
        }
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(x, y);
        ctx.getMatrices().scale(sc, sc);
    }
    public static void end(DrawContext ctx) { ctx.getMatrices().popMatrix(); }

    public static void panel(DrawContext ctx, int x, int y, int w, int h, int acc) {
        Config c = Config.I;
        ctx.fill(x, y, x + w, y + h, c.bg());
        if (c.accentBar) ctx.fill(x, y, x + 2, y + h, acc);
        if (c.hudBorder) {
            ctx.fill(x, y, x + w, y + 1, acc); ctx.fill(x, y + h - 1, x + w, y + h, acc);
            ctx.fill(x, y, x + 1, y + h, acc); ctx.fill(x + w - 1, y, x + w, y + h, acc);
        }
    }

    private static void renderHud(DrawContext ctx) {
        if (Perf.panic || Perf.hudHidden) return;
        if (Config.I.hideInChat && MinecraftClient.getInstance().currentScreen instanceof ChatScreen) return;
        Safe.run("hud", () -> renderHudInner(ctx));
    }

    private static void renderHudInner(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        More.renderBack(ctx, mc);
        More2.renderBack(ctx, mc);
        More3.renderBack(ctx, mc);
        Config c = Config.I;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int acc = c.accent();
        int pingVal = 0;
        List<String> lines = new ArrayList<>();
        if (c.watermark) lines.add("Aurora Visuals");
        if (c.fps) lines.add("FPS: " + mc.getCurrentFps());
        if (c.cps) lines.add("CPS: " + clicks.size());
        if (c.ping) {
            PlayerListEntry e = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            pingVal = e == null ? 0 : e.getLatency();
            lines.add("Ping: " + pingVal + " ms");
        }
        if (c.coords) lines.add(String.format("XYZ: %.0f %.0f %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        Extras.addInfoLines(lines, mc);
        if (Perf.isLimited()) lines.add("FX paused: low FPS");
        More.addLines(lines, mc);
        More2.addLines(lines, mc);
        More3.addLines(lines, mc);
        if (c.streamer) lines.removeIf(ln0 -> ln0.startsWith("XYZ:") || ln0.startsWith("IP:") || ln0.startsWith("Death:")
            || ln0.startsWith("Overworld:") || ln0.startsWith("Nether:") || ln0.startsWith("Chunk:")
            || ln0.startsWith("Biome:") || ln0.startsWith("Looking:") || ln0.startsWith("Below:"));

        if (!lines.isEmpty()) {
            int w = 0;
            for (String s : lines) w = Math.max(w, mc.textRenderer.getWidth(s));
            int lh = c.compact ? 9 : 11;
            int h = lines.size() * lh + 3;
            begin(ctx, c.pPanel, sw, sh);
            panel(ctx, 0, 0, w + 8, h, acc);
            for (int i = 0; i < lines.size(); i++) {
                String ln = lines.get(i);
                int lc = (i == 0 && c.watermark) ? acc : c.textCol();
                if (c.fpsColor && ln.startsWith("FPS:")) { int f = mc.getCurrentFps(); lc = f >= 120 ? Config.good() : f >= 60 ? 0xFFFFFF55 : Config.bad(); }
                if (c.pingColor && ln.startsWith("Ping:")) lc = pingVal <= 60 ? Config.good() : pingVal <= 150 ? 0xFFFFFF55 : Config.bad();
                ctx.drawText(mc.textRenderer, ln, 5, 3 + i * lh, lc, c.textShadow);
            }
            end(ctx);
            c.pPanel.w = (w + 8) * c.hudScale; c.pPanel.h = h * c.hudScale;
        }
        if (c.keystrokes) {
            begin(ctx, c.pKeys, sw, sh);
            key(ctx, mc, 20, 0, 18, "W", mc.options.forwardKey.isPressed(), acc);
            key(ctx, mc, 0, 20, 18, "A", mc.options.leftKey.isPressed(), acc);
            key(ctx, mc, 20, 20, 18, "S", mc.options.backKey.isPressed(), acc);
            key(ctx, mc, 40, 20, 18, "D", mc.options.rightKey.isPressed(), acc);
            key(ctx, mc, 0, 40, 58, "SPACE", mc.options.jumpKey.isPressed(), acc);
            if (c.keysMouse) {
                key(ctx, mc, 0, 60, 28, "LMB", mc.options.attackKey.isPressed(), acc);
                key(ctx, mc, 30, 60, 28, "RMB", mc.options.useKey.isPressed(), acc);
            }
            end(ctx);
            c.pKeys.w = 58 * c.hudScale; c.pKeys.h = (c.keysMouse ? 78 : 58) * c.hudScale;
        }
        Extras.renderSide(ctx, mc);
        More.render(ctx, mc);
        More2.render(ctx, mc);
        More3.render(ctx, mc);
        if (c.welcomeHint && System.currentTimeMillis() - Perf.joinAt < 10000) {
            String hint = "Aurora Visuals: Right Shift = menu | End = hide all";
            ctx.drawText(mc.textRenderer, hint, (sw - mc.textRenderer.getWidth(hint)) / 2, sh - 60, 0xFFFFFFFF, true);
        }
    }

    private static void key(DrawContext ctx, MinecraftClient mc, int x, int y, int w, String s, boolean down, int acc) {
        ctx.fill(x, y, x + w, y + 18, down ? (acc & 0x00FFFFFF) | 0xA0000000 : Config.I.bg());
        int tw = mc.textRenderer.getWidth(s);
        ctx.drawText(mc.textRenderer, s, x + (w - tw) / 2, y + 5, Config.I.textCol(), Config.I.textShadow);
    }

    private static void renderCrosshair(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || !mc.options.getPerspective().isFirstPerson()) return;
        Config c = Config.I;
        int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2;
        int base = c.chRainbow ? c.accent() : c.chCustom ? c.chRgb : 0xFFFFFF;
        if (c.chTargetColor && mc.targetedEntity != null) base = 0xFF4040;
        int col = (c.chAlpha << 24) | (base & 0xFFFFFF);
        int gap = c.chGap;
        if (c.chDynamic && mc.player != null && (mc.player.isSprinting() || !mc.player.isOnGround())) gap += 3;
        int t = c.chThick, s = c.chSize, o = t / 2;
        if (c.chCross) {
            int[][] arms = {
                {cx - gap - s, cy - o, cx - gap, cy - o + t}, {cx + gap, cy - o, cx + gap + s, cy - o + t},
                {cx - o, cy - gap - s, cx - o + t, cy - gap}, {cx - o, cy + gap, cx - o + t, cy + gap + s}};
            if (c.chOutline) for (int[] a : arms) ctx.fill(a[0] - 1, a[1] - 1, a[2] + 1, a[3] + 1, 0xFF000000);
            for (int[] a : arms) ctx.fill(a[0], a[1], a[2], a[3], col);
        }
        if (c.chCircle) {
            int rad = gap + s;
            for (int i = 0; i < 64; i++) {
                double a = Math.PI * 2 * i / 64;
                int px = cx + (int) Math.round(Math.cos(a) * rad), py = cy + (int) Math.round(Math.sin(a) * rad);
                ctx.fill(px, py, px + 1, py + 1, col);
            }
        }
        if (c.chDot) {
            if (c.chOutline) ctx.fill(cx - o - 1, cy - o - 1, cx - o + t + 1, cy - o + t + 1, 0xFF000000);
            ctx.fill(cx - o, cy - o, cx - o + t, cy - o + t, col);
        }
        if (c.hitmarker && hitTimer > 0) {
            int a = Math.max(40, 255 * hitTimer / Math.max(1, c.hitTicks));
            int hc = (a << 24) | 0xFFFFFF;
            for (int i = 0; i < c.hitSize; i++) {
                int d = gap + 2 + i;
                ctx.fill(cx - d, cy - d, cx - d + 1, cy - d + 1, hc);
                ctx.fill(cx + d, cy - d, cx + d + 1, cy - d + 1, hc);
                ctx.fill(cx - d, cy + d, cx - d + 1, cy + d + 1, hc);
                ctx.fill(cx + d, cy + d, cx + d + 1, cy + d + 1, hc);
            }
        }
    }
}
