package dev.auroravisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 10 PvP and 10 PvE information/visual features. Nothing here reveals hidden entities or automates actions. */
public class More3 {
    private static final Map<String, Integer> DEF = new HashMap<>();
    static {
        for (Feats.F[] arr : new Feats.F[][]{Feats.PVP, Feats.PVE}) for (Feats.F f : arr) DEF.put(f.key(), f.def());
    }
    private static boolean on(String k) { return Config.on(k); }
    private static int rgb(String k) { return More.rgb(k, DEF.getOrDefault(k, 0xFFFFFF)); }

    private static int lastKills = -1;
    private static long flashAt = 0, atkAt = 0;
    private static boolean deadFlag = false;
    private static double[] deathPos = null;
    private static double fallPeak = Double.NaN;
    private static String atkName = null;
    private static float lastHp = -1;

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        var p = mc.player;
        long now = System.currentTimeMillis();
        int k = Extras.killsTotal();
        if (lastKills >= 0 && k > lastKills) flashAt = now;
        lastKills = k;
        if (p.getHealth() <= 0 && !deadFlag) { deathPos = new double[]{p.getX(), p.getY(), p.getZ()}; deadFlag = true; }
        else if (p.getHealth() > 0) deadFlag = false;
        double y = p.getY();
        if (Double.isNaN(fallPeak)) fallPeak = y;
        if (p.isOnGround() || p.isTouchingWater() || p.isClimbing()) fallPeak = y;
        else fallPeak = Math.max(fallPeak, y);
        float hp = p.getHealth();
        if (lastHp >= 0 && hp < lastHp) {
            LivingEntity a = p.getAttacker();
            atkName = a == null ? "?" : a.getName().getString();
            atkAt = now;
        }
        lastHp = hp;
    }

    public static void addLines(List<String> lines, MinecraftClient mc) {
        var p = mc.player;
        if (on("blockBelow"))
            lines.add("Below: " + mc.world.getBlockState(p.getBlockPos().down()).getBlock().getName().getString());
        if (on("saturation")) lines.add(String.format("Saturation: %.1f", p.getHungerManager().getSaturationLevel()));
        if (on("dayTimer")) {
            long tt = mc.world.getTimeOfDay() % 24000;
            boolean toNight = tt < 13000;
            long sec = (toNight ? 13000 - tt : 24000 - tt) / 20;
            lines.add(String.format("%s: %d:%02d", toNight ? "Night in" : "Day in", sec / 60, sec % 60));
        }
        if (on("stackTotal")) {
            ItemStack h = p.getMainHandStack();
            if (!h.isEmpty()) lines.add("Total: " + p.getInventory().count(h.getItem()) + "x " + h.getName().getString());
        }
        if (on("oreHint")) {
            double y = p.getY();
            lines.add("Ore: " + (y <= -50 ? "diamonds best" : y <= 16 ? "diamonds/redstone" : y <= 64 ? "coal/iron/copper" : "surface"));
        }
        if (on("deathPoint") && deathPos != null) {
            double dx = deathPos[0] - p.getX(), dy = deathPos[1] - p.getY(), dz = deathPos[2] - p.getZ();
            lines.add(String.format("Death: %.0f %.0f %.0f (%.0f m)", deathPos[0], deathPos[1], deathPos[2], Math.sqrt(dx * dx + dy * dy + dz * dz)));
        }
        if (on("lastAttacker") && atkName != null && System.currentTimeMillis() - atkAt < 6000)
            lines.add("Hit by: " + atkName);
    }

    /** Full-screen flash: drawn before the HUD. */
    public static void renderBack(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.options.hudHidden) return;
        long age = System.currentTimeMillis() - flashAt;
        if (on("killFlash") && !Config.I.calm && flashAt > 0 && age < 350)
            ctx.fill(0, 0, ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight(), ((int) (110 * (1 - age / 350.0)) << 24) | rgb("killFlash"));
    }

    public static void render(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        var p = mc.player;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight(), cx = sw / 2, cy = sh / 2;
        long now = System.currentTimeMillis();

        if (on("critReady") && !p.isOnGround() && p.getVelocity().y < 0 && !p.isClimbing() && !p.isTouchingWater()
                && !p.isSprinting() && !p.hasVehicle() && p.getAttackCooldownProgress(0.5f) > 0.9f)
            ctx.drawText(mc.textRenderer, "CRIT", cx - 10, cy - 36, 0xFF000000 | rgb("critReady"), true);
        if (on("blockState") && p.isBlocking())
            ctx.drawText(mc.textRenderer, "BLOCK", cx - 14, cy - 46, 0xFF55AAFF, true);
        if (on("dmgDealt") && Extras.lastDamageMs() > 0) {
            long age = now - Extras.lastDamageMs();
            if (age < 1500)
                ctx.drawText(mc.textRenderer, String.format("+%.1f", Extras.lastDamage()), cx + 14, cy - 20 - (int) (age / 60), 0xFF000000 | rgb("dmgDealt"), true);
        }
        if (on("distBar") && mc.targetedEntity != null)
            More.bar(ctx, cx - 20, cy + 66, 40, 3, 1f - p.distanceTo(mc.targetedEntity) / 6f, 0xFF000000 | rgb("distBar"));
        ItemStack held = p.getMainHandStack();
        boolean tool = !held.isEmpty() && held.isDamageable();
        if (on("toolBar") && tool)
            More.bar(ctx, cx - 20, cy + 70, 40, 3, (held.getMaxDamage() - held.getDamage()) / (float) held.getMaxDamage(), 0xFF000000 | rgb("toolBar"));
        if (on("mountBar") && p.getVehicle() instanceof LivingEntity mount)
            More.bar(ctx, cx - 20, cy + 74, 40, 3, mount.getHealth() / mount.getMaxHealth(), 0xFF000000 | rgb("mountBar"));
        if (on("fallDmg") && !p.isOnGround()) {
            double fall = fallPeak - p.getY();
            if (fall > 3) ctx.drawText(mc.textRenderer, "Fall ~" + (int) Math.floor(fall - 3) + " HP", cx - 24, cy - 56, 0xFFFFAA33, true);
        }

        // blinking alerts at the top center
        List<String> al = new ArrayList<>();
        List<Integer> alc = new ArrayList<>();
        if (on("armorAlert")) {
            float pct = Config.num("armorPct", 15f) / 100f;
            for (EquipmentSlot sl : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack a = p.getEquippedStack(sl);
                if (!a.isEmpty() && a.isDamageable() && (a.getMaxDamage() - a.getDamage()) < a.getMaxDamage() * pct) {
                    al.add("ARMOR LOW"); alc.add(Config.bad()); break;
                }
            }
        }
        if (on("totemAlert") && p.getInventory().count(Items.TOTEM_OF_UNDYING) == 0) { al.add("NO TOTEM"); alc.add(0xFFFFAA33); }
        if (on("toolWarn") && tool && (held.getMaxDamage() - held.getDamage()) < held.getMaxDamage() * Config.num("toolPct", 10f) / 100f) {
            al.add("TOOL LOW"); alc.add(0xFFFF8855);
        }
        if (on("effectWarn"))
            for (StatusEffectInstance e : p.getStatusEffects())
                if (!e.isInfinite() && e.getDuration() <= 200) {
                    al.add(e.getEffectType().value().getName().getString() + " " + (e.getDuration() / 20) + "s"); alc.add(0xFFFFFF55);
                }
        if (on("pingWarn")) {
            PlayerListEntry pe = mc.getNetworkHandler() == null ? null : mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
            int ping = pe == null ? 0 : pe.getLatency();
            if (ping > Config.num("pingMs", 200f)) { al.add("HIGH PING " + ping + " ms"); alc.add(Config.bad()); }
        }
        if (!al.isEmpty() && (Config.I.calm || (now / 400) % 2 == 0)) {
            int ay = 24;
            for (int i = 0; i < al.size(); i++) {
                ctx.drawText(mc.textRenderer, al.get(i), (sw - mc.textRenderer.getWidth(al.get(i))) / 2, ay, alc.get(i), true);
                ay += 11;
            }
        }
    }
}
