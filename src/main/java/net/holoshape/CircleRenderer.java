package net.holoshape;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;

public class CircleRenderer {

    private static RenderLayer xrayLinesLayer = null;
    private static RenderLayer xrayTranslucentLinesLayer = null;
    private static RenderLayer xraySolidLayer = null;

    private static RenderLayer getXrayLinesLayer() {
        if (xrayLinesLayer == null) {
            xrayLinesLayer = createXrayLayer(RenderLayers.lines(), "xray_lines");
        }
        return xrayLinesLayer;
    }

    private static RenderLayer getXrayTranslucentLinesLayer() {
        if (xrayTranslucentLinesLayer == null) {
            xrayTranslucentLinesLayer = createXrayLayer(RenderLayers.linesTranslucent(), "xray_translucent_lines");
        }
        return xrayTranslucentLinesLayer;
    }

    private static RenderLayer getXraySolidLayer() {
        if (xraySolidLayer == null) {
            xraySolidLayer = createXrayLayer(RenderLayers.solid(), "xray_solid");
        }
        return xraySolidLayer;
    }

    private static RenderLayer createXrayLayer(RenderLayer baseLayer, String name) {
        try {
            RenderLayer xrayTextLayer = RenderLayers.textBackgroundSeeThrough();
            
            java.lang.reflect.Field setupField = RenderLayer.class.getDeclaredField("renderSetup");
            setupField.setAccessible(true);
            
            Object baseSetup = setupField.get(baseLayer);
            Object xrayTextSetup = setupField.get(xrayTextLayer);
            
            Class<?> setupClass = Class.forName("net.minecraft.client.render.RenderSetup");
            java.lang.reflect.Field pipelineField = setupClass.getDeclaredField("pipeline");
            pipelineField.setAccessible(true);
            
            com.mojang.blaze3d.pipeline.RenderPipeline xrayPipeline = (com.mojang.blaze3d.pipeline.RenderPipeline) pipelineField.get(xrayTextSetup);
            
            java.lang.reflect.Method builderMethod = setupClass.getDeclaredMethod("builder", com.mojang.blaze3d.pipeline.RenderPipeline.class);
            builderMethod.setAccessible(true);
            Object builder = builderMethod.invoke(null, xrayPipeline);
            
            java.lang.reflect.Field layeringField = setupClass.getDeclaredField("layeringTransform");
            layeringField.setAccessible(true);
            Object layering = layeringField.get(baseSetup);
            builder.getClass().getDeclaredMethod("layeringTransform", Class.forName("net.minecraft.client.render.LayeringTransform"))
                   .invoke(builder, layering);
                  
            java.lang.reflect.Field translucentField = setupClass.getDeclaredField("translucent");
            translucentField.setAccessible(true);
            if (translucentField.getBoolean(baseSetup)) {
                builder.getClass().getDeclaredMethod("translucent").invoke(builder);
            }
            
            java.lang.reflect.Method buildMethod = builder.getClass().getDeclaredMethod("build");
            buildMethod.setAccessible(true);
            net.minecraft.client.render.RenderSetup customSetup = (net.minecraft.client.render.RenderSetup) buildMethod.invoke(builder);
            
            for (java.lang.reflect.Field f : setupClass.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getName().equals("pipeline")) {
                    f.set(customSetup, xrayPipeline);
                } else {
                    f.set(customSetup, f.get(baseSetup));
                }
            }
            
            return RenderLayer.of(name, customSetup);
        } catch (Exception e) {
            e.printStackTrace();
            return baseLayer;
        }
    }

    /**
     * Render các hình dạng ảo với tùy chọn vẽ viền (Lines) hoặc khối đặc mờ (Solid).
     */
    public static void renderShape(WorldRenderContext context, BlockPos center, List<BlockPos> offsets, 
                                   float r, float g, float b, float a, 
                                   int renderMode, BlockState blockState, 
                                   boolean xray, boolean animated) {
        if (center == null || offsets == null || offsets.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        MatrixStack matrices = context.matrices();
        Vec3d cameraPos = context.worldState().cameraRenderState.pos;
        final VertexConsumerProvider consumers = context.consumers();
        if (consumers == null) return;

        long time = System.currentTimeMillis();

        if (renderMode == 1) { // SOLID MODE
            // Lấy RenderLayer khối đặc (đè hoặc xuyên tường)
            RenderLayer solidLayer = xray ? getXraySolidLayer() : RenderLayers.solid();
            
            // Tạo wrapped provider để biến đổi mọi model render thành mờ
            VertexConsumerProvider wrappedProvider = new VertexConsumerProvider() {
                @Override
                public VertexConsumer getBuffer(RenderLayer layer) {
                    // Nếu là layer solid ta dùng xray/solid layer và bọc translucent
                    VertexConsumer parentConsumer = consumers.getBuffer(xray ? getXraySolidLayer() : RenderLayers.solid());
                    return new TranslucentVertexConsumer(parentConsumer, 0.4f);
                }
            };
            
            BlockRenderManager manager = client.getBlockRenderManager();
            
            for (BlockPos offset : offsets) {
                matrices.push();
                double x = center.getX() + offset.getX() - cameraPos.x;
                double y = center.getY() + offset.getY() - cameraPos.y;
                double z = center.getZ() + offset.getZ() - cameraPos.z;
                matrices.translate(x, y, z);
                
                manager.renderBlockAsEntity(blockState, matrices, wrappedProvider, 15728880, OverlayTexture.DEFAULT_UV);
                
                matrices.pop();
            }
        } else { // LINES MODE
            // Lấy RenderLayer đường viền (đè hoặc xuyên tường)
            RenderLayer linesLayer = xray ? getXrayTranslucentLinesLayer() : RenderLayers.linesTranslucent();
            VertexConsumer vertexConsumer = consumers.getBuffer(linesLayer);

            matrices.push();
            double xOffset = center.getX() - cameraPos.x;
            double yOffset = center.getY() - cameraPos.y;
            double zOffset = center.getZ() - cameraPos.z;
            matrices.translate(xOffset, yOffset, zOffset);

            int index = 0;
            for (BlockPos offset : offsets) {
                matrices.push();
                matrices.translate(offset.getX(), offset.getY(), offset.getZ());
                
                float currentAlpha = a;
                if (animated) {
                    double phase = (time / 150.0) - (index * 0.25);
                    float pulse = (float) (0.3 + 0.7 * (0.5 + 0.5 * Math.sin(phase)));
                    currentAlpha = a * pulse;
                }
                
                drawBoxOutline(matrices, vertexConsumer, r, g, b, currentAlpha);
                
                matrices.pop();
                index++;
            }
            matrices.pop();
        }
    }

    private static void drawBoxOutline(MatrixStack matrices, VertexConsumer vertexConsumer, float r, float g, float b, float a) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f model = entry.getPositionMatrix();

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

    /**
     * Wrapper VertexConsumer thay đổi Opacity.
     */
    public static class TranslucentVertexConsumer implements VertexConsumer {
        private final VertexConsumer parent;
        private final float alphaOverride;

        public TranslucentVertexConsumer(VertexConsumer parent, float alphaOverride) {
            this.parent = parent;
            this.alphaOverride = alphaOverride;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            parent.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            parent.color(r, g, b, (int) (a * alphaOverride));
            return this;
        }

        @Override
        public VertexConsumer color(int argb) {
            int a = (argb >> 24) & 0xFF;
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            int newAlpha = (int) (a * alphaOverride);
            int newArgb = (newAlpha << 24) | (r << 16) | (g << 8) | b;
            parent.color(newArgb);
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            parent.texture(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            parent.overlay(u, v);
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            parent.light(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            parent.normal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(float red, float green, float blue, float alpha) {
            parent.color(red, green, blue, alpha * alphaOverride);
            return this;
        }

        @Override
        public VertexConsumer lineWidth(float width) {
            parent.lineWidth(width);
            return this;
        }
    }
}
