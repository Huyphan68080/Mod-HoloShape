package net.holoshape;

public enum ShapeType {
    CIRCLE("Vòng tròn"),
    RECTANGLE("Hình chữ nhật"),
    SPHERE("Hình cầu"),
    LINE("Đường thẳng"),
    CUBOID("Hình hộp"),
    CYLINDER("Hình trụ"),
    CONE("Hình nón"),
    PYRAMID("Hình chóp"),
    STAR("Ngôi sao"),
    HEART("Trái tim"),
    TORUS("Hình phao"),
    HEXAGON("Hình lục giác"),
    OCTAGON("Hình bát giác");

    private final String displayName;

    ShapeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
