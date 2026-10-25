package dev.auroravisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import java.awt.Color;
import java.util.List;
import java.util.Random;

/** 50 data-driven cosmetics, widgets and screen effects (see Feats). */
public class More {
    static final Random R = new Random();
    static float t = 0;
    private static float lastHp = -1, hpDelta = 0;
    static int ticks = 0;
    private static long hpTime = 0;
    private static double lx, lz;
    static double spd;
    private static final int[] fpsHist = new int[40];

    static boolean on(String k) { return Config.on(k); }
    static int rgb(String k, int d) { return Config.rgb(k, d) & 0xFFFFFF; }

    static void d(MinecraftClient mc, String k, int def, double x, double y, double z, float size) {
        mc.particleManager.addParticle(new DustParticleEffect(rgb(k, def), size), x, y, z, 0, 0, 0);
    }
    static void pt(MinecraftClient mc, ParticleEffect e, double x, double y, double z, double vx, double vy, double vz) {
        mc.particleManager.addParticle(e, x, y, z, vx, vy, vz);
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        var p = mc.player;
        Config c = Config.I;
        ticks++;
        t += 0.1f * Config.num("cosSpeed", 1f) * (Config.I.reducedMotion ? 0.4f : 1f);
        float sc = Config.num("cosScale", 1f);
        spd = Math.hypot(p.getX() - lx, p.getZ() - lz) * 20; lx = p.getX(); lz = p.getZ();
        float hp = p.getHealth();
        if (lastHp >= 0 && hp != lastHp) { hpDelta = hp - lastHp; hpTime = System.currentTimeMillis(); }
        lastHp = hp;
        if (ticks % 5 == 0) {
            System.arraycopy(fpsHist, 1, fpsHist, 0, fpsHist.length - 1);
            fpsHist[fpsHist.length - 1] = mc.getCurrentFps();
        }
        if (!Perf.ok() || (mc.options.getPerspective().isFirstPerson() && !c.cosFirst)) return;

        double x = p.getX(), y = p.getY(), z = p.getZ(), top = y + p.getHeight();
        double yaw = Math.toRadians(p.getBodyYaw());
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw), fx = -Math.sin(yaw), fz = Math.cos(yaw);

        if (on("orbitStars")) for (int k = 0; k < 3; k++) {
            double a = t * 2 + k * Math.PI * 2 / 3;
            d(mc, "orbitStars", 0xFFE066, x + Math.cos(a) * 0.7 * sc, top + 0.3 + Math.sin(t * 3 + k) * 0.1, z + Math.sin(a) * 0.7 * sc, 0.9f);
        }
        if (on("dna")) for (int i = 0; i < 14; i++) {
            double s = i / 14.0, a = s * Math.PI * 4 + t * 2;
            d(mc, "dna", 0x55FFAA, x + Math.cos(a) * 0.5 * sc, y + s * 2.0, z + Math.sin(a) * 0.5 * sc, 0.6f);
            d(mc, "dna", 0x55FFAA, x + Math.cos(a + Math.PI) * 0.5 * sc, y + s * 2.0, z + Math.sin(a + Math.PI) * 0.5 * sc, 0.6f);
        }
        if (on("pentagram")) {
            int[] ord = {0, 2, 4, 1, 3, 0};
            double r = 0.9 * sc, rot = t * 0.3;
            for (int e = 0; e < 5; e++) {
                double a1 = rot + ord[e] * Math.PI * 2 / 5, a2 = rot + ord[e + 1] * Math.PI * 2 / 5;
                for (int s = 0; s < 6; s++) {
                    double f = s / 6.0;
                    d(mc, "pentagram", 0xFF4D4D, x + (Math.cos(a1) * (1 - f) + Math.cos(a2) * f) * r, y + 0.03, z + (Math.sin(a1) * (1 - f) + Math.sin(a2) * f) * r, 0.6f);
                }
            }
        }
        if (on("groundRing")) for (int i = 0; i < 24; i++) {
            double a = Math.PI * 2 * i / 24 + t * 0.5;
            d(mc, "groundRing", 0x4DA6FF, x + Math.cos(a) * 1.0 * sc, y + 0.03, z + Math.sin(a) * 1.0 * sc, 0.6f);
        }
        if (on("hexagon")) {
            double r = 0.9 * sc, rot = -t * 0.4;
            for (int e = 0; e < 6; e++) {
                double a1 = rot + e * Math.PI / 3, a2 = rot + (e + 1) * Math.PI / 3;
                for (int s = 0; s < 4; s++) {
                    double f = s / 4.0;
                    d(mc, "hexagon", 0xC84DFF, x + (Math.cos(a1) * (1 - f) + Math.cos(a2) * f) * r, y + 0.03, z + (Math.sin(a1) * (1 - f) + Math.sin(a2) * f) * r, 0.6f);
                }
            }
        }
        if (on("rainbowArc")) for (int i = 0; i <= 16; i++) {
            double ang = Math.PI * i / 16, w = Math.cos(ang) * 0.8 * sc;
            int col = Color.HSBtoRGB(i / 16f, 0.8f, 1f) & 0xFFFFFF;
            pt(mc, new DustParticleEffect(col, 0.8f), x + rx * w, top + 0.35 + Math.sin(ang) * 0.6 * sc, z + rz * w, 0, 0, 0);
        }
        if (on("butterflies")) for (int k = 0; k < 4; k++) {
            double a = t * 1.3 + k * 1.7, r = (0.8 + 0.3 * Math.sin(t * 2 + k)) * sc, by = y + 1.2 + Math.sin(t * 3 + k * 2) * 0.5;
            double bx = x + Math.cos(a) * r, bz = z + Math.sin(a) * r, fl = Math.sin(t * 20 + k) * 0.06;
            d(mc, "butterflies", 0xFF88DD, bx + fl, by, bz, 0.6f);
            d(mc, "butterflies", 0xFF88DD, bx - fl, by, bz, 0.6f);
        }
        if (on("stormHalo") && R.nextInt(2) == 0) {
            double a = R.nextDouble() * Math.PI * 2;
            pt(mc, ParticleTypes.ELECTRIC_SPARK, x + Math.cos(a) * 0.5, top + 0.3, z + Math.sin(a) * 0.5, 0, 0, 0);
        }
        if (on("comet")) for (int j = 0; j < 5; j++) {
            double a = t * 3 - j * 0.15;
            d(mc, "comet", 0x66CCFF, x + Math.cos(a) * 0.9 * sc, y + 1 + Math.sin(a * 0.5) * 0.9, z + Math.sin(a) * 0.9 * sc, 1.1f - j * 0.18f);
        }
        if (on("heartRing") && ticks % 6 == 0) for (int k = 0; k < 6; k++) {
            double a = t + k * Math.PI / 3;
            pt(mc, ParticleTypes.HEART, x + Math.cos(a) * 0.8 * sc, y + 0.9, z + Math.sin(a) * 0.8 * sc, 0, 0.01, 0);
        }
        if (on("tornado")) for (int i = 0; i < 20; i++) {
            double s = i / 20.0, r = (0.2 + s * 0.9) * sc, a = s * Math.PI * 6 + t * 3;
            d(mc, "tornado", 0xCCCCCC, x + Math.cos(a) * r, y + s * 2.2, z + Math.sin(a) * r, 0.6f);
        }
        if (on("bubbles") && R.nextInt(2) == 0)
            pt(mc, ParticleTypes.BUBBLE_POP, x + (R.nextDouble() - 0.5) * 1.2, y + R.nextDouble() * 1.8, z + (R.nextDouble() - 0.5) * 1.2, 0, 0, 0);
        if (on("smoke") && Math.abs(p.getVelocity().x) + Math.abs(p.getVelocity().z) > 0.05)
            pt(mc, ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.1, z, 0, 0.02, 0);
        if (on("glow"))
            pt(mc, ParticleTypes.GLOW, x + (R.nextDouble() - 0.5) * 1.2, y + R.nextDouble() * 1.8, z + (R.nextDouble() - 0.5) * 1.2, 0, 0, 0);
        if (on("spores")) for (int k = 0; k < 2; k++)
            pt(mc, ParticleTypes.SPORE_BLOSSOM_AIR, x + (R.nextDouble() - 0.5) * 8, y + 3 + R.nextDouble() * 2, z + (R.nextDouble() - 0.5) * 8, 0, 0, 0);
        if (on("drips") && ticks % 3 == 0)
            pt(mc, ParticleTypes.DRIPPING_HONEY, x + (R.nextDouble() - 0.5) * 0.6, top + 0.1, z + (R.nextDouble() - 0.5) * 0.6, 0, 0, 0);
        if (on("critHalo") && ticks % 4 == 0) for (int k = 0; k < 8; k++) {
            double a = t + k * Math.PI / 4;
            pt(mc, ParticleTypes.CRIT, x + Math.cos(a) * 0.5 * sc, top + 0.3, z + Math.sin(a) * 0.5 * sc, 0, 0, 0);
        }
        if (on("galaxy")) for (int i = 0; i < 20; i++) {
            double rr = (0.2 + (i / 20.0) * 1.4) * sc, a = rr * 3 + (i % 3) * 2.09 + t;
            d(mc, "galaxy", 0x8A4DFF, x + Math.cos(a) * rr, y + 0.5, z + Math.sin(a) * rr, 0.5f);
        }
        if (on("sphere")) for (int i = 0; i < 24; i++) {
            double yy = 1 - 2 * i / 23.0, rad = Math.sqrt(Math.max(0, 1 - yy * yy)), th = i * 2.39996 + t, r = 1.1 * sc;
            d(mc, "sphere", 0x4DDBFF, x + Math.cos(th) * rad * r, y + 0.9 + yy * r, z + Math.sin(th) * rad * r, 0.5f);
        }
        if (on("feathers") && ticks % 3 == 0)
            pt(mc, ParticleTypes.ASH, x - fx * 0.4 + (R.nextDouble() - 0.5) * 0.6, y + 1.0 + R.nextDouble() * 0.6, z - fz * 0.4 + (R.nextDouble() - 0.5) * 0.6, 0, 0, 0);
        if (on("sparkler"))
            pt(mc, ParticleTypes.END_ROD, x + rx * 0.4 + fx * 0.2, y + 1.0, z + rz * 0.4 + fz * 0.2, (R.nextDouble() - 0.5) * 0.1, (R.nextDouble() - 0.5) * 0.1, (R.nextDouble() - 0.5) * 0.1);
        if (on("fountain")) for (int k = 0; k < 3; k++) {
            double ang = t * 2 + k * 2.09;
            for (int j = 0; j < 6; j++) {
                double s = j / 5.0, r = s * 0.9 * sc;
                d(mc, "fountain", 0xFFC83D, x + Math.cos(ang) * r, top + 0.5 + 4 * s * (1 - s) * 0.6, z + Math.sin(ang) * r, 0.5f);
            }
        }
        if (on("starRain"))
            pt(mc, ParticleTypes.END_ROD, x + (R.nextDouble() - 0.5) * 10, y + 4 + R.nextDouble() * 2, z + (R.nextDouble() - 0.5) * 10, 0, -0.15, 0);
        if (on("fireRing")) for (int i = 0; i < 6; i++) {
            double a = R.nextDouble() * Math.PI * 2;
            pt(mc, ParticleTypes.FLAME, x + Math.cos(a) * 0.8 * sc, y + 0.05, z + Math.sin(a) * 0.8 * sc, 0, 0.01, 0);
        }
        if (on("iceCrown")) for (int k = 0; k < 8; k++) {
            double a = t + k * Math.PI / 4;
            d(mc, "iceCrown", 0xA6D1FF, x + Math.cos(a) * 0.4 * sc, top + 0.2, z + Math.sin(a) * 0.4 * sc, 0.6f);
        }
        if (on("lavaDrips") && ticks % 3 == 0)
            pt(mc, ParticleTypes.DRIPPING_LAVA, x + (R.nextDouble() - 0.5) * 0.6, top + 0.1, z + (R.nextDouble() - 0.5) * 0.6, 0, 0, 0);
        if (on("portalSwirl")) for (int k = 0; k < 2; k++)
            pt(mc, ParticleTypes.PORTAL, x + (R.nextDouble() - 0.5) * 1.2, y + 0.5 + R.nextDouble(), z + (R.nextDouble() - 0.5) * 1.2, (R.nextDouble() - 0.5) * 0.5, (R.nextDouble() - 0.5) * 0.5, (R.nextDouble() - 0.5) * 0.5);
        if (on("mist") && ticks % 2 == 0) {
            double a = R.nextDouble() * Math.PI * 2;
            pt(mc, ParticleTypes.CLOUD, x + Math.cos(a) * sc, y + 0.1, z + Math.sin(a) * sc, 0, 0.002, 0);
        }
    }

    public static void addLines(List<String> lines, MinecraftClient mc) {
        var p = mc.player;
        if (on("invSlots")) {
            int u = 0;
            for (int i = 0; i < 36; i++) if (!p.getInventory().getStack(i).isEmpty()) u++;
            lines.add("Inv: " + u + "/36");
        }
        if (on("dayPhase")) {
            long tt = mc.world.getTimeOfDay() % 24000;
            lines.add("Phase: " + (tt < 12000 ? "Day" : tt < 13000 ? "Dusk" : tt < 23000 ? "Night" : "Dawn"));
        }
        if (on("moonPhase")) lines.add("Moon: " + ((mc.world.getTimeOfDay() / 24000L) % 8 + 1) + "/8");
        if (on("lookBlock") && mc.crosshairTarget instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK)
            lines.add("Looking: " + mc.world.getBlockState(b.getBlockPos()).getBlock().getName().getString());
        if (on("weather")) lines.add("Weather: " + (mc.world.isThundering() ? "Storm" : mc.world.isRaining() ? "Rain" : "Clear"));
        if (on("chunk")) lines.add("Chunk: " + (((int) Math.floor(p.getX())) >> 4) + " " + (((int) Math.floor(p.getZ())) >> 4));
    }

    static void bar(DrawContext ctx, int x, int y, int w, int h, float f, int col) {
        f = Math.max(0f, Math.min(1f, f));
        ctx.fill(x, y, x + w, y + h, 0x80000000);
        ctx.fill(x, y, x + (int) (w * f), y + h, col);
    }

    /** Full-screen layers: drawn BEFORE the HUD so they never darken the panels. */
    public static void renderBack(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.options.hudHidden) return;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        long now = System.currentTimeMillis();
        if (on("vignette")) {
            int a0 = (int) (200 * Config.num("vigAlpha", 0.5f)), col = rgb("vignette", 0x000000);
            for (int i = 0; i < 12; i++) {
                int cc = ((a0 * (12 - i) / 12) << 24) | col, o = i * 6;
                ctx.fill(0, o, sw, o + 6, cc); ctx.fill(0, sh - o - 6, sw, sh - o, cc);
                ctx.fill(0, o + 6, o + 6, sh - o - 6, cc); ctx.fill(sw - o - 6, o + 6, sw - o, sh - o - 6, cc);
            }
        }
        if (on("tint")) ctx.fill(0, 0, sw, sh, (((int) Config.num("tintA", 40f)) << 24) | rgb("tint", 0x3355FF));
        if (on("letterbox")) {
            int hb = (int) Config.num("letterH", 40f), col = 0xFF000000 | rgb("letterbox", 0x000000);
            ctx.fill(0, 0, sw, hb, col); ctx.fill(0, sh - hb, sw, sh, col);
        }
        if (on("corners")) {
            int col = 0xFF000000 | rgb("corners", 0xFFFFFF), L = 20, T = 2;
            ctx.fill(4, 4, 4 + L, 4 + T, col); ctx.fill(4, 4, 4 + T, 4 + L, col);
            ctx.fill(sw - 4 - L, 4, sw - 4, 4 + T, col); ctx.fill(sw - 4 - T, 4, sw - 4, 4 + L, col);
            ctx.fill(4, sh - 4 - T, 4 + L, sh - 4, col); ctx.fill(4, sh - 4 - L, 4 + T, sh - 4, col);
            ctx.fill(sw - 4 - L, sh - 4 - T, sw - 4, sh - 4, col); ctx.fill(sw - 4 - T, sh - 4 - L, sw - 4, sh - 4, col);
        }
        if (on("pulseFrame")) {
            int a = Config.I.calm ? 80 : (int) (60 + 60 * Math.sin(now / 300.0)), col = (a << 24) | rgb("pulseFrame", 0xFF4DDB), th = 4;
            ctx.fill(0, 0, sw, th, col); ctx.fill(0, sh - th, sw, sh, col);
            ctx.fill(0, th, th, sh - th, col); ctx.fill(sw - th, th, sw, sh - th, col);
        }

    }

    public static void render(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.options.hudHidden) return;
        var p = mc.player;
        Config c = Config.I;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight(), cx = sw / 2, cy = sh / 2, acc = c.accent();
        long now = System.currentTimeMillis();

        if (on("hpBar")) bar(ctx, cx - 20, cy + 34, 40, 3, p.getHealth() / p.getMaxHealth(), 0xFF000000 | rgb("hpBar", 0xFF4D4D));
        if (on("armorBar")) bar(ctx, cx - 20, cy + 38, 40, 3, p.getArmor() / 20f, 0xFF000000 | rgb("armorBar", 0x8899AA));
        if (on("foodBar")) bar(ctx, cx - 20, cy + 42, 40, 3, p.getHungerManager().getFoodLevel() / 20f, 0xFF000000 | rgb("foodBar", 0xFFAA33));
        if (on("speedBar")) bar(ctx, cx - 20, cy + 48, 40, 3, (float) (spd / 12.0), 0xFF000000 | rgb("speedBar", 0x4DDBFF));
        if (on("comboBar")) bar(ctx, cx - 20, cy + 54, 40, 3, Extras.comboLeft(), 0xFF000000 | rgb("comboBar", 0xFFC83D));
        if (on("cdRing")) {
            float cd = p.getAttackCooldownProgress(0f);
            if (cd < 1f) {
                int dots = 32, cnt = (int) (dots * cd), col = 0xFF000000 | rgb("cdRing", 0x55FF55);
                for (int i = 0; i < dots; i++) {
                    double a = -Math.PI / 2 + Math.PI * 2 * i / dots;
                    int px = cx + (int) Math.round(Math.cos(a) * 14), py = cy + (int) Math.round(Math.sin(a) * 14);
                    ctx.fill(px, py, px + 2, py + 2, i < cnt ? col : 0x55FFFFFF);
                }
            }
        }
        long age = now - hpTime;
        if (age < 1500) {
            int rise = (int) (age / 60);
            if (hpDelta < 0 && on("dmgTaken"))
                ctx.drawText(mc.textRenderer, String.format("%.1f", hpDelta), cx + 14, cy - 6 - rise, 0xFFFF5555, true);
            if (hpDelta > 0 && on("healPop"))
                ctx.drawText(mc.textRenderer, String.format("+%.1f", hpDelta), cx + 14, cy - 6 - rise, 0xFF55FF55, true);
        }
        if (on("sprintDot") && p.isSprinting())
            ctx.fill(cx + 10, cy - 14, cx + 13, cy - 11, 0xFF000000 | rgb("sprintDot", 0x55FF55));
        if (on("sneakText") && p.isSneaking())
            ctx.drawText(mc.textRenderer, "SNEAK", cx + 8, cy - 26, 0xFFFFFFFF, true);
        if (on("fpsGraph")) {
            AuroraVisuals.begin(ctx, c.pGraph, sw, sh);
            AuroraVisuals.panel(ctx, 0, 0, 84, 30, acc);
            int col = 0xFF000000 | rgb("fpsGraph", 0x55FF55);
            for (int i = 0; i < fpsHist.length; i++) {
                int hh = Math.min(26, fpsHist[i] * 26 / 240);
                ctx.fill(2 + i * 2, 28 - hh, 4 + i * 2, 28, col);
            }
            AuroraVisuals.end(ctx);
            c.pGraph.w = 84 * c.hudScale; c.pGraph.h = 30 * c.hudScale;
        }
    }
}
