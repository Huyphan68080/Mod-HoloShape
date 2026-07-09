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
import net.minecraft.util.Identifier;
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
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Direction;

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
    private static BlockPos lastTargetPos = null;

    private static int renderMode = 0; 
    private static BlockState virtualBlockState = Blocks.RED_STAINED_GLASS.getDefaultState();
    private static boolean xrayEnabled = false;
    private static boolean animatedGuides = false;

    private static int length = 1; 
    private static int gridOffset = 0; 
    private static int orientationMode = 0; 
    
    private static int resolvedOrientation = 1;

    private static Direction clickedFace = Direction.UP;

    private static String mathExprX = "t * cos(t)";
    private static String mathExprY = "t";
    private static String mathExprZ = "t * sin(t)";
    private static double mathTMin = 0.0;
    private static double mathTMax = 20.0;
    private static double mathTStep = 0.1;

    public static KeyBinding openGuiKey;

    public static ShapeType getCurrentShape() { return currentShape; }
    public static void setCurrentShape(ShapeType shape) {
        currentShape = shape;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            reset(client.player);
            client.player.sendMessage(Text.literal("§e[HoloShape] Đã đổi sang hình dạng: " + shape.getDisplayName()), false);
        }
    }

    public static int getRenderMode() { return renderMode; }
    public static void setRenderMode(int m) { renderMode = m; }
    public static BlockState getVirtualBlockState() { return virtualBlockState; }
    public static void setVirtualBlockState(BlockState s) { virtualBlockState = s; }
    public static boolean isXrayEnabled() { return xrayEnabled; }
    public static void setXrayEnabled(boolean e) { xrayEnabled = e; }
    public static boolean isAnimatedGuides() { return animatedGuides; }
    public static void setAnimatedGuides(boolean a) { animatedGuides = a; }
    public static int getLength() { return length; }
    public static void setLength(int l) { length = l; }
    public static int getGridOffset() { return gridOffset; }
    public static void setGridOffset(int o) { gridOffset = o; }
    public static int getOrientationMode() { return orientationMode; }
    public static void setOrientationMode(int m) { orientationMode = m; }
    public static String getMathExprX() { return mathExprX; }
    public static void setMathExprX(String s) { mathExprX = s; }
    public static String getMathExprY() { return mathExprY; }
    public static void setMathExprY(String s) { mathExprY = s; }
    public static String getMathExprZ() { return mathExprZ; }
    public static void setMathExprZ(String s) { mathExprZ = s; }
    public static double getMathTMin() { return mathTMin; }
    public static void setMathTMin(double d) { mathTMin = d; }
    public static double getMathTMax() { return mathTMax; }
    public static void setMathTMax(double d) { mathTMax = d; }
    public static double getMathTStep() { return mathTStep; }
    public static void setMathTStep(double d) { mathTStep = d; }
    public static int getResolvedOrientation() { return resolvedOrientation; }
    public static int getState() { return state; }
    public static BlockPos getCenterPos() { return centerPos; }
    public static BlockPos getSecondPos() { return secondPos; }
    public static double getRadius() { return radius; }

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.holoshape.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KeyBinding.Category.create(Identifier.of("holoshape", "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new RadialMenuScreen());
                }
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(Items.WOODEN_SHOVEL)) return ActionResult.PASS;

            if (player.isSneaking()) {
                reset(player);
                return ActionResult.SUCCESS;
            }

            BlockPos clickedPos = hitResult.getBlockPos();
            clickedFace = hitResult.getSide();

            if (state == 0) {
                if (orientationMode == 0) {
                    float pitch = player.getPitch();
                    if (pitch > 55.0f || pitch < -55.0f) {
                        resolvedOrientation = 1;
                    } else {
                        Direction facing = player.getHorizontalFacing();
                        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                            resolvedOrientation = 3;
                        } else {
                            resolvedOrientation = 2;
                        }
                    }
                } else {
                    resolvedOrientation = orientationMode;
                }
            }

            BlockPos snappedPos = clickedPos;
            if (gridOffset == 1) {
                snappedPos = clickedPos.add(clickedFace.getVector());
            } else if (gridOffset == -1) {
                snappedPos = clickedPos.add(clickedFace.getOpposite().getVector());
            }

            handleInteraction(player, snappedPos);
            return ActionResult.SUCCESS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(Items.WOODEN_SHOVEL)) return ActionResult.PASS;

            if (player.isSneaking()) {
                reset(player);
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });

        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) return;

            if (state >= 1 && centerPos != null) {
                List<BlockPos> centerList = List.of(BlockPos.ORIGIN);
                CircleRenderer.renderShape(context, centerPos, centerList, 1.0f, 0.0f, 0.0f, 0.8f, 0, Blocks.RED_STAINED_GLASS.getDefaultState(), xrayEnabled, false);
            }

            if (state == 1 && centerPos != null) {
                HitResult hit = client.crosshairTarget;
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    BlockPos targetPos = ((BlockHitResult) hit).getBlockPos();
                    BlockPos snappedPos = targetPos;
                    if (gridOffset == 1) {
                        snappedPos = targetPos.add(((BlockHitResult) hit).getSide().getVector());
                    } else if (gridOffset == -1) {
                        snappedPos = targetPos.add(((BlockHitResult) hit).getSide().getOpposite().getVector());
                    }
                    calculateOffsets(snappedPos);
                }
                CircleRenderer.renderShape(context, centerPos, shapeOffsets, 1.0f, 1.0f, 1.0f, 0.4f, renderMode, virtualBlockState, xrayEnabled, animatedGuides);
            } 
            else if (state == 2 && centerPos != null) {
                CircleRenderer.renderShape(context, centerPos, shapeOffsets, 0.0f, 1.0f, 1.0f, 0.6f, renderMode, virtualBlockState, xrayEnabled, animatedGuides);
            }
        });
    }

    private static void calculateOffsets(BlockPos targetPos) {
        if (centerPos == null || targetPos == null) return;
        if (targetPos.equals(lastTargetPos)) return;
        lastTargetPos = targetPos;

        int orient = resolvedOrientation;
        int dx = targetPos.getX() - centerPos.getX();
        int dy = targetPos.getY() - centerPos.getY();
        int dz = targetPos.getZ() - centerPos.getZ();

        int du = 0, dv = 0, dp = 0;
        if (orient == 1) { 
            du = dx; dv = dz; dp = dy;
        } else if (orient == 2) { 
            du = dz; dv = dy; dp = dx;
        } else if (orient == 3) { 
            du = dx; dv = dy; dp = dz;
        }

        double localR = Math.sqrt(du * du + dv * dv);
        List<BlockPos> rawOffsets = new ArrayList<>();

        switch (currentShape) {
            case CIRCLE:
                rawOffsets = ShapeMath.getCircleOffsets(localR);
                break;
            case RECTANGLE: {
                int minU = Math.min(0, du);
                int maxU = Math.max(0, du);
                int minV = Math.min(0, dv);
                int maxV = Math.max(0, dv);
                for (int u = minU; u <= maxU; u++) {
                    rawOffsets.add(new BlockPos(u, 0, minV));
                    if (minV != maxV) rawOffsets.add(new BlockPos(u, 0, maxV));
                }
                for (int v = minV + 1; v < maxV; v++) {
                    rawOffsets.add(new BlockPos(minU, 0, v));
                    if (minU != maxU) rawOffsets.add(new BlockPos(maxU, 0, v));
                }
                break;
            }
            case SPHERE:
                rawOffsets = ShapeMath.getSphereOffsets(localR);
                break;
            case LINE:
                rawOffsets = ShapeMath.getLineOffsets(BlockPos.ORIGIN, new BlockPos(dx, dy, dz));
                break;
            case CUBOID:
                rawOffsets = ShapeMath.getCuboidOffsets(BlockPos.ORIGIN, new BlockPos(dx, dy, dz));
                break;
            case CYLINDER:
                rawOffsets = ShapeMath.getCircleOffsets(localR);
                break;
            case CONE: {
                int len = (length > 1) ? length : Math.max(1, Math.abs(dp));
                rawOffsets = ShapeMath.getConeOffsets(localR, len);
                break;
            }
            case PYRAMID: {
                int len = (length > 1) ? length : Math.max(1, Math.abs(dp));
                rawOffsets = ShapeMath.getPyramidOffsets(Math.abs(du), len, Math.abs(dv));
                break;
            }
            case STAR:
                rawOffsets = ShapeMath.getStarOffsets(localR, 0);
                break;
            case HEART:
                rawOffsets = ShapeMath.getHeartOffsets(localR, 0);
                break;
            case TORUS:
                rawOffsets = ShapeMath.getTorusOffsets(localR, Math.max(1.0, localR * 0.25));
                break;
            case HEXAGON:
                rawOffsets = ShapeMath.getPolygonOffsets(localR, 0, 6);
                break;
            case OCTAGON:
                rawOffsets = ShapeMath.getPolygonOffsets(localR, 0, 8);
                break;
            case ARCH: {
                double R = Math.max(1.0, Math.abs(du));
                int leg = Math.max(0, Math.abs(dv) - (int)Math.round(R));
                rawOffsets = ShapeMath.getArchOffsets(R, leg);
                break;
            }
            case HORSESHOE: {
                double R = Math.max(1.0, Math.abs(du));
                int leg = Math.max(0, Math.abs(dv) - (int)Math.round(R));
                rawOffsets = ShapeMath.getHorseshoeOffsets(R, leg);
                break;
            }
            case MATH:
                rawOffsets = ShapeMath.getMathOffsets(mathExprX, mathExprY, mathExprZ, mathTMin, mathTMax, mathTStep);
                break;
        }

        shapeOffsets = mapAndExtrude(rawOffsets, du, dv, dp);
    }

    private static List<BlockPos> mapAndExtrude(List<BlockPos> raw, int du, int dv, int dp) {
        List<BlockPos> mapped = new ArrayList<>();
        int orient = resolvedOrientation;
        
        boolean applyMapping = currentShape != ShapeType.LINE && 
                              currentShape != ShapeType.CUBOID && 
                              currentShape != ShapeType.MATH;
        
        for (BlockPos p : raw) {
            if (applyMapping) {
                int x = p.getX();
                int y = p.getY();
                int z = p.getZ();
                if (orient == 1) { 
                    mapped.add(new BlockPos(x, y, z));
                } else if (orient == 2) { 
                    mapped.add(new BlockPos(y, z, x));
                } else if (orient == 3) { 
                    mapped.add(new BlockPos(x, z, y));
                }
            } else {
                mapped.add(p);
            }
        }
        
        boolean shouldExtrude = currentShape == ShapeType.CIRCLE ||
                                currentShape == ShapeType.RECTANGLE ||
                                currentShape == ShapeType.STAR ||
                                currentShape == ShapeType.HEART ||
                                currentShape == ShapeType.HEXAGON ||
                                currentShape == ShapeType.OCTAGON ||
                                currentShape == ShapeType.ARCH ||
                                currentShape == ShapeType.HORSESHOE ||
                                currentShape == ShapeType.CYLINDER;
                                
        if (shouldExtrude && applyMapping) {
            int len = (length > 1) ? length : (currentShape == ShapeType.CYLINDER ? Math.max(1, Math.abs(dp)) : 1);
            if (len > 1) {
                BlockPos dirVec = BlockPos.ORIGIN;
                if (orient == 1) {
                    dirVec = new BlockPos(0, dp >= 0 ? 1 : -1, 0);
                } else if (orient == 2) {
                    dirVec = new BlockPos(dp >= 0 ? 1 : -1, 0, 0);
                } else if (orient == 3) {
                    dirVec = new BlockPos(0, 0, dp >= 0 ? 1 : -1);
                }
                mapped = ShapeMath.extrudeOffsets(mapped, len, dirVec);
            }
        }
        
        return mapped;
    }

    private static void handleInteraction(PlayerEntity player, BlockPos clickedPos) {
        if (state == 0) {
            state = 1;
            centerPos = clickedPos;
            secondPos = null;
            radius = 0.0;
            shapeOffsets.clear();
            lastRadiusCalculated = -1.0;
            lastTargetPos = null;
            player.sendMessage(Text.translatable("chat.holoshape.set_point1", clickedPos.toShortString()), false);
        } else if (state == 1) {
            state = 2;
            secondPos = clickedPos;
            calculateOffsets(clickedPos);
            player.sendMessage(Text.translatable("chat.holoshape.finalized"), false);
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
        lastTargetPos = null;
        player.sendMessage(Text.translatable("chat.holoshape.reset"), false);
    }

    public static String exportShareCode() {
        if (centerPos == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("shape=").append(currentShape.name()).append(";");
        sb.append("center=").append(centerPos.getX()).append(",").append(centerPos.getY()).append(",").append(centerPos.getZ()).append(";");
        if (secondPos != null) {
            sb.append("second=").append(secondPos.getX()).append(",").append(secondPos.getY()).append(",").append(secondPos.getZ()).append(";");
        }
        sb.append("radius=").append(radius).append(";");
        sb.append("renderMode=").append(renderMode).append(";");
        sb.append("block=").append(Registries.BLOCK.getId(virtualBlockState.getBlock()).toString()).append(";");
        sb.append("xray=").append(xrayEnabled).append(";");
        sb.append("guides=").append(animatedGuides).append(";");
        sb.append("length=").append(length).append(";");
        sb.append("offset=").append(gridOffset).append(";");
        sb.append("orient=").append(orientationMode).append(";");
        sb.append("mathX=").append(mathExprX).append(";");
        sb.append("mathY=").append(mathExprY).append(";");
        sb.append("mathZ=").append(mathExprZ).append(";");
        sb.append("tmin=").append(mathTMin).append(";");
        sb.append("tmax=").append(mathTMax).append(";");
        sb.append("tstep=").append(mathTStep);
        
        try {
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            return "HoloShapev1:" + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean importShareCode(String code) {
        if (code == null || !code.startsWith("HoloShapev1:")) return false;
        try {
            String base64 = code.substring("HoloShapev1:".length()).trim();
            byte[] bytes = Base64.getDecoder().decode(base64);
            String decompressed = new String(bytes, StandardCharsets.UTF_8);
            
            String[] parts = decompressed.split(";");
            ShapeType newShape = null;
            BlockPos newCenter = null;
            BlockPos newSecond = null;
            double newRadius = 0.0;
            int newRenderMode = 0;
            BlockState newBlock = null;
            boolean newXray = false;
            boolean newGuides = false;
            int newLength = 1;
            int newOffset = 0;
            int newOrient = 0;
            String newMathX = "t*cos(t)", newMathY = "t", newMathZ = "t*sin(t)";
            double newTMin = 0.0, newTMax = 20.0, newTStep = 0.1;
            
            for (String part : parts) {
                String[] kv = part.split("=", 2);
                if (kv.length < 2) continue;
                String key = kv[0].trim();
                String val = kv[1].trim();
                
                switch (key) {
                    case "shape":
                        newShape = ShapeType.valueOf(val);
                        break;
                    case "center": {
                        String[] xyz = val.split(",");
                        newCenter = new BlockPos(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
                        break;
                    }
                    case "second": {
                        String[] xyz = val.split(",");
                        newSecond = new BlockPos(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
                        break;
                    }
                    case "radius":
                        newRadius = Double.parseDouble(val);
                        break;
                    case "renderMode":
                        newRenderMode = Integer.parseInt(val);
                        break;
                    case "block": {
                        Identifier id = Identifier.of(val);
                        newBlock = Registries.BLOCK.get(id).getDefaultState();
                        break;
                    }
                    case "xray":
                        newXray = Boolean.parseBoolean(val);
                        break;
                    case "guides":
                        newGuides = Boolean.parseBoolean(val);
                        break;
                    case "length":
                        newLength = Integer.parseInt(val);
                        break;
                    case "offset":
                        newOffset = Integer.parseInt(val);
                        break;
                    case "orient":
                        newOrient = Integer.parseInt(val);
                        break;
                    case "mathX":
                        newMathX = val;
                        break;
                    case "mathY":
                        newMathY = val;
                        break;
                    case "mathZ":
                        newMathZ = val;
                        break;
                    case "tmin":
                        newTMin = Double.parseDouble(val);
                        break;
                    case "tmax":
                        newTMax = Double.parseDouble(val);
                        break;
                    case "tstep":
                        newTStep = Double.parseDouble(val);
                        break;
                }
            }
            
            if (newShape == null || newCenter == null) return false;
            
            currentShape = newShape;
            centerPos = newCenter;
            secondPos = newSecond;
            radius = newRadius;
            renderMode = newRenderMode;
            if (newBlock != null) virtualBlockState = newBlock;
            xrayEnabled = newXray;
            animatedGuides = newGuides;
            length = newLength;
            gridOffset = newOffset;
            orientationMode = newOrient;
            mathExprX = newMathX;
            mathExprY = newMathY;
            mathExprZ = newMathZ;
            mathTMin = newTMin;
            mathTMax = newTMax;
            mathTStep = newTStep;
            
            if (orientationMode == 0) {
                resolvedOrientation = 1;
            } else {
                resolvedOrientation = orientationMode;
            }
            
            state = (secondPos != null) ? 2 : 1;
            lastRadiusCalculated = -1.0;
            lastHeightCalculated = -99999;
            lastTargetPos = null;
            
            if (secondPos != null) {
                calculateOffsets(secondPos);
            } else {
                calculateOffsets(centerPos);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
