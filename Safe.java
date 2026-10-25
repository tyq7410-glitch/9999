package dev.auroravisuals;

import java.util.HashSet;
import java.util.Set;

/** Runs a feature and never lets an exception reach the game: the error is printed once and the frame continues. */
public final class Safe {
    private static final Set<String> logged = new HashSet<>();

    public static void run(String name, Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            if (logged.add(name)) {
                System.err.println("[AuroraVisuals] feature '" + name + "' failed and was skipped: " + t);
                t.printStackTrace();
            }
        }
    }

    private Safe() {}
}
