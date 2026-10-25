package dev.auroravisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Second batch of 50 data-driven features (see Feats.COS2 / HUD2 / SCR2). */
public class More2 {
    private static final Random R = More.R;
    private static final Map<String, Integer> DEF = new HashMap<>();
    private static final double[][] FLOWERS = new double[12][2];
    static {
        for (Feats.F[] arr : new Feats.F[][]{Feats.COS2, Feats.HUD2, Feats.SCR2}) for (Feats.F f : arr) DEF.put(f.key(), f.def());
        Random fr = new Random(42);
        for (int i = 0; i < 12; i++) { FLOWERS[i][0] = fr.nextDouble() * Math.PI * 2; FLOWERS[i][1] = 0.4 + fr.nextDouble() * 1.1; }
    }

    private static final ArrayDeque<double[]> hist = new ArrayDeque<>();
    private static final ArrayDeque<Long> swings = new ArrayDeque<>();
    private static boolean wasAtk = false, dead = false, firstTick = true;
    private static int totalSwings = 0, deaths = 0, maxCps = 0;
    private static long firstSwing = 0, lastSwing = 0;
    private static double dist = 0, fallPeak = Double.NaN, lastFall = 0, lx, lz;

    private static boolean on(String k) { return Config.on(k); }
    private static int rgb(String k) { return More.rgb(k, DEF.getOrDefault(k, 0xFFFFFF)); }
    private static void d(MinecraftClient mc, String k, double x, double y, double z, float size) {
        More.d(mc, k, DEF.getOrDefault(k, 0xFFFFFF), x, y, z, size);
    }
    private static void pt(MinecraftClient mc, ParticleEffect e, double x, double y, double z, double vx, double vy, double vz) {
        More.pt(mc, e, x, y, z, vx, vy, vz);
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        var p = mc.player;
        Config c = Config.I;
        long now = System.currentTimeMillis();
        double x = p.getX(), y = p.getY(), z = p.getZ();

        // ---- statistics (always tracked)
        boolean atk = mc.options.attackKey.isPressed();
        if (atk && !wasAtk) { swings.add(now); totalSwings++; if (firstSwing == 0) firstSwing = now; lastSwing = now; }
        wasAtk = atk;
        while (!swings.isEmpty() && now - swings.peekFirst() > 1000) swings.pollFirst();
        maxCps = Math.max(maxCps, swings.size());
        if (firstTick) { lx = x; lz = z; firstTick = false; }
        double dd = Math.hypot(x - lx, z - lz);
        if (dd < 10) dist += dd;
        lx = x; lz = z;
        if (p.getHealth() <= 0 && !dead) { deaths++; dead = true; } else if (p.getHealth() > 0) dead = false;
        if (Double.isNaN(fallPeak)) fallPeak = y;
        if (p.isOnGround()) { if (fallPeak - y > 0.5) lastFall = fallPeak - y; fallPeak = y; }
        else fallPeak = Math.max(fallPeak, y);
        hist.addFirst(new double[]{x, y, z});
        while (hist.size() > 30) hist.pollLast();

        if (!Perf.ok() || (mc.options.getPerspective().isFirstPerson() && !c.cosFirst)) return;

        // ---- cosmetics
        float t = More.t;
        int ticks = More.ticks;
        double sc = Config.num("cosScale", 1f);
        double top = y + p.getHeight();
        double yaw = Math.toRadians(p.getBodyYaw());
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw), fx = -Math.sin(yaw), fz = Math.cos(yaw);

        if (on("dblRing")) for (int i = 0; i < 20; i++) {
            double a = t * 1.5 + Math.PI * 2 * i / 20;
            d(mc, "dblRing", x + Math.cos(a) * 0.8 * sc, y + 0.1, z + Math.sin(a) * 0.8 * sc, 0.6f);
            d(mc, "dblRing", x + Math.cos(-a) * 0.8 * sc, top + 0.1, z + Math.sin(-a) * 0.8 * sc, 0.6f);
        }
        if (on("spiralUp")) {
            double s0 = (t * 0.5) % 1.0;
            for (int j = 0; j < 8; j++) {
                double s2 = ((s0 - j * 0.04) % 1.0 + 1.0) % 1.0, a = s2 * Math.PI * 6 + t;
                d(mc, "spiralUp", x + Math.cos(a) * 0.7 * sc, y + s2 * 2.2, z + Math.sin(a) * 0.7 * sc, (float) (1.0 - j * 0.1));
            }
        }
        if (on("hexStar")) for (int tri = 0; tri < 2; tri++) {
            double b = (tri == 0 ? Math.PI / 2 : -Math.PI / 2) + t * 0.5;
            for (int e = 0; e < 3; e++) {
                double a1 = b + e * Math.PI * 2 / 3, a2 = b + (e + 1) * Math.PI * 2 / 3;
                for (int s = 0; s < 5; s++) {
                    double f = s / 5.0;
                    d(mc, "hexStar", x + (Math.cos(a1) * (1 - f) + Math.cos(a2) * f) * 0.6 * sc, top + 0.4,
                        z + (Math.sin(a1) * (1 - f) + Math.sin(a2) * f) * 0.6 * sc, 0.5f);
                }
            }
        }
        if (on("crescent")) for (int i = 0; i <= 12; i++) {
            double ang = -2.2 + 4.4 * i / 12.0, h1 = Math.cos(ang) * 0.35 * sc, v1 = Math.sin(ang) * 0.35 * sc;
            d(mc, "crescent", x + rx * 0.9 + fx * h1, top + 0.1 + v1, z + rz * 0.9 + fz * h1, 0.6f);
        }
        if (on("sunRays")) {
            for (int k = 0; k < 4; k++)
                d(mc, "sunRays", x + (R.nextDouble() - 0.5) * 0.1, top + 0.6 + (R.nextDouble() - 0.5) * 0.1, z + (R.nextDouble() - 0.5) * 0.1, 1.0f);
            for (int k = 0; k < 8; k++) {
                double a = t + k * Math.PI / 4;
                for (int j = 1; j <= 3; j++) {
                    double r = (0.12 + 0.12 * j) * sc;
                    d(mc, "sunRays", x + Math.cos(a) * r, top + 0.6, z + Math.sin(a) * r, 0.5f);
                }
            }
        }
        if (on("rose")) for (int i = 0; i < 40; i++) {
            double th = Math.PI * 2 * i / 40 + t * 0.2, r = Math.cos(3 * th) * 0.9 * sc;
            d(mc, "rose", x + Math.cos(th) * r, y + 0.04, z + Math.sin(th) * r, 0.6f);
        }
        if (on("infinity")) {
            double phi = t * 0.3;
            for (int i = 0; i < 30; i++) {
                double u = Math.PI * 2 * i / 30 + t, den = 1 + Math.sin(u) * Math.sin(u);
                double px = Math.cos(u) / den * sc, pz = Math.sin(u) * Math.cos(u) / den * sc;
                d(mc, "infinity", x + px * Math.cos(phi) - pz * Math.sin(phi), y + 1.0, z + px * Math.sin(phi) + pz * Math.cos(phi), 0.6f);
            }
        }
        if (on("heartShape")) for (int i = 0; i < 28; i++) {
            double u = Math.PI * 2 * i / 28;
            double hx = 16 * Math.pow(Math.sin(u), 3) * 0.025 * sc;
            double hy = (13 * Math.cos(u) - 5 * Math.cos(2 * u) - 2 * Math.cos(3 * u) - Math.cos(4 * u)) * 0.025 * sc;
            d(mc, "heartShape", x + rx * hx, top + 0.8 + hy, z + rz * hx, 0.6f);
        }
        if (on("atom")) for (int k = 0; k < 3; k++) {
            double tilt = k * Math.PI / 3, phi = t * 0.2;
            for (int j = 0; j < 10; j++) {
                double a = Math.PI * 2 * j / 10 + t * (1 + k * 0.3);
                double ox = Math.cos(a) * 0.8 * sc, oy = Math.sin(a) * 0.8 * sc;
                double yy = oy * Math.cos(tilt), zz = oy * Math.sin(tilt);
                d(mc, "atom", x + ox * Math.cos(phi) - zz * Math.sin(phi), y + 1.0 + yy, z + ox * Math.sin(phi) + zz * Math.cos(phi), 0.5f);
            }
        }
        if (on("waveRing")) for (int i = 0; i < 32; i++) {
            double a = Math.PI * 2 * i / 32 + t;
            d(mc, "waveRing", x + Math.cos(a) * sc, y + 0.1 + 0.12 * Math.sin(a * 5 + t * 4), z + Math.sin(a) * sc, 0.6f);
        }
        if (on("bolt") && ticks % 4 == 0) for (int j = 0; j <= 8; j++)
            d(mc, "bolt", x + (R.nextDouble() - 0.5) * 0.3, top + 2.2 - j * 0.28, z + (R.nextDouble() - 0.5) * 0.3, 0.8f);
        if (on("dome")) for (int i = 0; i < 24; i++) {
            double yy = i / 23.0, rad = Math.sqrt(Math.max(0, 1 - yy * yy)), th = i * 2.39996 + t * 0.5;
            d(mc, "dome", x + Math.cos(th) * rad * 0.9 * sc, top - 0.1 + yy * 0.9 * sc, z + Math.sin(th) * rad * 0.9 * sc, 0.5f);
        }
        if (on("ribbon")) {
            int idx = 0;
            for (double[] h : hist) {
                if (idx % 2 == 0 && idx > 3)
                    d(mc, "ribbon", h[0], h[1] + 1.0 + Math.sin(idx * 0.4 + t * 3) * 0.1, h[2], (float) Math.max(0.3, 0.9 - idx * 0.02));
                idx++;
            }
        }
        if (on("planets")) {
            double[] rad = {1.0, 1.3, 1.6}, sp2 = {1.2, 0.8, 0.5};
            for (int k = 0; k < 3; k++) {
                double a = t * sp2[k] + k * 2.1;
                double px = x + Math.cos(a) * rad[k] * sc, py = y + 1.0 + Math.sin(a * 0.7) * 0.3, pz = z + Math.sin(a) * rad[k] * sc;
                for (int q = 0; q < 3; q++)
                    d(mc, "planets", px + (R.nextDouble() - 0.5) * 0.08 * (k + 1), py + (R.nextDouble() - 0.5) * 0.08 * (k + 1),
                        pz + (R.nextDouble() - 0.5) * 0.08 * (k + 1), 1.0f - k * 0.15f);
            }
        }
        if (on("pulseWave")) {
            double ph = (t * 0.6) % 1.0, r = ph * 2.5 * sc;
            for (int i = 0; i < 28; i++) {
                double a = Math.PI * 2 * i / 28;
                d(mc, "pulseWave", x + Math.cos(a) * r, y + 0.04, z + Math.sin(a) * r, (float) (0.9 - ph * 0.6));
            }
        }
        if (on("scanRing")) {
            double yy = y + (Math.sin(t) * 0.5 + 0.5) * 2.0;
            for (int i = 0; i < 16; i++) {
                double a = Math.PI * 2 * i / 16;
                d(mc, "scanRing", x + Math.cos(a) * 0.6 * sc, yy, z + Math.sin(a) * 0.6 * sc, 0.5f);
            }
        }
        if (on("matrix")) for (int k = 0; k < 4; k++)
            d(mc, "matrix", x + (R.nextDouble() - 0.5) * 6, y + R.nextDouble() * 3, z + (R.nextDouble() - 0.5) * 6, 0.6f);
        if (on("wisps")) for (int k = 0; k < 3; k++) {
            double wx = x + Math.sin(t * 0.7 + k * 2.1) * 1.2, wy = y + 1 + Math.sin(t * 1.1 + k) * 0.6, wz = z + Math.cos(t * 0.9 + k * 1.7) * 1.2;
            d(mc, "wisps", wx, wy, wz, 1.0f);
            d(mc, "wisps", wx + 0.04, wy + 0.04, wz, 0.6f);
        }
        if (on("flowers")) for (int i = 0; i < 12; i++) {
            double a = FLOWERS[i][0], r = FLOWERS[i][1] * sc;
            d(mc, "flowers", x + Math.cos(a) * r, y + 0.05, z + Math.sin(a) * r, 0.7f);
        }
        if (on("cape")) for (int j = 0; j < 3; j++) for (int i = -2; i <= 2; i++) {
            double yy = top - 0.4 - j * 0.35, lat = i * 0.14;
            double sway = Math.sin(t * 2 + j * 0.8 + i * 0.3) * 0.05 * (j + 1), back = -0.25 - j * 0.06 - sway;
            d(mc, "cape", x + fx * back + rx * lat, yy, z + fz * back + rz * lat, 0.7f);
        }
        if (on("smokeRing") && ticks % 4 == 0) for (int k = 0; k < 6; k++) {
            double a = t + k * Math.PI / 3;
            pt(mc, ParticleTypes.CLOUD, x + Math.cos(a) * 0.4, top + 0.4, z + Math.sin(a) * 0.4, 0, 0, 0);
        }
        if (on("soulRing")) for (int k = 0; k < 6; k++) {
            double a = R.nextDouble() * Math.PI * 2;
            pt(mc, ParticleTypes.SOUL_FIRE_FLAME, x + Math.cos(a) * 0.8 * sc, y + 0.05, z + Math.sin(a) * 0.8 * sc, 0, 0.01, 0);
        }
        if (on("totemRing") && ticks % 6 == 0) for (int k = 0; k < 8; k++) {
            double a = t + k * Math.PI / 4;
            pt(mc, ParticleTypes.TOTEM_OF_UNDYING, x + Math.cos(a) * sc, y + 0.1, z + Math.sin(a) * sc, 0, 0.02, 0);
        }
        if (on("waterDrips") && ticks % 3 == 0)
            pt(mc, ParticleTypes.DRIPPING_WATER, x + (R.nextDouble() - 0.5) * 0.6, top + 0.1, z + (R.nextDouble() - 0.5) * 0.6, 0, 0, 0);
        if (on("noteRing") && ticks % 8 == 0) for (int k = 0; k < 4; k++) {
            double a = t + k * Math.PI / 2;
            pt(mc, ParticleTypes.NOTE, x + Math.cos(a) * 0.5, top + 0.5, z + Math.sin(a) * 0.5, R.nextDouble(), 0, 0);
        }
        if (on("embers")) for (int k = 0; k < 2; k++)
            pt(mc, ParticleTypes.SMALL_FLAME, x + (R.nextDouble() - 0.5) * 1.2, y + R.nextDouble() * 1.5, z + (R.nextDouble() - 0.5) * 1.2, 0, 0.02, 0);
    }

    public static void addLines(List<String> lines, MinecraftClient mc) {
        var p = mc.player;
        if (on("offhand")) {
            ItemStack o = p.getOffHandStack();
            if (!o.isEmpty()) lines.add("Off: " + o.getName().getString() + " x" + o.getCount());
        }
        if (on("armorPoints")) lines.add("Armor: " + p.getArmor());
        if (on("fallDist")) lines.add(String.format("Fall: %.1f", p.isOnGround() ? lastFall : Math.max(0, fallPeak - p.getY())));
        if (on("hitCount")) lines.add("Hits: " + Extras.hits);
        if (on("accuracy")) lines.add("Accuracy: " + (totalSwings == 0 ? 0 : Math.min(100, Extras.hits * 100 / totalSwings)) + "%");
        if (on("lastReach")) lines.add(String.format("Reach: %.2f", Extras.lastReach));
        if (on("avgCps")) lines.add(String.format("Avg CPS: %.1f", totalSwings / Math.max(1.0, (lastSwing - firstSwing) / 1000.0 + 1)));
        if (on("maxCps")) lines.add("Max CPS: " + maxCps);
        if (on("distance")) lines.add(String.format("Walked: %.0f m", dist));
        if (on("clock12")) lines.add("Time: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));
        if (on("deaths")) lines.add("Deaths: " + deaths);
    }

    /** Full-screen layers drawn BEFORE the HUD. */
    public static void renderBack(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.options.hudHidden) return;
        var p = mc.player;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        long now = System.currentTimeMillis();
        if (on("bottomFade")) {
            int col = rgb("bottomFade");
            for (int i = 0; i < 16; i++) ctx.fill(0, sh - (i + 1) * 8, sw, sh - i * 8, ((150 * (16 - i) / 16) << 24) | col);
        }
        if (on("cornerGlow")) {
            int col = rgb("cornerGlow");
            for (int i = 0; i < 8; i++) {
                int size = (8 - i) * 10, cc = (18 << 24) | col;
                ctx.fill(0, 0, size, size, cc); ctx.fill(sw - size, 0, sw, size, cc);
                ctx.fill(0, sh - size, size, sh, cc); ctx.fill(sw - size, sh - size, sw, sh, cc);
            }
        }
        if (on("scanlines")) {
            int cc = (((int) Config.num("scanA", 40f)) << 24) | rgb("scanlines");
            for (int yy = 0; yy < sh; yy += 3) ctx.fill(0, yy, sw, yy + 1, cc);
        }
        if (on("hpGlow")) {
            float f = Math.max(0f, Math.min(1f, p.getHealth() / p.getMaxHealth()));
            int cc = Config.I.colorblind
                ? (90 << 24) | ((int) (255 * (1 - f) + 77 * f) << 16) | (165 << 8) | (int) (255 * f)
                : (90 << 24) | ((int) (255 * (1 - f)) << 16) | ((int) (255 * f) << 8), th = 3;
            ctx.fill(0, 0, sw, th, cc); ctx.fill(0, sh - th, sw, sh, cc);
            ctx.fill(0, th, th, sh - th, cc); ctx.fill(sw - th, th, sw, sh - th, cc);
        }
        if (on("speedLines") && More.spd > 6) {
            int L = 24 + (int) ((now / 8) % 30), cc = (120 << 24) | rgb("speedLines"), step = Math.max(1, sh / 10);
            for (int i = 0; i < 10; i++) {
                int yy = i * step + (int) ((now / 12) % step);
                ctx.fill(0, yy, L, yy + 1, cc); ctx.fill(sw - L, yy, sw, yy + 1, cc);
            }
        }
        if (on("pillarbox")) {
            int w = (int) Config.num("pillarW", 40f), cc = 0xFF000000 | rgb("pillarbox");
            ctx.fill(0, 0, w, sh, cc); ctx.fill(sw - w, 0, sw, sh, cc);
        }
        if (on("topLine")) ctx.fill(0, 0, sw, 2, 0xFF000000 | rgb("topLine"));
        if (on("bottomLine")) ctx.fill(0, sh - 2, sw, sh, 0xFF000000 | rgb("bottomLine"));
    }

    public static void render(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.options.hudHidden) return;
        var p = mc.player;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight(), cx = sw / 2, cy = sh / 2;
        long now = System.currentTimeMillis();
        if (on("xpBar")) More.bar(ctx, cx - 20, cy + 58, 40, 3, p.experienceProgress, 0xFF000000 | rgb("xpBar"));
        if (on("airBar") && p.getAir() < p.getMaxAir())
            More.bar(ctx, cx - 20, cy + 62, 40, 3, p.getAir() / (float) p.getMaxAir(), 0xFF000000 | rgb("airBar"));
        if (on("darkWarn") && mc.world.getLightLevel(p.getBlockPos()) <= 7)
            ctx.drawText(mc.textRenderer, "DARK", cx - 34, cy - 14, 0xFFFF8855, true);
        if (on("chShadow")) {
            int cc = (140 << 24) | rgb("chShadow");
            for (int i = 0; i < 20; i++) {
                double a = Math.PI * 2 * i / 20;
                int px = cx + (int) Math.round(Math.cos(a) * 9), py = cy + (int) Math.round(Math.sin(a) * 9);
                ctx.fill(px - 1, py - 1, px + 2, py + 2, cc);
            }
        }
        long age = now - Extras.lastHitMs();
        if (on("hitRing") && age < 400) {
            int cc = ((int) (255 * (1 - age / 400.0)) << 24) | rgb("hitRing"), r = 6 + (int) (age / 10);
            for (int i = 0; i < 24; i++) {
                double a = Math.PI * 2 * i / 24;
                int px = cx + (int) Math.round(Math.cos(a) * r), py = cy + (int) Math.round(Math.sin(a) * r);
                ctx.fill(px, py, px + 2, py + 2, cc);
            }
        }
    }
}
