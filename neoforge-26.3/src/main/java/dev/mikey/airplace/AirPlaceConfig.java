package dev.mikey.airplace;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AirPlaceConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue HORIZONTAL_RADIUS;
    public static final ModConfigSpec.IntValue VERTICAL_RADIUS;
    public static final ModConfigSpec.DoubleValue REACH;
    public static final ModConfigSpec.BooleanValue STRICT_AIR_ONLY;
    public static final ModConfigSpec.BooleanValue TOGGLE_MODE;
    public static final ModConfigSpec.ConfigValue<String> COLOR_CANDIDATE;
    public static final ModConfigSpec.ConfigValue<String> COLOR_TARGET;
    public static final ModConfigSpec.DoubleValue OUTLINE_ALPHA;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Which blocks get highlighted and how far you can target.").push("targeting");
        HORIZONTAL_RADIUS = b
                .comment("Horizontal radius around the player. 2 = 5x5 footprint.")
                .defineInRange("horizontalRadius", 2, 0, 8);
        VERTICAL_RADIUS = b
                .comment("Vertical radius around the player. 2 = 5 layers tall.")
                .defineInRange("verticalRadius", 2, 0, 8);
        REACH = b
                .comment("Maximum targeting distance in blocks.")
                .defineInRange("reach", 6.0D, 1.0D, 16.0D);
        STRICT_AIR_ONLY = b
                .comment("true: only pure air. false: any replaceable block (tall grass, snow layers, water).")
                .define("strictAirOnly", false);
        b.pop();

        b.comment("Client-side appearance and controls.").push("display");
        TOGGLE_MODE = b
                .comment("true: tap the hotkey to toggle. false: hold the hotkey.")
                .define("toggleMode", true);
        COLOR_CANDIDATE = b
                .comment("RRGGBB hex for placeable-but-not-aimed-at blocks.")
                .define("candidateColor", "FF3B30");
        COLOR_TARGET = b
                .comment("RRGGBB hex for the block you are aiming at.")
                .define("targetColor", "34C759");
        OUTLINE_ALPHA = b
                .comment("Outline opacity.")
                .defineInRange("outlineAlpha", 0.85D, 0.05D, 1.0D);
        b.pop();

        SPEC = b.build();
    }

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
