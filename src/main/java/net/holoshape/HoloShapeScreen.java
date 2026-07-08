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
            Text buttonText = Text.literal(prefix + shape.getDisplayName());

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

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}
