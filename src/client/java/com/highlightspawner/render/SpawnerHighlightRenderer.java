package com.highlightspawner.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class SpawnerHighlightRenderer implements WorldRenderEvents.AfterTranslucent {
    // Radius di mana spawner akan aktif (dalam blok)
    private static final int SPAWNER_ACTIVATION_RANGE = 16;
    // Warna outline (RGBA)
    private static final float RED = 1.0F;
    private static final float GREEN = 0.0F;
    private static final float BLUE = 0.0F;
    private static final float ALPHA = 0.8F;

    @Override
    public void afterTranslucent(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        World world = client.world;

        if (player == null || world == null) {
            return;
        }

        // Cari spawner dalam jangkauan pemain
        List<BlockPos> spawnerPositions = findSpawnersInRange(player, world);

        if (!spawnerPositions.isEmpty()) {
            // Siapkan rendering
            MatrixStack matrices = context.matrixStack();
            matrices.push();

            // Kompensasi untuk camera position
            Vec3d cameraPos = context.camera().getPos();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            // Dapatkan VertexConsumerProvider
            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

            // Gunakan RenderLayer yang sesuai untuk garis
            VertexConsumer lineConsumer = immediate.getBuffer(RenderLayer.getLines());

            // Render outline untuk setiap spawner
            for (BlockPos pos : spawnerPositions) {
                drawSpawnerOutline(matrices, lineConsumer, pos);
            }

            // Flush buffer untuk memastikan semua yang digambar terlihat
            immediate.draw();

            matrices.pop();
        }
    }

    private List<BlockPos> findSpawnersInRange(PlayerEntity player, World world) {
        List<BlockPos> spawners = new ArrayList<>();
        BlockPos playerPos = player.getBlockPos();

        // Scan sekitar area pemain untuk mencari spawner
        int scanRange = SPAWNER_ACTIVATION_RANGE + 4; // Sedikit lebih jauh dari activation range

        for (int x = -scanRange; x <= scanRange; x++) {
            for (int y = -scanRange; y <= scanRange; y++) {
                for (int z = -scanRange; z <= scanRange; z++) {
                    BlockPos checkPos = playerPos.add(x, y, z);

                    // Cek apakah blok adalah spawner
                    if (world.getBlockState(checkPos).getBlock() == Blocks.SPAWNER) {
                        // Cek apakah jarak antara pemain dan spawner dalam jangkauan aktivasi
                        double distance = Math.sqrt(checkPos.getSquaredDistance(playerPos));
                        if (distance <= SPAWNER_ACTIVATION_RANGE) {
                            spawners.add(checkPos);
                        }
                    }
                }
            }
        }

        return spawners;
    }

    private void drawSpawnerOutline(MatrixStack matrices, VertexConsumer lineConsumer, BlockPos pos) {
        // Buat box sedikit lebih besar dari blok untuk outline
        Box box = new Box(pos).expand(0.02);

        // Dapatkan matrix untuk transformasi posisi
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Gunakan metode yang tersedia di 1.21.4 untuk rendering garis

        // Bottom face
        drawLine(lineConsumer, matrix, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ);
        drawLine(lineConsumer, matrix, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
        drawLine(lineConsumer, matrix, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ);
        drawLine(lineConsumer, matrix, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ);

        // Top face
        drawLine(lineConsumer, matrix, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ);
        drawLine(lineConsumer, matrix, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
        drawLine(lineConsumer, matrix, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ);
        drawLine(lineConsumer, matrix, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ);

        // Connecting edges
        drawLine(lineConsumer, matrix, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ);
        drawLine(lineConsumer, matrix, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
        drawLine(lineConsumer, matrix, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
        drawLine(lineConsumer, matrix, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ);
    }

    private void drawLine(VertexConsumer builder, Matrix4f matrix, double x1, double y1, double z1, double x2, double y2, double z2) {
        builder.vertex(matrix, (float)x1, (float)y1, (float)z1).color(RED, GREEN, BLUE, ALPHA).normal(1, 0, 0).vertex(matrix, (float)x2, (float)y2, (float)z2).color(RED, GREEN, BLUE, ALPHA).normal(1, 0, 0);
    }
}