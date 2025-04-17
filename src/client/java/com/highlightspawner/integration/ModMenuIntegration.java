package com.highlightspawner.integration;

import com.highlightspawner.config.SpawnerHighlightConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> SpawnerHighlightConfig.INSTANCE.getConfigScreen(parent);
    }
}
