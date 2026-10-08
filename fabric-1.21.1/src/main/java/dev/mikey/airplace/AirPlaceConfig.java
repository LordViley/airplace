package dev.mikey.airplace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Fabric has no bundled config framework like NeoForge's ModConfigSpec, so this is a small
 * hand-rolled JSON config using Gson (already on the classpath via Minecraft itself).
 */
public final class AirPlaceConfig {

    public static final class Data {
        public int horizontalRadius = 2;   // 2 = 5x5 footprint
        public int verticalRadius = 2;     // 2 = 5 layers tall
        public double reach = 6.0D;
        public boolean strictAirOnly = false;
        public boolean toggleMode = true;
        public String candidateColor = "FF3B30";
        public String targetColor = "34C759";
        public double outlineAlpha = 0.85D;
    }

    public static volatile Data CURRENT = new Data();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("airplace.json");
    }

    public static void load() {
        Path p = path();
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p)) {
                Data loaded = GSON.fromJson(r, Data.class);
                if (loaded != null) {
                    CURRENT = loaded;
                    clamp();
                    return;
                }
            } catch (IOException | RuntimeException e) {
                AirPlace.LOGGER.warn("Failed to read airplace.json, using defaults", e);
            }
        }
        save(); // write defaults on first run
    }

    /** Keeps hand-edited values sane. */
    private static void clamp() {
        Data d = CURRENT;
        d.horizontalRadius = Math.max(0, Math.min(8, d.horizontalRadius));
        d.verticalRadius = Math.max(0, Math.min(8, d.verticalRadius));
        d.reach = Math.max(1.0D, Math.min(16.0D, d.reach));
        d.outlineAlpha = Math.max(0.05D, Math.min(1.0D, d.outlineAlpha));
    }

    public static void save() {
        try {
            Files.createDirectories(path().getParent());
            try (Writer w = Files.newBufferedWriter(path())) {
                GSON.toJson(CURRENT, w);
            }
        } catch (IOException e) {
            AirPlace.LOGGER.warn("Failed to write airplace.json", e);
        }
    }

    /** Parses "RRGGBB" (with or without a leading #) into 0xRRGGBB, falling back on bad input. */
    public static int rgb(String hex, int fallback) {
        if (hex == null) return fallback;
        String s = hex.trim();
        if (s.startsWith("#")) s = s.substring(1);
        try {
            return Integer.parseUnsignedInt(s, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private AirPlaceConfig() {}
}
