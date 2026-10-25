package dev.auroravisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static class Pos {
        public float x, y;
        public transient float w = 60, h = 14;
        public Pos() {}
        public Pos(float x, float y) { this.x = x; this.y = y; }
    }
    public static final String[] PRESETS = {"Ocean", "Sunset", "Mint", "Violet", "Sakura", "Rainbow"};
    public static final String[] TEXT_MODES = {"White", "Accent"};
    public Pos pPanel = new Pos(0.01f, 0.01f), pKeys = new Pos(0.01f, 0.36f), pArmor = new Pos(0.9f, 0.28f),
               pPot = new Pos(0.7f, 0.5f), pTarget = new Pos(0.56f, 0.6f), pCd = new Pos(0.01f, 0.58f);
    public boolean cdHud = true, cdReady = true, combatTimer = true, lowHp = true, targetArmor = true, showReach = true, pops = false;
    public int combatSecs = 15, lowHpThreshold = 8;
    // info 2
    public boolean day = false, worldTime = false, biome = false, light = false, angles = false, heldItem = false,
                   netherCoords = false, hpText = false, hungerText = false, xpLevel = false, session = false,
                   fpsColor = true, pingColor = true, keysMouse = true;
    // visuals 2
    public boolean compass = false, lowFood = true, atkBar = false, frame = false, hurtFlash = false, kills = false,
                   bestCombo = false, chTargetColor = true;
    public int chAlpha = 255, frameThick = 3;
    // cosmetics 2
    public int pShape = 0;
    public boolean ring = false, helix = false, cloud = false, jumpRings = false, lvlBurst = false, fireflies = false;
    public float ringRadius = 0.7f;
    // colors
    public boolean accentCustom = false, chCustom = false;
    public int accentRgb = 0x4DA6FF, chRgb = 0xFFFFFF, partRgb = 0x4DA6FF;
    // data-driven features (see Feats / More)
    public java.util.Map<String, Boolean> on = new java.util.HashMap<>();
    public java.util.Map<String, Integer> rgb = new java.util.HashMap<>();
    public java.util.Map<String, Float> num = new java.util.HashMap<>();
    public Pos pGraph = new Pos(0.78f, 0.88f);
    public static boolean on(String k) { Boolean b = I.on.get(k); return b != null && b; }
    public static int rgb(String k, int d) { Integer v = I.rgb.get(k); return v == null ? d : v; }
    public static float num(String k, float d) { Float v = I.num.get(k); return v == null ? d : v; }
    // comfort / accessibility
    public boolean colorblind = false, calm = false, highContrast = false, reducedMotion = false, streamer = false,
                   quiet = false, compact = false, welcomeHint = true, hideInChat = true, keepOnScreen = true, autoShrink = true;
    // safety
    public boolean perfGuard = true;
    public int perfMinFps = 40;
    // cosmetics 3
    public boolean snow = false, heartAura = false, notes = false, electric = false, flameFeet = false,
                   souls = false, sakura = false, enchant = false, totemAura = false;
    public int cosIntensity = 2, killType = 0;
    public boolean wings = false, killEffect = true, noBob = false, noFovFx = false, hudBorder = false, accentBar = true;
    public float wingSize = 1f;
    public int textMode = 0, preset = 0;
    public static Config I = new Config();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("auroravisuals.json");

    // HUD
    public boolean watermark = true, fps = true, coords = true, cps = true, ping = true, keystrokes = true;
    public int hudX = 4, hudY = 4;
    public float hudScale = 1f;
    public boolean textShadow = true;
    // Crosshair
    public boolean crosshair = true, chDot = false, chOutline = true, chCross = true, chRainbow = false, chDynamic = false;
    public int chSize = 5, chGap = 3, chThick = 1;
    public boolean hitmarker = true;
    public int hitSize = 5, hitTicks = 8;
    // Theme
    public float hue = 0.58f, rainbowSpeed = 1f;
    public boolean rainbow = false;
    public int bgOpacity = 110;
    // Info widgets
    public boolean speed = false, direction = false, clock = false, memory = false, server = false, combo = true;
    public boolean armorHud = true, potionHud = true, targetHud = true;
    // Effects
    public boolean trail = false, trailFirstPerson = false, hat = false, hitParticles = true;
    public int trailDensity = 2, hatPoints = 14, hitCount = 8, hitType = 1;
    public float hatSize = 0.55f, hatHeight = 0.25f;
    public int hitSound = 0, pColor = 0, zoomFov = 40;
    public boolean state = false, itemCount = false, chCircle = false;
    // Cosmetics
    public boolean cosFirst = false, halo = false, aura = false, footsteps = false;
    public float haloSize = 0.35f, haloHeight = 0.3f, auraRadius = 0.8f, auraSpeed = 1f;
    public int auraCount = 6;

    public int accent() {
        if (accentCustom && !rainbow) return 0xFF000000 | accentRgb;
        float h = hue;
        if (rainbow) h += (System.currentTimeMillis() % 10000L) / 10000f * rainbowSpeed * 3f;
        return Color.HSBtoRGB(h % 1f, 0.65f, 1f) | 0xFF000000;
    }
    public int bg() { return ((highContrast ? Math.max(bgOpacity, 200) : bgOpacity) << 24); }
    public static int good() { return I.colorblind ? 0xFF4DA6FF : 0xFF55FF55; }
    public static int bad() { return I.colorblind ? 0xFFFFA500 : 0xFFFF5555; }
    public int textCol() { return (textMode == 1 && !highContrast) ? accent() : 0xFFFFFFFF; }
    public static void applyPreset(int i) {
        float[] hues = {0.58f, 0.04f, 0.42f, 0.75f, 0.92f};
        I.preset = i;
        I.rainbow = i >= hues.length;
        if (i < hues.length) I.hue = hues[i];
    }
    public static void resetPositions() {
        Config d = new Config();
        I.pPanel = d.pPanel; I.pKeys = d.pKeys; I.pArmor = d.pArmor; I.pPot = d.pPot; I.pTarget = d.pTarget; I.pCd = d.pCd; I.pGraph = d.pGraph;
    }
    public static void reset() { I = new Config(); save(); }

    private static void keepLook(Config o, Config n) {
        n.hue = o.hue; n.rainbow = o.rainbow; n.rainbowSpeed = o.rainbowSpeed; n.bgOpacity = o.bgOpacity; n.preset = o.preset;
        n.accentCustom = o.accentCustom; n.accentRgb = o.accentRgb; n.chCustom = o.chCustom; n.chRgb = o.chRgb; n.partRgb = o.partRgb;
        n.hudScale = o.hudScale; n.textShadow = o.textShadow; n.textMode = o.textMode;
        n.colorblind = o.colorblind; n.calm = o.calm; n.highContrast = o.highContrast; n.reducedMotion = o.reducedMotion;
        n.streamer = o.streamer; n.quiet = o.quiet; n.compact = o.compact; n.welcomeHint = o.welcomeHint;
        n.hideInChat = o.hideInChat; n.keepOnScreen = o.keepOnScreen; n.autoShrink = o.autoShrink;
        n.perfGuard = o.perfGuard; n.perfMinFps = o.perfMinFps;
        n.pPanel = o.pPanel; n.pKeys = o.pKeys; n.pArmor = o.pArmor; n.pPot = o.pPot; n.pTarget = o.pTarget; n.pCd = o.pCd; n.pGraph = o.pGraph; n.rgb = o.rgb; n.num = o.num;
    }

    /** 0 = minimal, 1 = PvP, 2 = cosmetic. Theme and HUD positions are kept. */
    public static void applyProfile(int i) {
        Config n = new Config();
        keepLook(I, n);
        if (i == 0) {
            n.watermark = false; n.coords = false; n.ping = false; n.keystrokes = false; n.combo = false;
            n.armorHud = false; n.potionHud = false; n.targetHud = false; n.cdHud = false; n.lowHp = false;
            n.crosshair = false; n.hitmarker = false; n.hitParticles = false; n.killEffect = false; n.lowFood = false;
        } else if (i == 1) {
            n.pops = true; n.itemCount = true; n.atkBar = true; n.hurtFlash = true; n.kills = true; n.bestCombo = true;
            n.hitSound = 1; n.chDot = true; n.chOutline = true;
        } else {
            n.wings = true; n.halo = true; n.aura = true; n.trail = true; n.ring = true; n.footsteps = true;
            n.sakura = true; n.heartAura = true; n.cosFirst = true; n.watermark = true;
        }
        I = n; save();
    }
    private static Path slot(int n) { return FabricLoader.getInstance().getConfigDir().resolve("auroravisuals_slot" + n + ".json"); }
    public static void saveSlot(int n) { try { Files.writeString(slot(n), GSON.toJson(I)); } catch (Exception ignored) {} }
    public static boolean loadSlot(int n) {
        try {
            if (!Files.exists(slot(n))) return false;
            Config c = GSON.fromJson(Files.readString(slot(n)), Config.class);
            if (c == null) return false;
            c.sanitize(); I = c; save(); return true;
        } catch (Exception e) { return false; }
    }

    /** One-click comfortable settings for people who are sensitive to flashing, motion or low contrast. */
    public static void applyComfort() {
        I.calm = true; I.reducedMotion = true; I.highContrast = true; I.compact = true;
        I.hideInChat = true; I.keepOnScreen = true; I.autoShrink = true; I.perfGuard = true;
        I.cosIntensity = 1; I.hurtFlash = false; I.frame = false;
        save();
    }

    public static String toJson() { return GSON.toJson(I); }

    /** Replaces the current config with the given JSON. Returns false if it is not a valid config. */
    public static boolean applyJson(String json) {
        try {
            Config c = GSON.fromJson(json, Config.class);
            if (c == null) return false;
            c.sanitize();
            I = c;
            save();
            return true;
        } catch (Exception e) { return false; }
    }

    private static int ci(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static float cf(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }

    /** Repairs missing or out-of-range values (e.g. from a hand-edited or old config file). */
    public void sanitize() {
        if (on == null) on = new java.util.HashMap<>();
        if (rgb == null) rgb = new java.util.HashMap<>();
        if (num == null) num = new java.util.HashMap<>();
        Config d = new Config();
        if (pPanel == null) pPanel = d.pPanel;
        if (pKeys == null) pKeys = d.pKeys;
        if (pArmor == null) pArmor = d.pArmor;
        if (pPot == null) pPot = d.pPot;
        if (pTarget == null) pTarget = d.pTarget;
        if (pCd == null) pCd = d.pCd;
        if (pGraph == null) pGraph = d.pGraph;
        hudScale = cf(hudScale, 0.5f, 2f); hue = cf(hue, 0f, 1f); rainbowSpeed = cf(rainbowSpeed, 0.1f, 5f);
        bgOpacity = ci(bgOpacity, 0, 255); chAlpha = ci(chAlpha, 30, 255); frameThick = ci(frameThick, 1, 10);
        hitCount = ci(hitCount, 1, 30); cosIntensity = ci(cosIntensity, 1, 5); trailDensity = ci(trailDensity, 1, 8);
        hatPoints = ci(hatPoints, 6, 30); auraCount = ci(auraCount, 1, 16); zoomFov = ci(zoomFov, 30, 70);
        combatSecs = ci(combatSecs, 5, 60); lowHpThreshold = ci(lowHpThreshold, 2, 18); perfMinFps = ci(perfMinFps, 20, 120);
        chSize = ci(chSize, 1, 20); chGap = ci(chGap, 0, 15); chThick = ci(chThick, 1, 6);
        hitSize = ci(hitSize, 2, 12); hitTicks = ci(hitTicks, 2, 30);
        preset = ci(preset, 0, PRESETS.length - 1); hitType = ci(hitType, 0, 7); killType = ci(killType, 0, 3);
        pShape = ci(pShape, 0, 5); pColor = ci(pColor, 0, 3); hitSound = ci(hitSound, 0, 3); textMode = ci(textMode, 0, 1);
    }

    public static void load() {
        try { if (Files.exists(FILE)) I = GSON.fromJson(Files.readString(FILE), Config.class); } catch (Exception ignored) {}
        if (I == null) I = new Config();
        I.sanitize();
    }
    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(I)); } catch (Exception ignored) {}
    }
}
