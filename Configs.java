package dev.auroravisuals;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/** Named config files (config/auroravisuals_configs/NAME.json) and clipboard import/export. */
public final class Configs {
    private static Path dir() {
        Path d = FabricLoader.getInstance().getConfigDir().resolve("auroravisuals_configs");
        try { Files.createDirectories(d); } catch (Exception ignored) {}
        return d;
    }

    /** Only letters, digits, _ and - are allowed in a name. */
    public static String clean(String n) {
        String s = n == null ? "" : n.trim().replaceAll("[^A-Za-z0-9_\\-]", "_");
        return s.length() > 24 ? s.substring(0, 24) : s;
    }

    public static List<String> list() {
        List<String> out = new ArrayList<>();
        try (var s = Files.list(dir())) {
            s.forEach(p -> {
                String f = p.getFileName().toString();
                if (f.endsWith(".json")) out.add(f.substring(0, f.length() - 5));
            });
        } catch (Exception ignored) {}
        Collections.sort(out, String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    public static boolean save(String name) {
        try { Files.writeString(dir().resolve(name + ".json"), Config.toJson()); return true; }
        catch (Exception e) { return false; }
    }

    public static boolean load(String name) {
        try { return Config.applyJson(Files.readString(dir().resolve(name + ".json"))); }
        catch (Exception e) { return false; }
    }

    public static boolean delete(String name) {
        try { return Files.deleteIfExists(dir().resolve(name + ".json")); }
        catch (Exception e) { return false; }
    }

    public static String export() {
        return "AV1:" + Base64.getEncoder().encodeToString(Config.toJson().getBytes(StandardCharsets.UTF_8));
    }

    public static boolean importFrom(String text) {
        try {
            if (text == null) return false;
            String t = text.trim();
            if (!t.startsWith("AV1:") || t.length() > 300000) return false;
            byte[] raw = Base64.getDecoder().decode(t.substring(4));
            return Config.applyJson(new String(raw, StandardCharsets.UTF_8));
        } catch (Exception e) { return false; }
    }

    private Configs() {}
}
