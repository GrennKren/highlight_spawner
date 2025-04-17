package com.highlightspawner;

import com.highlightspawner.render.SpawnerHighlightRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class HighlightSpawnerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HighlightSpawner.LOGGER.info("Initializing Highlight Spawner Client");

		// Register our renderer to the WorldRenderEvents
		WorldRenderEvents.AFTER_TRANSLUCENT.register(new SpawnerHighlightRenderer());
	}
}