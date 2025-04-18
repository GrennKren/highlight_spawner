package com.highlightspawner;

import com.highlightspawner.render.SpawnerHighlightRenderer;
import com.highlightspawner.config.SpawnerHighlightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class HighlightSpawnerClient implements ClientModInitializer {
	public static KeyBinding TOGGLE_HIGHLIGHT_KEY;

	@Override
	public void onInitializeClient() {
		HighlightSpawner.LOGGER.info("Initializing Highlight Spawner Client");

		// Register our renderer to the WorldRenderEvents
		WorldRenderEvents.AFTER_TRANSLUCENT.register(new SpawnerHighlightRenderer());

		// Registrasi keybind, contoh gunakan tombol H
		TOGGLE_HIGHLIGHT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.highlightspawner.toggle",            // Translation key
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_H,                          // Tombol default
				"category.highlightspawner"               // Kategori keybind
		));

		// Cek setiap akhir tick client untuk mendeteksi key press
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (TOGGLE_HIGHLIGHT_KEY.wasPressed()) {
				SpawnerHighlightConfig.INSTANCE.highlightEnabled = !SpawnerHighlightConfig.INSTANCE.highlightEnabled;
				// Di sini Anda juga bisa menampilkan feedback ke pemain, misalnya chat message:
				if (client.player != null) {
					client.player.sendMessage(
							net.minecraft.text.Text.of("Spawner Highlight " + (SpawnerHighlightConfig.INSTANCE.highlightEnabled ? "Enabled" : "Disabled")),
							false
					);
				}
				// Simpan konfigurasi jika diperlukan
			}
		});

		// Pastikan konfigurasi dimuat saat mod dimulai
		SpawnerHighlightConfig.INSTANCE.load();

		// Bentuk lambda yang lebih singkat (expression lambda)
		Runtime.getRuntime().addShutdownHook(new Thread(SpawnerHighlightConfig.INSTANCE::save));
	}
}