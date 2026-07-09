package net.holoshape;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RadialMenuScreen extends Screen {
    private double startMouseX = -999;
    private double startMouseY = -999;
    private final long openTime;
    
    private static final ShapeType[] SHAPES = ShapeType.values();
    
    public RadialMenuScreen() {
        super(Text.literal("HoloShape Radial Menu"));
        this.openTime = System.currentTimeMillis();
    }
    
    @Override
    protected void init() {
        super.init();
        if (startMouseX == -999) {
            startMouseX = client.mouse.getX() * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
            startMouseY = client.mouse.getY() * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (client == null || client.getWindow() == null) return;
        
        if (!InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_H)) {
            long elapsed = System.currentTimeMillis() - this.openTime;
            double currentMouseX = client.mouse.getX() * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
            double currentMouseY = client.mouse.getY() * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
            
            double dx = currentMouseX - startMouseX;
            double dy = currentMouseY - startMouseY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            this.close();
            if (elapsed < 200 && dist <= 12) {
                client.setScreen(new HoloShapeScreen());
            } else {
                int hoveredIndex = getHoveredSector(currentMouseX, currentMouseY);
                if (hoveredIndex >= 0 && hoveredIndex < SHAPES.length) {
                    HoloShapeClient.setCurrentShape(SHAPES[hoveredIndex]);
                } else if (hoveredIndex == -1) {
                    if (client.player != null) {
                        HoloShapeClient.reset(client.player);
                    }
                }
            }
        }
    }
    
    private int getHoveredSector(double mouseX, double mouseY) {
        double dx = mouseX - startMouseX;
        double dy = mouseY - startMouseY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 22) {
            return -1;
        }
        
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) {
            angle += 360;
        }
        
        int numSectors = SHAPES.length;
        double sectorSize = 360.0 / numSectors;
        double adjustedAngle = (angle - 270.0 + 360.0 + sectorSize / 2.0) % 360.0;
        
        return (int) (adjustedAngle / sectorSize) % numSectors;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Keeping background transparent by not calling super.renderBackground or context.fill screen overlay
        
        double currentMouseX = client.mouse.getX() * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
        double currentMouseY = client.mouse.getY() * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
        
        int hovered = getHoveredSector(currentMouseX, currentMouseY);
        int numSectors = SHAPES.length;
        
        // Draw Sector Circles (Inner radius 22, Outer radius 62)
        drawSectorCircle(context, (int)startMouseX, (int)startMouseY, 22, hovered, 0x40FFFFFF, 0xFF55FFFF);
        drawSectorCircle(context, (int)startMouseX, (int)startMouseY, 62, hovered, 0x40FFFFFF, 0xFF55FFFF);
        
        // Draw 21 Sector boundary lines
        double innerRadius = 22.0;
        double outerRadius = 62.0;
        for (int i = 0; i < numSectors; i++) {
            double angleDeg = 270.0 + (i - 0.5) * (360.0 / numSectors);
            double angleRad = Math.toRadians(angleDeg);
            int x1 = (int) Math.round(startMouseX + innerRadius * Math.cos(angleRad));
            int y1 = (int) Math.round(startMouseY + innerRadius * Math.sin(angleRad));
            int x2 = (int) Math.round(startMouseX + outerRadius * Math.cos(angleRad));
            int y2 = (int) Math.round(startMouseY + outerRadius * Math.sin(angleRad));
            
            boolean isBoundaryHighlighted = (hovered == i || hovered == (i - 1 + numSectors) % numSectors);
            int lineColor = isBoundaryHighlighted ? 0xFF55FFFF : 0x20FFFFFF;
            
            drawPixelLine(context, x1, y1, x2, y2, lineColor);
        }
        
        // Draw Pointer Line to Cursor (only outside the center X button)
        double distToCursor = Math.sqrt((currentMouseX - startMouseX) * (currentMouseX - startMouseX) + (currentMouseY - startMouseY) * (currentMouseY - startMouseY));
        if (distToCursor > 22) {
            double angle = Math.atan2(currentMouseY - startMouseY, currentMouseX - startMouseX);
            int startLineX = (int) (startMouseX + 22.0 * Math.cos(angle));
            int startLineY = (int) (startMouseY + 22.0 * Math.sin(angle));
            drawPixelLine(context, startLineX, startLineY, (int)currentMouseX, (int)currentMouseY, 0x8055FFFF);
        }
        
        // Draw the center X button
        boolean isCenterHovered = (hovered == -1);
        int xColor = isCenterHovered ? 0xFFFF3333 : 0xFFDDDDDD;
        int bgCenterColor = isCenterHovered ? 0x80FF2222 : 0x60000000;
        
        fillPixelCircle(context, (int)startMouseX, (int)startMouseY, 15, bgCenterColor);
        drawPixelCircle(context, (int)startMouseX, (int)startMouseY, 15, isCenterHovered ? 0xFFFF3333 : 0x80FFFFFF);
        
        int cx = (int)startMouseX;
        int cy = (int)startMouseY;
        drawPixelLine(context, cx - 4, cy - 4, cx + 4, cy + 4, xColor);
        drawPixelLine(context, cx - 4, cy + 4, cx + 4, cy - 4, xColor);
        drawPixelLine(context, cx - 3, cy - 4, cx + 5, cy + 4, xColor);
        drawPixelLine(context, cx - 5, cy + 4, cx + 3, cy - 4, xColor);
        
        // Render Shape Text Boxes (radius 82)
        double textRadius = 82.0;
        for (int i = 0; i < numSectors; i++) {
            double angle = Math.toRadians(270.0 + i * (360.0 / numSectors));
            double x = startMouseX + textRadius * Math.cos(angle);
            double y = startMouseY + textRadius * Math.sin(angle);
            
            ShapeType shape = SHAPES[i];
            String name = shape.getDisplayName().getString();
            int nameWidth = textRenderer.getWidth(name);
            
            boolean isHovered = (i == hovered);
            
            int color = isHovered ? 0xFF55FFFF : 0xFFFFFFFF;
            int bg = isHovered ? 0x90008888 : 0x60000000;
            
            int boxX1 = (int)x - nameWidth / 2 - 5;
            int boxY1 = (int)y - 7;
            int boxX2 = (int)x + nameWidth / 2 + 5;
            int boxY2 = (int)y + 9;
            
            context.fill(boxX1, boxY1, boxX2, boxY2, bg);
            
            if (isHovered) {
                context.fill(boxX1 - 1, boxY1 - 1, boxX2 + 1, boxY1, 0xFF55FFFF);
                context.fill(boxX1 - 1, boxY2, boxX2 + 1, boxY2 + 1, 0xFF55FFFF);
                context.fill(boxX1 - 1, boxY1, boxX1, boxY2, 0xFF55FFFF);
                context.fill(boxX2, boxY1, boxX2 + 1, boxY2, 0xFF55FFFF);
            }
            
            context.drawText(textRenderer, name, (int)x - nameWidth / 2, (int)y - 3, color, false);
        }
        
        String hint = "Hold [H] & Drag to select shape • Release to select • Quick-press for settings";
        int hintWidth = textRenderer.getWidth(hint);
        context.drawText(textRenderer, hint, width / 2 - hintWidth / 2, height - 25, 0xFFAAAAAA, false);
        
        super.render(context, mouseX, mouseY, delta);
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

    private void drawSectorCircle(DrawContext context, int xc, int yc, int radius, int hoveredSector, int normalColor, int highlightColor) {
        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;
        drawSectorCirclePixels(context, xc, yc, x, y, hoveredSector, normalColor, highlightColor);
        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            drawSectorCirclePixels(context, xc, yc, x, y, hoveredSector, normalColor, highlightColor);
        }
    }

    private void drawSectorCirclePixels(DrawContext context, int xc, int yc, int x, int y, int hoveredSector, int normalColor, int highlightColor) {
        drawSectorPixel(context, xc, yc, x, y, hoveredSector, normalColor, highlightColor);
        drawSectorPixel(context, xc, yc, -x, y, hoveredSector, normalColor, highlightColor);
        drawSectorPixel(context, xc, yc, x, -y, hoveredSector, normalColor, highlightColor);
        drawSectorPixel(context, xc, yc, -x, -y, hoveredSector, normalColor, highlightColor);
        drawSectorPixel(context, xc, yc, y, x, hoveredSector, normalColor, highlightColor);
        drawSectorPixel(context, xc, yc, -y, x, hoveredSector, normalColor, highlightColor);
        drawSectorPixel(context, xc, yc, y, -x, hoveredSector, normalColor, highlightColor);
        drawSectorPixel(context, xc, yc, -y, -x, hoveredSector, normalColor, highlightColor);
    }

    private void drawSectorPixel(DrawContext context, int xc, int yc, int dx, int dy, int hoveredSector, int normalColor, int highlightColor) {
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        if (angle < 0) {
            angle += 360.0;
        }
        int sector = getSectorForAngle(angle);
        int color = (sector == hoveredSector) ? highlightColor : normalColor;
        drawPixel(context, xc + dx, yc + dy, 1, color);
    }

    private int getSectorForAngle(double angle) {
        int numSectors = SHAPES.length;
        double sectorSize = 360.0 / numSectors;
        double adjustedAngle = (angle - 270.0 + 360.0 + sectorSize / 2.0) % 360.0;
        return (int) (adjustedAngle / sectorSize) % numSectors;
    }
}
