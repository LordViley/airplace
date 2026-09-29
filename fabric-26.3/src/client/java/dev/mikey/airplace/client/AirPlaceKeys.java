package dev.mikey.airplace.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.mikey.airplace.AirPlace;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public final class AirPlaceKeys {

    // Fabric API 26.1 renamed KeyBindingHelper -> KeyMappingHelper, and key mappings need a
    // registered KeyMapping.Category. Its lang key is "key.category.<namespace>.<path>".
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(AirPlace.id("general"));

    public static KeyMapping TOGGLE;

    public static void register() {
        // 26.3 moved input from GLFW to SDL: the key type is now KEYBOARD (was KEYSYM) and the
        // codes are SDL scancodes, so use InputConstants.KEY_* rather than raw GLFW numbers.
        TOGGLE = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.airplace.toggle",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_G,
                CATEGORY));
    }

    private AirPlaceKeys() {}
}
