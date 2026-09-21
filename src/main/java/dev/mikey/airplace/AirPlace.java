package dev.mikey.airplace;

import dev.mikey.airplace.net.AirPlaceNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(AirPlace.MODID)
public class AirPlace {
    public static final String MODID = "airplace";

    public AirPlace(IEventBus modBus, ModContainer container) {
        modBus.addListener(AirPlaceNetwork::register);
        // COMMON: each side loads its own copy of the file. The server always
        // re-validates placement against its own values, so a mismatch is harmless.
        container.registerConfig(ModConfig.Type.COMMON, AirPlaceConfig.SPEC);
    }
}
