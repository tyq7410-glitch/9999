package dev.auroravisuals;

import java.util.ArrayList;
import java.util.List;

/** Table of the data-driven features: key, display name, has a color, default color. */
public final class Feats {
    public record F(String key, String name, boolean colored, int def) {}

    public static final F[] COS = {
        new F("orbitStars", "Orbit stars", true, 0xFFE066), new F("dna", "DNA helix", true, 0x55FFAA),
        new F("pentagram", "Pentagram", true, 0xFF4D4D), new F("groundRing", "Ground ring", true, 0x4DA6FF),
        new F("hexagon", "Hex circle", true, 0xC84DFF), new F("rainbowArc", "Rainbow arc", false, 0),
        new F("butterflies", "Butterflies", true, 0xFF88DD), new F("stormHalo", "Storm halo", false, 0),
        new F("comet", "Comet", true, 0x66CCFF), new F("heartRing", "Heart ring", false, 0),
        new F("tornado", "Tornado", true, 0xCCCCCC), new F("bubbles", "Bubbles", false, 0),
        new F("smoke", "Smoke trail", false, 0), new F("glow", "Glow dust", false, 0),
        new F("spores", "Spore bloom", false, 0), new F("drips", "Honey drips", false, 0),
        new F("critHalo", "Crit halo", false, 0), new F("galaxy", "Galaxy disc", true, 0x8A4DFF),
        new F("sphere", "Shield sphere", true, 0x4DDBFF), new F("feathers", "Feathers", false, 0),
        new F("sparkler", "Sparkler", false, 0), new F("fountain", "Fountain", true, 0xFFC83D),
        new F("starRain", "Star rain", false, 0), new F("fireRing", "Fire ring", false, 0),
        new F("iceCrown", "Ice crown", true, 0xA6D1FF), new F("lavaDrips", "Lava drips", false, 0),
        new F("portalSwirl", "Portal swirl", false, 0), new F("mist", "Ground mist", false, 0)};

    public static final F[] HUD = {
        new F("hpBar", "Mini HP bar", true, 0xFF4D4D), new F("armorBar", "Mini armor bar", true, 0x8899AA),
        new F("foodBar", "Mini food bar", true, 0xFFAA33), new F("cdRing", "Attack ring", true, 0x55FF55),
        new F("dmgTaken", "Damage taken", false, 0), new F("healPop", "Heal popup", false, 0),
        new F("sprintDot", "Sprint dot", true, 0x55FF55), new F("sneakText", "Sneak label", false, 0),
        new F("speedBar", "Speed bar", true, 0x4DDBFF), new F("invSlots", "Inventory slots", false, 0),
        new F("dayPhase", "Day phase", false, 0), new F("moonPhase", "Moon phase", false, 0),
        new F("lookBlock", "Looking at", false, 0), new F("weather", "Weather", false, 0),
        new F("fpsGraph", "FPS graph", true, 0x55FF55), new F("chunk", "Chunk coords", false, 0)};

    public static final F[] SCR = {
        new F("vignette", "Vignette", true, 0x000000), new F("letterbox", "Letterbox", true, 0x000000),
        new F("tint", "Screen tint", true, 0x3355FF), new F("corners", "Corner brackets", true, 0xFFFFFF),
        new F("comboBar", "Combo bar", true, 0xFFC83D), new F("pulseFrame", "Pulse frame", true, 0xFF4DDB)};

    public static final F[] COS2 = {
        new F("dblRing", "Double ring", true, 0x4DDBFF), new F("spiralUp", "Rising spiral", true, 0xFF8A3D),
        new F("hexStar", "Hexagram", true, 0xFFC83D), new F("crescent", "Crescent", true, 0xFFF0A0),
        new F("sunRays", "Sun rays", true, 0xFFC83D), new F("rose", "Rose curve", true, 0xFF4D94),
        new F("infinity", "Infinity", true, 0xFF4DDB), new F("heartShape", "Heart shape", true, 0xFF4D94),
        new F("atom", "Atom", true, 0x55FFAA), new F("waveRing", "Wave ring", true, 0x3DFFC4),
        new F("bolt", "Lightning bolt", true, 0xFFF04D), new F("dome", "Dome", true, 0x4DA6FF),
        new F("ribbon", "Ribbon", true, 0xFF4D94), new F("planets", "Planets", true, 0xC84DFF),
        new F("pulseWave", "Pulse wave", true, 0xFF4D4D), new F("scanRing", "Scan ring", true, 0x4DFF6A),
        new F("matrix", "Matrix rain", true, 0x33FF66), new F("wisps", "Wisps", true, 0x8A4DFF),
        new F("flowers", "Flower patch", true, 0xFF88AA), new F("cape", "Dust cape", true, 0xC84DFF),
        new F("smokeRing", "Smoke ring", false, 0), new F("soulRing", "Soul ring", false, 0),
        new F("totemRing", "Totem ring", false, 0), new F("waterDrips", "Water drips", false, 0),
        new F("noteRing", "Note ring", false, 0), new F("embers", "Embers", false, 0)};

    public static final F[] HUD2 = {
        new F("xpBar", "Mini XP bar", true, 0x80FF20), new F("airBar", "Air bar", true, 0x55AAFF),
        new F("offhand", "Offhand item", false, 0), new F("armorPoints", "Armor points", false, 0),
        new F("fallDist", "Fall distance", false, 0), new F("darkWarn", "Dark warning", false, 0),
        new F("hitCount", "Hit counter", false, 0), new F("accuracy", "Accuracy", false, 0),
        new F("lastReach", "Last hit reach", false, 0), new F("avgCps", "Average CPS", false, 0),
        new F("maxCps", "Max CPS", false, 0), new F("distance", "Distance walked", false, 0),
        new F("clock12", "Clock 12h", false, 0), new F("deaths", "Death counter", false, 0)};

    public static final F[] SCR2 = {
        new F("scanlines", "Scanlines", true, 0x000000), new F("hitRing", "Hit ring", true, 0xFFFFFF),
        new F("hpGlow", "Health glow", false, 0), new F("speedLines", "Speed lines", true, 0xFFFFFF),
        new F("pillarbox", "Pillarbox", true, 0x000000), new F("topLine", "Top line", true, 0x4DA6FF),
        new F("bottomLine", "Bottom line", true, 0x4DA6FF), new F("chShadow", "Crosshair shadow", true, 0x000000),
        new F("bottomFade", "Bottom fade", true, 0x000000), new F("cornerGlow", "Corner glow", true, 0x4DA6FF)};

    public static final F[] PVP = {
        new F("critReady", "Crit ready", true, 0xFFC83D), new F("blockState", "Block state", false, 0),
        new F("killFlash", "Kill flash", true, 0xFFC83D), new F("dmgDealt", "Damage dealt", true, 0xFFAA33),
        new F("armorAlert", "Armor alert", false, 0), new F("totemAlert", "Totem alert", false, 0),
        new F("effectWarn", "Effect expiring", false, 0), new F("distBar", "Target distance bar", true, 0x4DDBFF),
        new F("lastAttacker", "Last attacker", false, 0), new F("pingWarn", "Ping warning", false, 0)};

    public static final F[] PVE = {
        new F("toolBar", "Tool durability bar", true, 0x55FF55), new F("toolWarn", "Tool warning", false, 0),
        new F("blockBelow", "Block below", false, 0), new F("oreHint", "Ore depth hint", false, 0),
        new F("saturation", "Saturation", false, 0), new F("dayTimer", "Day/night timer", false, 0),
        new F("mountBar", "Mount health bar", true, 0xFF8844), new F("stackTotal", "Item total", false, 0),
        new F("deathPoint", "Death point", false, 0), new F("fallDmg", "Fall damage hint", false, 0)};

    public static List<F> colored() {
        List<F> out = new ArrayList<>();
        for (F[] arr : new F[][]{COS, HUD, SCR, COS2, HUD2, SCR2, PVP, PVE}) for (F f : arr) if (f.colored()) out.add(f);
        return out;
    }
    private Feats() {}
}
