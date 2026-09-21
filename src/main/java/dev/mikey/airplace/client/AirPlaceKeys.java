package dev.mikey.airplace.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.mikey.airplace.AirPlace;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = AirPlace.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AirPlaceKeys {

    public static final String CATEGORY = "key.categories.airplace";

    /** Rebindable from Options -> Controls like any vanilla key. */
    public static KeyMapping TOGGLE;

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        TOGGLE = new KeyMapping(
                "key.airplace.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY);
        event.register(TOGGLE);
    }

    private AirPlaceKeys() {}
}
