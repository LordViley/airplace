package dev.mikey.airplace;

import dev.mikey.airplace.net.AirPlaceNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AirPlace.MODID)
public class AirPlace {
    public static final String MODID = "airplace";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public AirPlace(IEventBus modBus, ModContainer container) {
        modBus.addListener(AirPlaceNetwork::register);
        container.registerConfig(ModConfig.Type.COMMON, AirPlaceConfig.SPEC);
    }
}
