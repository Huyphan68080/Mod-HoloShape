package net.holoshape;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public class CircleRenderer {

    /**
     * Render các outline block ảo và kiểm tra tính hợp lệ của block thực tế trong world.
     */
    public static void renderValidated(WorldRenderContext context, BlockPos center, List<BlockPos> offsets, net.minecraft.block.Block targetBlock, float alpha) {
        if (center == null || offsets == null || offsets.isEmpty()) return;

        net.minecraft.client.world.ClientWorld world = net.minecraft.client.MinecraftClient.getInstance().world;
        if (world == null) return;

        MatrixStack matrices = context.matrices();
        Vec3d cameraPos = context.worldState().cameraRenderState.pos;
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayers.lines());

        matrices.push();
        
        double xOffset = center.getX() - cameraPos.x;
        double yOffset = center.getY() - cameraPos.y;
        double zOffset = center.getZ() - cameraPos.z;
        
        matrices.translate(xOffset, yOffset, zOffset);

        for (BlockPos offset : offsets) {
            BlockPos worldPos = center.add(offset);
            net.minecraft.block.BlockState blockState = world.getBlockState(worldPos);

            float r, g, b;
            if (blockState.isAir()) {
                // Thiếu: Vàng
                r = 1.0f;
                g = 1.0f;
                b = 0.0f;
            } else if (targetBlock != null && blockState.isOf(targetBlock)) {
                // Đúng: Trắng
                r = 1.0f;
                g = 1.0f;
                b = 1.0f;
            } else {
                // Sai: Đỏ
                r = 1.0f;
                g = 0.0f;
                b = 0.0f;
            }

            matrices.push();
            matrices.translate(offset.getX(), offset.getY(), offset.getZ());
            
            drawBoxOutline(matrices, vertexConsumer, r, g, b, alpha);
            
            matrices.pop();
        }

        matrices.pop();
    }

    /**
     * Render các outline block tương đối dựa trên danh sách offset và tọa độ tâm thực tế.
     */
    public static void render(WorldRenderContext context, BlockPos center, List<BlockPos> offsets, float r, float g, float b, float a) {
        if (center == null || offsets == null || offsets.isEmpty()) return;

        MatrixStack matrices = context.matrices();
        Vec3d cameraPos = context.worldState().cameraRenderState.pos;
        VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        // Lấy VertexConsumer dành riêng cho vẽ đường thẳng (Lines)
        VertexConsumer vertexConsumer = consumers.getBuffer(RenderLayers.lines());

        matrices.push();
        
        // Tịnh tiến ma trận về tọa độ Tâm so với Vị trí Camera của người chơi
        double xOffset = center.getX() - cameraPos.x;
        double yOffset = center.getY() - cameraPos.y;
        double zOffset = center.getZ() - cameraPos.z;
        
        matrices.translate(xOffset, yOffset, zOffset);

        // Lặp qua từng block trong danh sách offset để vẽ khung bao (outline)
        for (BlockPos offset : offsets) {
            matrices.push();
            matrices.translate(offset.getX(), offset.getY(), offset.getZ());
            
            drawBoxOutline(matrices, vertexConsumer, r, g, b, a);
            
            matrices.pop();
        }

        matrices.pop();
    }

    /**
     * Vẽ khung đường viền 1x1x1 cho một khối.
     */
    private static void drawBoxOutline(MatrixStack matrices, VertexConsumer vertexConsumer, float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f model = entry.getPositionMatrix();

        // Vẽ 12 cạnh của một hình hộp đơn vị 1x1x1 [0, 1]
        
        // Mặt Đáy (Y = 0)
        vertex(model, vertexConsumer, 0, 0, 0, r, g, b, a, 1, 0, 0);
        vertex(model, vertexConsumer, 1, 0, 0, r, g, b, a, 1, 0, 0);

        vertex(model, vertexConsumer, 1, 0, 0, r, g, b, a, 0, 0, 1);
        vertex(model, vertexConsumer, 1, 0, 1, r, g, b, a, 0, 0, 1);

        vertex(model, vertexConsumer, 1, 0, 1, r, g, b, a, -1, 0, 0);
        vertex(model, vertexConsumer, 0, 0, 1, r, g, b, a, -1, 0, 0);

        vertex(model, vertexConsumer, 0, 0, 1, r, g, b, a, 0, 0, -1);
        vertex(model, vertexConsumer, 0, 0, 0, r, g, b, a, 0, 0, -1);

        // Mặt Đỉnh (Y = 1)
        vertex(model, vertexConsumer, 0, 1, 0, r, g, b, a, 1, 0, 0);
        vertex(model, vertexConsumer, 1, 1, 0, r, g, b, a, 1, 0, 0);

        vertex(model, vertexConsumer, 1, 1, 0, r, g, b, a, 0, 0, 1);
        vertex(model, vertexConsumer, 1, 1, 1, r, g, b, a, 0, 0, 1);

        vertex(model, vertexConsumer, 1, 1, 1, r, g, b, a, -1, 0, 0);
        vertex(model, vertexConsumer, 0, 1, 1, r, g, b, a, -1, 0, 0);

        vertex(model, vertexConsumer, 0, 1, 1, r, g, b, a, 0, 0, -1);
        vertex(model, vertexConsumer, 0, 1, 0, r, g, b, a, 0, 0, -1);

        // Các cạnh đứng nối Mặt Đáy và Mặt Đỉnh
        vertex(model, vertexConsumer, 0, 0, 0, r, g, b, a, 0, 1, 0);
        vertex(model, vertexConsumer, 0, 1, 0, r, g, b, a, 0, 1, 0);

        vertex(model, vertexConsumer, 1, 0, 0, r, g, b, a, 0, 1, 0);
        vertex(model, vertexConsumer, 1, 1, 0, r, g, b, a, 0, 1, 0);

        vertex(model, vertexConsumer, 1, 0, 1, r, g, b, a, 0, 1, 0);
        vertex(model, vertexConsumer, 1, 1, 1, r, g, b, a, 0, 1, 0);

        vertex(model, vertexConsumer, 0, 0, 1, r, g, b, a, 0, 1, 0);
        vertex(model, vertexConsumer, 0, 1, 1, r, g, b, a, 0, 1, 0);
    }

    private static void vertex(Matrix4f model, VertexConsumer consumer, float x, float y, float z, float r, float g, float b, float a, float nx, float ny, float nz) {
        consumer.vertex(model, x, y, z)
                .color(r, g, b, a)
                .normal(nx, ny, nz)
                .lineWidth(1.5f);
    }
}
