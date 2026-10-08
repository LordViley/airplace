package dev.mikey.airplace.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class AirPlaceKeys {

    public static final String CATEGORY = "key.categories.airplace";

    public static KeyMapping TOGGLE;

    public static void register() {
        TOGGLE = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.airplace.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY));
    }

    private AirPlaceKeys() {}
}
