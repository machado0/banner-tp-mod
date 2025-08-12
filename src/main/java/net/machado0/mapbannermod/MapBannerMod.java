package net.machado0.mapbannermod;

import net.fabricmc.api.ModInitializer;

import net.machado0.mapbannermod.handler.MapInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapBannerMod implements ModInitializer {
	public static final String MOD_ID = "mapbannermod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MapInteractionHandler.register();
	}
}