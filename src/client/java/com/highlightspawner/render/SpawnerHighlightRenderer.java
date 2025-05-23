package com.highlightspawner.render;

import com.highlightspawner.config.SpawnerHighlightConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class SpawnerHighlightRenderer implements WorldRenderEvents.AfterTranslucent {
    // Warna outline (RGBA)

    @Override
    public void afterTranslucent(WorldRenderContext context) {
        // Hentikan rendering jika highlight dimatikan
        if (!SpawnerHighlightConfig.INSTANCE.highlightEnabled) {
            return;
        }

        // Ambil nilai konfigurasi
        float offset = SpawnerHighlightConfig.INSTANCE.outlineOffset;
        float red   = SpawnerHighlightConfig.INSTANCE.red;
        float green = SpawnerHighlightConfig.INSTANCE.green;
        float blue  = SpawnerHighlightConfig.INSTANCE.blue;
        float alpha = SpawnerHighlightConfig.INSTANCE.alpha;
        int activationRange = SpawnerHighlightConfig.INSTANCE.spawnerActivationRange;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        // Gunakan nilai activationRange dari konfigurasi saat mencari spawner
        List<BlockPos> spawnerPositions = findSpawnersInRange(client.world, activationRange);
        if (spawnerPositions.isEmpty()) {
            return;
        }

        MatrixStack matrices = context.matrixStack();
        // Tambahkan pengecekan null untuk matrices
        if (matrices == null) {
            return;
        }

        // Pengecekan untuk camera juga
        if (context.camera() == null) {
            return;
        }

        matrices.push();
        Vec3d cameraPos = context.camera().getPos();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Simpan state state yang Anda ubah (depth, blend, cull, dll)
        boolean depthEnabled = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        boolean blendEnabled = GL11.glGetBoolean(GL11.GL_BLEND);
        boolean cullEnabled  = GL11.glGetBoolean(GL11.GL_CULL_FACE);
        int oldDepthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);

        // Setup state untuk outline
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        GL11.glLineWidth(3.0f);

        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer lineConsumer = immediate.getBuffer(RenderLayer.getLines());

        for (BlockPos pos : spawnerPositions) {
            drawSpawnerOutline(matrices, lineConsumer, pos, offset, red, green, blue, alpha);
        }
        immediate.draw();

        // Kembalikan state render
        GL11.glLineWidth(1.0f);
        RenderSystem.depthMask(true);
        GL11.glDepthFunc(oldDepthFunc);
        if (!depthEnabled) RenderSystem.disableDepthTest();
        if (!blendEnabled) RenderSystem.disableBlend();
        if (cullEnabled) RenderSystem.enableCull();

        matrices.pop();
    }


    @SuppressWarnings("deprecation")
    private List<BlockPos> findSpawnersInRange(World world, int activationRange) {
        List<BlockPos> spawners = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();

        // Cek apakah player sudah tersedia
        if (client.player == null) {
            return spawners;
        }

        Vec3d playerPosVec = client.player.getPos();
        BlockPos playerPos = BlockPos.ofFloored(playerPosVec);

        // Pastikan kita scan cukup besar agar bisa mencapai spawner di diagonal
        int scanRange = activationRange + 1;

        for (int x = -scanRange; x <= scanRange; x++) {
            for (int y = -scanRange; y <= scanRange; y++) {
                for (int z = -scanRange; z <= scanRange; z++) {
                    BlockPos checkPos = playerPos.add(x, y, z);

                    if (!world.isChunkLoaded(checkPos)) continue;

                    if (world.getBlockState(checkPos).getBlock() == Blocks.SPAWNER) {
                        double dx = client.player.getX() - (checkPos.getX() + 0.5);
                        double dy = client.player.getY() - (checkPos.getY() + 0.5);
                        double dz = client.player.getZ() - (checkPos.getZ() + 0.5);
                        double distanceSquared = dx * dx + dy * dy + dz * dz;

                        if (distanceSquared <= activationRange * activationRange) {
                            spawners.add(checkPos);
                        }
                    }
                }
            }
        }
        return spawners;
    }


    private void drawSpawnerOutline(MatrixStack matrices, VertexConsumer lineConsumer, BlockPos pos, float offset,
                                    float red, float green, float blue, float alpha) {
        // Buat box sedikit lebih besar dari blok untuk outline
        Box box = new Box(pos).expand(offset);

        // Dapatkan matrix untuk transformasi posisi
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Bottom face
        drawLine(lineConsumer, matrix, (float)box.minX, (float)box.minY, (float)box.minZ, (float)box.maxX, (float)box.minY, (float)box.minZ, red, green, blue, alpha);
        drawLine(lineConsumer, matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, (float)box.maxX, (float)box.minY, (float)box.maxZ, red, green, blue, alpha);
        drawLine(lineConsumer, matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, (float)box.minX, (float)box.minY, (float)box.maxZ, red, green, blue, alpha);
        drawLine(lineConsumer, matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, (float)box.minX, (float)box.minY, (float)box.minZ, red, green, blue, alpha);

        // Top face
        drawLine(lineConsumer, matrix, (float)box.minX, (float)box.maxY, (float)box.minZ, (float)box.maxX, (float)box.maxY, (float)box.minZ, red, green, blue, alpha);
        drawLine(lineConsumer, matrix, (float)box.maxX, (float)box.maxY, (float)box.minZ, (float)box.maxX, (float)box.maxY, (float)box.maxZ, red, green, blue, alpha);
        drawLine(lineConsumer, matrix, (float)box.maxX, (float)box.maxY, (float)box.maxZ, (float)box.minX, (float)box.maxY, (float)box.maxZ, red, green, blue, alpha);
        drawLine(lineConsumer, matrix, (float)box.minX, (float)box.maxY, (float)box.maxZ, (float)box.minX, (float)box.maxY, (float)box.minZ, red, green, blue, alpha);

        // Connecting edges
        drawLine(lineConsumer, matrix, (float)box.minX, (float)box.minY, (float)box.minZ, (float)box.minX, (float)box.maxY, (float)box.minZ, red, green, blue, alpha);
        drawLine(lineConsumer, matrix, (float)box.maxX, (float)box.minY, (float)box.minZ, (float)box.maxX, (float)box.maxY, (float)box.minZ, red, green, blue, alpha);
        drawLine(lineConsumer, matrix, (float)box.maxX, (float)box.minY, (float)box.maxZ, (float)box.maxX, (float)box.maxY, (float)box.maxZ, red, green, blue, alpha);
        drawLine(lineConsumer, matrix, (float)box.minX, (float)box.minY, (float)box.maxZ, (float)box.minX, (float)box.maxY, (float)box.maxZ, red, green, blue, alpha);
    }

    private void drawLine(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
        // Vertex pertama
        builder.vertex(matrix, x1, y1, z1);
        builder.color(red, green, blue, alpha); // Gunakan parameter warna
        builder.normal(1, 1, 1);
        // Vertex kedua
        builder.vertex(matrix, x2, y2, z2);
        builder.color(red, green, blue, alpha); // Gunakan parameter warna
        builder.normal(1, 1, 1);
    }
}