package net.holoshape;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HoloShapeScreen extends Screen {

    public HoloShapeScreen() {
        super(Text.translatable("gui.holoshape.title"));
    }

    @Override
    protected void init() {
        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 8;
        int columns = 2;
        int buttonsPerCol = (ShapeType.values().length + columns - 1) / columns;
        int totalHeight = buttonsPerCol * buttonHeight + (buttonsPerCol - 1) * spacing;
        int startY = this.height / 2 - totalHeight / 2;
        int startX = this.width / 2 - (buttonWidth * columns + spacing * (columns - 1)) / 2;

        int index = 0;
        for (ShapeType shape : ShapeType.values()) {
            boolean isCurrent = HoloShapeClient.getCurrentShape() == shape;
            String prefix = isCurrent ? "➔ " : "";
            Text buttonText = Text.literal(prefix).append(shape.getDisplayName());

            int col = index / buttonsPerCol;
            int row = index % buttonsPerCol;
            int x = startX + col * (buttonWidth + spacing);
            int y = startY + row * (buttonHeight + spacing);

            this.addDrawableChild(ButtonWidget.builder(buttonText, button -> {
                HoloShapeClient.setCurrentShape(shape);
                this.close();
            })
            .dimensions(x, y, buttonWidth, buttonHeight)
            .build());

            index++;
        }
    }

    private void drawMinecraftPanel(DrawContext context, int x, int y, int width, int height) {
        // 1. Nền xám nhạt (Light gray background of Minecraft GUI)
        context.fill(x, y, x + width, y + height, 0xFFC6C6C6);

        // 2. Viền ngoài màu đen (Black outer border)
        context.fill(x, y, x + width, y + 1, 0xFF000000);
        context.fill(x, y + height - 1, x + width, y + height, 0xFF000000);
        context.fill(x, y, x + 1, y + height, 0xFF000000);
        context.fill(x + width - 1, y, x + width, y + height, 0xFF000000);

        // 3. Viền nổi 3D màu trắng (Top and Left inner border)
        context.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFFFFFFFF);
        context.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFFFFFFFF);

        // 4. Viền chìm 3D màu xám đậm (Bottom and Right inner border)
        context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF555555);
        context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF555555);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 8;
        int columns = 2;
        int buttonsPerCol = (ShapeType.values().length + columns - 1) / columns;
        int totalHeight = buttonsPerCol * buttonHeight + (buttonsPerCol - 1) * spacing;

        int panelWidth = buttonWidth * columns + spacing * (columns - 1) + 32;
        int panelHeight = totalHeight + 48;
        int panelX = this.width / 2 - panelWidth / 2;
        int panelY = this.height / 2 - panelHeight / 2;

        drawMinecraftPanel(context, panelX, panelY, panelWidth, panelHeight);

        // Vẽ tiêu đề dạng chữ tối xám không đổ bóng (phong cách tủ đồ Minecraft)
        context.drawText(this.textRenderer, this.title, this.width / 2 - this.textRenderer.getWidth(this.title) / 2, panelY + 12, 0x404040, false);

        // Vẽ chữ Credit dạng xám nhạt hơn (phong cách phụ đề tủ đồ)
        Text creText = Text.literal("Cre: HuyPhan(Sun)");
        context.drawText(this.textRenderer, creText, this.width / 2 - this.textRenderer.getWidth(creText) / 2, panelY + panelHeight - 16, 0x606060, false);

        super.render(context, mouseX, mouseY, delta);
    }
}
