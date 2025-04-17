package com.highlightspawner.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screen.Screen;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class SpawnerHighlightConfig {
    // Instance singleton supaya mudah dipakai di mana saja
    public static SpawnerHighlightConfig INSTANCE = new SpawnerHighlightConfig();

    // Variabel konfigurasi
    public float outlineOffset = 0.05F;       // Gap/offset outline
    public float red = 1.0F;
    public float green = 0.0F;
    public float blue = 0.0F;
    public float alpha = 1.0F;
    public int spawnerActivationRange = 16;
    public boolean highlightEnabled = true;

    // (Opsional) Method untuk simpan dan load ke file (gunakan Gson atau library config lain)
    // Di sini bisa ditambahkan metode load() dan save()

    // Contoh method untuk integrasi Cloth Config
    public Screen getConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("Spawner Highlight Config"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Konversi float RGB (0-1) ke int RGB (0-255) dengan lebih presisi
        int redInt = Math.min(255, Math.max(0, (int)(red * 255)));
        int greenInt = Math.min(255, Math.max(0, (int)(green * 255)));
        int blueInt = Math.min(255, Math.max(0, (int)(blue * 255)));
        int colorRGB = (redInt << 16) | (greenInt << 8) | blueInt;

        builder.getOrCreateCategory(Text.of("General"))
                .addEntry(entryBuilder
                        .startFloatField(Text.of("Outline Offset"), outlineOffset)
                        .setDefaultValue(0.05F)
                        .setMin(0.01F)
                        .setMax(0.5F)
                        .setTooltip(Text.of("Jarak outline dari blok (0.01-0.5)"))
                        .setSaveConsumer(val -> outlineOffset = val)
                        .build())
                .addEntry(entryBuilder
                        .startColorField(Text.of("Outline Color"), colorRGB)
                        .setDefaultValue(0xFF0000) // Merah default
                        .setTooltip(Text.of("Warna outline spawner"))
                        .setSaveConsumer(colorInt -> {
                            // Konversi int RGB (0-255) kembali ke float RGB (0-1) dengan lebih presisi
                            red = ((colorInt >> 16) & 0xFF) / 255.0F;
                            green = ((colorInt >> 8) & 0xFF) / 255.0F;
                            blue = (colorInt & 0xFF) / 255.0F;
                        })
                        .build())
                .addEntry(entryBuilder
                        .startFloatField(Text.of("Alpha (Transparency)"), alpha)
                        .setDefaultValue(1.0F)
                        .setMin(0.1F)
                        .setMax(1.0F)
                        .setTooltip(Text.of("Transparansi outline (0.1-1.0)"))
                        .setSaveConsumer(val -> alpha = val)
                        .build())
                .addEntry(entryBuilder
                        .startIntSlider(Text.of("Spawner Activation Range"), spawnerActivationRange, 4, 32)
                        .setDefaultValue(16)
                        .setTooltip(Text.of("Jarak aktivasi spawner dalam blok (4-32)"))
                        .setSaveConsumer(val -> spawnerActivationRange = val)
                        .build())
                .addEntry(entryBuilder
                        .startBooleanToggle(Text.of("Enable Highlight"), highlightEnabled)
                        .setDefaultValue(true)
                        .setTooltip(Text.of("Aktifkan/nonaktifkan highlight spawner"))
                        .setSaveConsumer(val -> highlightEnabled = val)
                        .build());

        return builder.build();
    }
}
