package net.holoshape;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HoloShapeClient implements ClientModInitializer {
    public static final String MOD_ID = "holoshape";

    // Quản lý trạng thái máy trạng thái (State Machine):
    // 0: Default (Mặc định)
    // 1: Point 1 Set (Đã chọn điểm 1, đang hiển thị Preview)
    // 2: Point 2 Finalized (Đã chốt Điểm 2, khóa hình dạng ảo cố định)
    private static int state = 0;
    private static BlockPos centerPos = null;
    private static BlockPos secondPos = null;
    private static double radius = 0.0;
    private static List<BlockPos> shapeOffsets = new ArrayList<>();
    private static double lastRadiusCalculated = -1.0;
    private static int lastHeightCalculated = -99999;
    private static ShapeType currentShape = ShapeType.CIRCLE;

    public static KeyBinding openGuiKey;

    public static ShapeType getCurrentShape() {
        return currentShape;
    }

    public static void setCurrentShape(ShapeType shape) {
        currentShape = shape;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            reset(client.player);
            client.player.sendMessage(Text.literal("§e[HoloShape] Đã đổi sang hình dạng: " + shape.getDisplayName()), false);
        }
    }

    @Override
    public void onInitializeClient() {
        // Đăng ký phím nóng mở GUI
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.holoshape.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KeyBinding.Category.MISC
        ));

        // Lắng nghe sự kiện tick để mở GUI khi bấm phím
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new HoloShapeScreen());
                }
            }
        });

        // Sự kiện Click chuột phải vào Block bất kỳ
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(Items.WOODEN_SHOVEL)) return ActionResult.PASS;

            // Sneak (Shift) + Right-click -> Reset
            if (player.isSneaking()) {
                reset(player);
                return ActionResult.SUCCESS;
            }

            BlockPos clickedPos = hitResult.getBlockPos();
            handleInteraction(player, clickedPos);
            return ActionResult.SUCCESS;
        });

        // Sự kiện Click chuột phải vào Không khí (để bắt lệnh Reset khi nhìn lên trời)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(Items.WOODEN_SHOVEL)) return ActionResult.PASS;

            // Sneak (Shift) + Right-click -> Reset
            if (player.isSneaking()) {
                reset(player);
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });

        // Sự kiện Render trong thế giới (World Rendering Callback)
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) return;

            // 1. Vẽ Điểm 1 bằng một khối viền màu Đỏ khi state >= 1
            if (state >= 1 && centerPos != null) {
                List<BlockPos> centerList = List.of(BlockPos.ORIGIN);
                CircleRenderer.render(context, centerPos, centerList, 1.0f, 0.0f, 0.0f, 0.8f);
            }

            // 2. Vẽ hình dạng ảo màu trắng mờ khi ở trạng thái Preview (State 1)
            if (state == 1 && centerPos != null) {
                HitResult hit = client.crosshairTarget;
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    BlockPos targetPos = ((BlockHitResult) hit).getBlockPos();
                    calculateOffsets(targetPos);
                }
                // Render đường viền ảo màu trắng mờ (Alpha = 0.4)
                CircleRenderer.render(context, centerPos, shapeOffsets, 1.0f, 1.0f, 1.0f, 0.4f);
            } 
            // 3. Vẽ hình dạng cố định màu Cyan khi đã chốt bán kính (State 2)
            else if (state == 2 && centerPos != null) {
                // Render đường viền ảo màu Cyan (Alpha = 0.6)
                CircleRenderer.render(context, centerPos, shapeOffsets, 0.0f, 1.0f, 1.0f, 0.6f);
            }
        });
    }

    private static void calculateOffsets(BlockPos targetPos) {
        if (centerPos == null || targetPos == null) return;

        switch (currentShape) {
            case CIRCLE: {
                double dx = targetPos.getX() - centerPos.getX();
                double dz = targetPos.getZ() - centerPos.getZ();
                double currentR = Math.sqrt(dx * dx + dz * dz);
                
                if (Math.abs(currentR - lastRadiusCalculated) > 0.001) {
                    radius = currentR;
                    shapeOffsets = ShapeMath.getCircleOffsets(currentR);
                    lastRadiusCalculated = currentR;
                }
                break;
            }
            case RECTANGLE:
                shapeOffsets = ShapeMath.getRectangleOffsets(centerPos, targetPos);
                break;
            case SPHERE: {
                double dxSph = targetPos.getX() - centerPos.getX();
                double dySph = targetPos.getY() - centerPos.getY();
                double dzSph = targetPos.getZ() - centerPos.getZ();
                double currentRSph = Math.sqrt(dxSph * dxSph + dySph * dySph + dzSph * dzSph);

                if (Math.abs(currentRSph - lastRadiusCalculated) > 0.001) {
                    radius = currentRSph;
                    shapeOffsets = ShapeMath.getSphereOffsets(currentRSph);
                    lastRadiusCalculated = currentRSph;
                }
                break;
            }
            case LINE:
                shapeOffsets = ShapeMath.getLineOffsets(centerPos, targetPos);
                break;
            case CUBOID:
                shapeOffsets = ShapeMath.getCuboidOffsets(centerPos, targetPos);
                break;
            case CYLINDER: {
                double dx = targetPos.getX() - centerPos.getX();
                double dz = targetPos.getZ() - centerPos.getZ();
                double currentR = Math.sqrt(dx * dx + dz * dz);
                int currentH = targetPos.getY() - centerPos.getY();

                if (Math.abs(currentR - lastRadiusCalculated) > 0.001 || currentH != lastHeightCalculated) {
                    radius = currentR;
                    shapeOffsets = ShapeMath.getCylinderOffsets(currentR, currentH);
                    lastRadiusCalculated = currentR;
                    lastHeightCalculated = currentH;
                }
                break;
            }
            case CONE: {
                double dx = targetPos.getX() - centerPos.getX();
                double dz = targetPos.getZ() - centerPos.getZ();
                double currentR = Math.sqrt(dx * dx + dz * dz);
                int currentH = targetPos.getY() - centerPos.getY();

                if (Math.abs(currentR - lastRadiusCalculated) > 0.001 || currentH != lastHeightCalculated) {
                    radius = currentR;
                    shapeOffsets = ShapeMath.getConeOffsets(currentR, currentH);
                    lastRadiusCalculated = currentR;
                    lastHeightCalculated = currentH;
                }
                break;
            }
            case PYRAMID: {
                int rx = targetPos.getX() - centerPos.getX();
                int ry = targetPos.getY() - centerPos.getY();
                int rz = targetPos.getZ() - centerPos.getZ();
                shapeOffsets = ShapeMath.getPyramidOffsets(rx, ry, rz);
                break;
            }
            case STAR: {
                double dx = targetPos.getX() - centerPos.getX();
                double dz = targetPos.getZ() - centerPos.getZ();
                double currentR = Math.sqrt(dx * dx + dz * dz);
                int currentH = targetPos.getY() - centerPos.getY();

                if (Math.abs(currentR - lastRadiusCalculated) > 0.001 || currentH != lastHeightCalculated) {
                    radius = currentR;
                    shapeOffsets = ShapeMath.getStarOffsets(currentR, currentH);
                    lastRadiusCalculated = currentR;
                    lastHeightCalculated = currentH;
                }
                break;
            }
            case HEART: {
                double dx = targetPos.getX() - centerPos.getX();
                double dz = targetPos.getZ() - centerPos.getZ();
                double currentR = Math.sqrt(dx * dx + dz * dz);
                int currentH = targetPos.getY() - centerPos.getY();

                if (Math.abs(currentR - lastRadiusCalculated) > 0.001 || currentH != lastHeightCalculated) {
                    radius = currentR;
                    shapeOffsets = ShapeMath.getHeartOffsets(currentR, currentH);
                    lastRadiusCalculated = currentR;
                    lastHeightCalculated = currentH;
                }
                break;
            }
            case TORUS: {
                double dx = targetPos.getX() - centerPos.getX();
                double dz = targetPos.getZ() - centerPos.getZ();
                double currentR = Math.sqrt(dx * dx + dz * dz);
                double minorR = Math.max(1.0, currentR * 0.25);

                if (Math.abs(currentR - lastRadiusCalculated) > 0.001) {
                    radius = currentR;
                    shapeOffsets = ShapeMath.getTorusOffsets(currentR, minorR);
                    lastRadiusCalculated = currentR;
                }
                break;
            }
            case HEXAGON: {
                double dx = targetPos.getX() - centerPos.getX();
                double dz = targetPos.getZ() - centerPos.getZ();
                double currentR = Math.sqrt(dx * dx + dz * dz);
                int currentH = targetPos.getY() - centerPos.getY();

                if (Math.abs(currentR - lastRadiusCalculated) > 0.001 || currentH != lastHeightCalculated) {
                    radius = currentR;
                    shapeOffsets = ShapeMath.getPolygonOffsets(currentR, currentH, 6);
                    lastRadiusCalculated = currentR;
                    lastHeightCalculated = currentH;
                }
                break;
            }
            case OCTAGON: {
                double dx = targetPos.getX() - centerPos.getX();
                double dz = targetPos.getZ() - centerPos.getZ();
                double currentR = Math.sqrt(dx * dx + dz * dz);
                int currentH = targetPos.getY() - centerPos.getY();

                if (Math.abs(currentR - lastRadiusCalculated) > 0.001 || currentH != lastHeightCalculated) {
                    radius = currentR;
                    shapeOffsets = ShapeMath.getPolygonOffsets(currentR, currentH, 8);
                    lastRadiusCalculated = currentR;
                    lastHeightCalculated = currentH;
                }
                break;
            }
        }
    }

    private static void handleInteraction(PlayerEntity player, BlockPos clickedPos) {
        if (state == 0) {
            state = 1;
            centerPos = clickedPos;
            secondPos = null;
            radius = 0.0;
            shapeOffsets.clear();
            lastRadiusCalculated = -1.0;
            player.sendMessage(Text.literal("§c[HoloShape] Đã đặt Điểm 1 tại: " + clickedPos.toShortString()), false);
        } else if (state == 1) {
            state = 2;
            secondPos = clickedPos;
            calculateOffsets(clickedPos);
            player.sendMessage(Text.literal("§3[HoloShape] Đã chốt hình dạng!"), false);
        }
    }

    public static void reset(PlayerEntity player) {
        state = 0;
        centerPos = null;
        secondPos = null;
        radius = 0.0;
        shapeOffsets.clear();
        lastRadiusCalculated = -1.0;
        lastHeightCalculated = -99999;
        player.sendMessage(Text.literal("§e[HoloShape] Đã reset trạng thái!"), false);
    }
}
