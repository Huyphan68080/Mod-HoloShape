package net.holoshape;

import net.minecraft.text.Text;

public enum ShapeType {
    CIRCLE("circle"),
    RECTANGLE("rectangle"),
    SPHERE("sphere"),
    LINE("line"),
    CUBOID("cuboid"),
    CYLINDER("cylinder"),
    CONE("cone"),
    PYRAMID("pyramid"),
    STAR("star"),
    HEART("heart"),
    TORUS("torus"),
    HEXAGON("hexagon"),
    OCTAGON("octagon");

    private final String translationKey;

    ShapeType(String keyName) {
        this.translationKey = "gui.holoshape.shape." + keyName;
    }

    public Text getDisplayName() {
        return Text.translatable(translationKey);
    }
}
