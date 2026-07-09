package net.holoshape;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class HoloShapeScreen extends Screen {

    private TextFieldWidget shareCodeField;
    
    // Math fields
    private TextFieldWidget mathXField;
    private TextFieldWidget mathYField;
    private TextFieldWidget mathZField;
    private TextFieldWidget mathMinField;
    private TextFieldWidget mathMaxField;
    private TextFieldWidget mathStepField;

    private static final BlockState[] BLOCKS = {
        Blocks.RED_STAINED_GLASS.getDefaultState(),
        Blocks.GLASS.getDefaultState(),
        Blocks.QUARTZ_BLOCK.getDefaultState(),
        Blocks.SMOOTH_STONE.getDefaultState(),
        Blocks.GLOWSTONE.getDefaultState(),
        Blocks.EMERALD_BLOCK.getDefaultState(),
        Blocks.DIAMOND_BLOCK.getDefaultState(),
        Blocks.OAK_PLANKS.getDefaultState()
    };

    private static final String[] BLOCK_NAMES = {
        "Red Glass", "Glass", "Quartz", "Stone", "Glowstone", "Emerald", "Diamond", "Oak Planks"
    };

    public HoloShapeScreen() {
        super(Text.translatable("gui.holoshape.title"));
    }

    @Override
    protected void init() {
        // Layout calculations
        int shapeColWidth = 85;
        int shapeSpacing = 4;
        int shapeColumns = 2;
        
        int leftPaneWidth = shapeColWidth * shapeColumns + shapeSpacing;
        int rightPaneWidth = 180;
        int paneSpacing = 40;
        
        int totalWidth = leftPaneWidth + rightPaneWidth + paneSpacing;
        int totalHeight = 190;
        
        int startX = this.width / 2 - totalWidth / 2;
        int startY = this.height / 2 - totalHeight / 2 + 10;

        // --- LEFT PANE: SHAPES LIST (2 columns of 8 buttons) ---
        ShapeType[] shapes = ShapeType.values();
        int shapesPerCol = (shapes.length + 1) / 2;
        
        for (int i = 0; i < shapes.length; i++) {
            ShapeType shape = shapes[i];
            boolean isCurrent = HoloShapeClient.getCurrentShape() == shape;
            String prefix = isCurrent ? "➔ " : "";
            Text buttonText = prefix.isEmpty() ? shape.getDisplayName() : Text.literal(prefix).append(shape.getDisplayName());

            int col = i / shapesPerCol;
            int row = i % shapesPerCol;
            int x = startX + col * (shapeColWidth + shapeSpacing);
            int y = startY + row * 22;

            this.addDrawableChild(ButtonWidget.builder(buttonText, button -> {
                HoloShapeClient.setCurrentShape(shape);
                // Re-initialize GUI to update active tabs/fields
                this.clearAndInit();
            })
            .dimensions(x, y, shapeColWidth, 20)
            .build());
        }

        // --- RIGHT PANE: SETTINGS & TOOLS ---
        int rightX = startX + leftPaneWidth + paneSpacing;
        int currentY = startY;

        // 1. Render Mode
        String modeText = HoloShapeClient.getRenderMode() == 0 ? "Mode: Lines" : "Mode: Solid";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(modeText), button -> {
            HoloShapeClient.setRenderMode(1 - HoloShapeClient.getRenderMode());
            button.setMessage(Text.literal(HoloShapeClient.getRenderMode() == 0 ? "Mode: Lines" : "Mode: Solid"));
        })
        .dimensions(rightX, currentY, 88, 20)
        .build());

        // 2. Block Type
        int blockIdx = getBlockIndex(HoloShapeClient.getVirtualBlockState());
        String blockName = BLOCK_NAMES[blockIdx];
        this.addDrawableChild(ButtonWidget.builder(Text.literal(blockName), button -> {
            int nextIdx = (getBlockIndex(HoloShapeClient.getVirtualBlockState()) + 1) % BLOCKS.length;
            HoloShapeClient.setVirtualBlockState(BLOCKS[nextIdx]);
            button.setMessage(Text.literal(BLOCK_NAMES[nextIdx]));
        })
        .dimensions(rightX + 92, currentY, 88, 20)
        .build());

        currentY += 24;

        // 3. X-Ray Toggle
        String xrayText = HoloShapeClient.isXrayEnabled() ? "X-Ray: ON" : "X-Ray: OFF";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(xrayText), button -> {
            HoloShapeClient.setXrayEnabled(!HoloShapeClient.isXrayEnabled());
            button.setMessage(Text.literal(HoloShapeClient.isXrayEnabled() ? "X-Ray: ON" : "X-Ray: OFF"));
        })
        .dimensions(rightX, currentY, 88, 20)
        .build());

        // 4. Pulse Animation Toggle
        String pulseText = HoloShapeClient.isAnimatedGuides() ? "Pulse: ON" : "Pulse: OFF";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(pulseText), button -> {
            HoloShapeClient.setAnimatedGuides(!HoloShapeClient.isAnimatedGuides());
            button.setMessage(Text.literal(HoloShapeClient.isAnimatedGuides() ? "Pulse: ON" : "Pulse: OFF"));
        })
        .dimensions(rightX + 92, currentY, 88, 20)
        .build());

        currentY += 24;

        // 5. Orientation Mode
        String[] orientNames = {"Orient: Auto", "Orient: X-Z", "Orient: Y-Z", "Orient: X-Y"};
        this.addDrawableChild(ButtonWidget.builder(Text.literal(orientNames[HoloShapeClient.getOrientationMode()]), button -> {
            int next = (HoloShapeClient.getOrientationMode() + 1) % 4;
            HoloShapeClient.setOrientationMode(next);
            button.setMessage(Text.literal(orientNames[next]));
        })
        .dimensions(rightX, currentY, 88, 20)
        .build());

        // 6. Grid Offset Snapping
        String[] offsetNames = {"Offset: Flush", "Offset: Inside", "Offset: Outside"};
        int offsetVal = HoloShapeClient.getGridOffset() == 0 ? 0 : (HoloShapeClient.getGridOffset() == -1 ? 1 : 2);
        this.addDrawableChild(ButtonWidget.builder(Text.literal(offsetNames[offsetVal]), button -> {
            int nextVal = (HoloShapeClient.getGridOffset() == 0 ? -1 : (HoloShapeClient.getGridOffset() == -1 ? 1 : 0));
            HoloShapeClient.setGridOffset(nextVal);
            int nextOffsetIdx = nextVal == 0 ? 0 : (nextVal == -1 ? 1 : 2);
            button.setMessage(Text.literal(offsetNames[nextOffsetIdx]));
        })
        .dimensions(rightX + 92, currentY, 88, 20)
        .build());

        currentY += 24;

        // 7. Length Adjustment
        this.addDrawableChild(ButtonWidget.builder(Text.literal("-"), button -> {
            HoloShapeClient.setLength(Math.max(1, HoloShapeClient.getLength() - 1));
            this.clearAndInit();
        })
        .dimensions(rightX, currentY, 20, 20)
        .build());

        String lenLabel = "Length: " + HoloShapeClient.getLength();
        this.addDrawableChild(ButtonWidget.builder(Text.literal(lenLabel), button -> {})
        .dimensions(rightX + 22, currentY, 136, 20)
        .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> {
            HoloShapeClient.setLength(Math.min(100, HoloShapeClient.getLength() + 1));
            this.clearAndInit();
        })
        .dimensions(rightX + 160, currentY, 20, 20)
        .build());

        currentY += 24;

        // 8. Math Options OR Share Code UI
        if (HoloShapeClient.getCurrentShape() == ShapeType.MATH) {
            // Mini title
            // Text fields for equation X, Y, Z
            mathXField = new TextFieldWidget(textRenderer, rightX, currentY, 58, 16, Text.literal("X(t)"));
            mathXField.setMaxLength(64);
            mathXField.setText(HoloShapeClient.getMathExprX());
            mathXField.setChangedListener(HoloShapeClient::setMathExprX);
            this.addDrawableChild(mathXField);

            mathYField = new TextFieldWidget(textRenderer, rightX + 61, currentY, 58, 16, Text.literal("Y(t)"));
            mathYField.setMaxLength(64);
            mathYField.setText(HoloShapeClient.getMathExprY());
            mathYField.setChangedListener(HoloShapeClient::setMathExprY);
            this.addDrawableChild(mathYField);

            mathZField = new TextFieldWidget(textRenderer, rightX + 122, currentY, 58, 16, Text.literal("Z(t)"));
            mathZField.setMaxLength(64);
            mathZField.setText(HoloShapeClient.getMathExprZ());
            mathZField.setChangedListener(HoloShapeClient::setMathExprZ);
            this.addDrawableChild(mathZField);

            currentY += 20;

            // Math range parameters: TMin, TMax, TStep
            mathMinField = new TextFieldWidget(textRenderer, rightX, currentY, 58, 16, Text.literal("TMin"));
            mathMinField.setText(String.valueOf(HoloShapeClient.getMathTMin()));
            mathMinField.setChangedListener(s -> {
                try { HoloShapeClient.setMathTMin(Double.parseDouble(s)); } catch (Exception e) {}
            });
            this.addDrawableChild(mathMinField);

            mathMaxField = new TextFieldWidget(textRenderer, rightX + 61, currentY, 58, 16, Text.literal("TMax"));
            mathMaxField.setText(String.valueOf(HoloShapeClient.getMathTMax()));
            mathMaxField.setChangedListener(s -> {
                try { HoloShapeClient.setMathTMax(Double.parseDouble(s)); } catch (Exception e) {}
            });
            this.addDrawableChild(mathMaxField);

            mathStepField = new TextFieldWidget(textRenderer, rightX + 122, currentY, 58, 16, Text.literal("TStep"));
            mathStepField.setText(String.valueOf(HoloShapeClient.getMathTStep()));
            mathStepField.setChangedListener(s -> {
                try { HoloShapeClient.setMathTStep(Double.parseDouble(s)); } catch (Exception e) {}
            });
            this.addDrawableChild(mathStepField);

            currentY += 22;
        }

        // Share Code input text field
        shareCodeField = new TextFieldWidget(textRenderer, rightX, currentY, 180, 16, Text.literal("Share Code"));
        shareCodeField.setMaxLength(512);
        this.addDrawableChild(shareCodeField);

        currentY += 20;

        // Import / Export buttons
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Export"), button -> {
            String code = HoloShapeClient.exportShareCode();
            shareCodeField.setText(code);
            if (client != null && client.keyboard != null) {
                client.keyboard.setClipboard(code);
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§a[HoloShape] Đã sao chép mã chia sẻ vào Clipboard!"), false);
                }
            }
        })
        .dimensions(rightX, currentY, 88, 20)
        .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Import"), button -> {
            String code = shareCodeField.getText();
            if (HoloShapeClient.importShareCode(code)) {
                this.close();
                if (client != null && client.player != null) {
                    client.player.sendMessage(Text.literal("§a[HoloShape] Đã tải trạng thái từ mã chia sẻ thành công!"), false);
                }
            } else {
                shareCodeField.setText("MÃ KHÔNG HỢP LỆ!");
            }
        })
        .dimensions(rightX + 92, currentY, 88, 20)
        .build());
    }

    private int getBlockIndex(BlockState state) {
        for (int i = 0; i < BLOCKS.length; i++) {
            if (BLOCKS[i].getBlock() == state.getBlock()) return i;
        }
        return 0;
    }

    private void drawMinecraftPanel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, 0xFFC6C6C6);

        context.fill(x, y, x + width, y + 1, 0xFF000000);
        context.fill(x, y + height - 1, x + width, y + height, 0xFF000000);
        context.fill(x, y, x + 1, y + height, 0xFF000000);
        context.fill(x + width - 1, y, x + width, y + height, 0xFF000000);

        context.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFFFFFFFF);
        context.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFFFFFFFF);

        context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF555555);
        context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF555555);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Do not call super.renderBackground(context, mouseX, mouseY, delta) to keep background transparent!

        int shapeColWidth = 85;
        int shapeSpacing = 4;
        int shapeColumns = 2;
        int leftPaneWidth = shapeColWidth * shapeColumns + shapeSpacing;
        int rightPaneWidth = 180;
        int paneSpacing = 40;
        int totalWidth = leftPaneWidth + rightPaneWidth + paneSpacing;
        int totalHeight = 190;

        int panelWidth = totalWidth + 32;
        int panelHeight = totalHeight + 48;
        int panelX = this.width / 2 - panelWidth / 2;
        int panelY = this.height / 2 - panelHeight / 2;

        drawMinecraftPanel(context, panelX, panelY, panelWidth, panelHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int shapeColWidth = 85;
        int shapeSpacing = 4;
        int shapeColumns = 2;
        int leftPaneWidth = shapeColWidth * shapeColumns + shapeSpacing;
        int rightPaneWidth = 180;
        int paneSpacing = 40;
        int totalWidth = leftPaneWidth + rightPaneWidth + paneSpacing;
        int totalHeight = 190;

        int panelHeight = totalHeight + 48;
        int panelY = this.height / 2 - panelHeight / 2;

        context.drawText(this.textRenderer, this.title, this.width / 2 - this.textRenderer.getWidth(this.title) / 2, panelY + 10, 0x404040, false);

        Text creText = Text.literal("Cre: HuyPhan(Sun)");
        context.drawText(this.textRenderer, creText, this.width / 2 - this.textRenderer.getWidth(creText) / 2, panelY + panelHeight - 16, 0x606060, false);

        // Draw the red X button in the center
        int cx = this.width / 2;
        int cy = this.height / 2;
        boolean hoveredX = mouseX >= cx - 12 && mouseX < cx + 12 && mouseY >= cy - 12 && mouseY < cy + 12;

        int xColor = hoveredX ? 0xFFFF3333 : 0xFFDDDDDD;
        int bgCenterColor = hoveredX ? 0x80FF2222 : 0x60000000;

        fillPixelCircle(context, cx, cy, 11, bgCenterColor);
        drawPixelCircle(context, cx, cy, 11, hoveredX ? 0xFFFF3333 : 0x80FFFFFF);

        drawPixelLine(context, cx - 3, cy - 3, cx + 3, cy + 3, xColor);
        drawPixelLine(context, cx - 3, cy + 3, cx + 3, cy - 3, xColor);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int cx = this.width / 2;
        int cy = this.height / 2;
        if (mouseX >= cx - 12 && mouseX < cx + 12 && mouseY >= cy - 12 && mouseY < cy + 12) {
            if (client != null && client.player != null) {
                HoloShapeClient.reset(client.player);
            }
            this.close();
            return true;
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseReleased(Click click) {
        double mouseX = click.x();
        double mouseY = click.y();
        int cx = this.width / 2;
        int cy = this.height / 2;
        if (mouseX >= cx - 12 && mouseX < cx + 12 && mouseY >= cy - 12 && mouseY < cy + 12) {
            if (client != null && client.player != null) {
                HoloShapeClient.reset(client.player);
            }
            this.close();
            return true;
        }
        return super.mouseReleased(click);
    }

    private void drawPixel(DrawContext context, int x, int y, int size, int color) {
        context.fill(x, y, x + size, y + size, color);
    }

    private void drawPixelLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            drawPixel(context, x1, y1, 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void fillPixelCircle(DrawContext context, int xc, int yc, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            int width = (int) Math.sqrt(radius * radius - y * y);
            context.fill(xc - width, yc + y, xc + width + 1, yc + y + 1, color);
        }
    }

    private void drawPixelCircle(DrawContext context, int xc, int yc, int radius, int color) {
        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;
        drawCirclePixels(context, xc, yc, x, y, color);
        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            drawCirclePixels(context, xc, yc, x, y, color);
        }
    }

    private void drawCirclePixels(DrawContext context, int xc, int yc, int x, int y, int color) {
        drawPixel(context, xc + x, yc + y, 1, color);
        drawPixel(context, xc - x, yc + y, 1, color);
        drawPixel(context, xc + x, yc - y, 1, color);
        drawPixel(context, xc - x, yc - y, 1, color);
        drawPixel(context, xc + y, yc + x, 1, color);
        drawPixel(context, xc - y, yc + x, 1, color);
        drawPixel(context, xc + y, yc - x, 1, color);
        drawPixel(context, xc - y, yc - x, 1, color);
    }
}
