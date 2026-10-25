package dev.auroravisuals;

import net.minecraft.client.MinecraftClient;

/** FPS guard (pauses particle cosmetics when FPS is low) and the panic switch (hides everything). */
public final class Perf {
    public static boolean panic = false, hudHidden = false, fxOff = false;
    public static long joinAt = 0;
    private static Object lastPlayer = null;
    private static boolean limited = false;

    public static void update(MinecraftClient mc) {
        if (mc.player != lastPlayer) { lastPlayer = mc.player; joinAt = System.currentTimeMillis(); }
        Config c = Config.I;
        if (!c.perfGuard) { limited = false; return; }
        int fps = mc.getCurrentFps();
        if (limited) { if (fps > c.perfMinFps + 15) limited = false; }
        else if (fps > 0 && fps < c.perfMinFps) limited = true;
    }

    /** True when particle cosmetics may spawn. */
    public static boolean ok() { return !panic && !fxOff && !limited; }
    public static boolean isLimited() { return limited && !panic; }

    private Perf() {}
}
