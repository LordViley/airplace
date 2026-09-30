package dev.mikey.airplace.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.mikey.airplace.AirPlace;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = AirPlace.MODID, value = Dist.CLIENT)
public final class AirPlaceKeys {

    // NOTE: EventBusSubscriber's bus() parameter no longer exists in 26.3 - the framework now
    // appears to auto-route based on the event type, matching how AirPlaceClient/GhostRenderer
    // already compile fine on the game bus without specifying one.
    // NOTE: KeyMapping's category parameter is now a KeyMapping.Category, not a raw String
    // (the compiler's own error for this constructor spelled out the required type directly).
    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(AirPlace.MODID, "general"));

    public static KeyMapping TOGGLE;

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        TOGGLE = new KeyMapping(
                "key.airplace.toggle",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_G,
                CATEGORY);
        event.register(TOGGLE);
    }

    private AirPlaceKeys() {}
}
