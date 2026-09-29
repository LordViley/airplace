package dev.mikey.airplace;

import dev.mikey.airplace.net.AirPlaceNetwork;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirPlace implements ModInitializer {
    public static final String MOD_ID = "airplace";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AirPlaceConfig.load();
        AirPlaceNetwork.registerCommon();
        LOGGER.info("AirPlace loaded - config: {}", AirPlaceConfig.CURRENT);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
