package net.holoshape;

import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class ShapeMath {

    /**
     * Tính toán các block tương đối (offset) nằm trên chu vi hình tròn.
     */
    public static List<BlockPos> getCircleOffsets(double radius) {
        List<BlockPos> offsets = new ArrayList<>();
        if (radius < 0.1) {
            return offsets;
        }

        int rLimit = (int) Math.ceil(radius + 0.5);
        double innerRadiusSq = Math.pow(radius - 0.5, 2);
        double outerRadiusSq = Math.pow(radius + 0.5, 2);

        for (int dx = -rLimit; dx <= rLimit; dx++) {
            for (int dz = -rLimit; dz <= rLimit; dz++) {
                double distSq = dx * dx + dz * dz;
                if (distSq >= innerRadiusSq && distSq < outerRadiusSq) {
                    offsets.add(new BlockPos(dx, 0, dz));
                }
            }
        }
        return offsets;
    }

    /**
     * Tính toán các block tương đối (offset) tạo thành đường viền hình chữ nhật trên phẳng X-Z.
     */
    public static List<BlockPos> getRectangleOffsets(BlockPos p1, BlockPos p2) {
        List<BlockPos> offsets = new ArrayList<>();
        if (p1 == null || p2 == null) return offsets;

        int dx = p2.getX() - p1.getX();
        int dz = p2.getZ() - p1.getZ();
        int minX = Math.min(0, dx);
        int maxX = Math.max(0, dx);
        int minZ = Math.min(0, dz);
        int maxZ = Math.max(0, dz);

        // Vẽ các cạnh song song với trục X
        for (int x = minX; x <= maxX; x++) {
            offsets.add(new BlockPos(x, 0, minZ));
            if (minZ != maxZ) {
                offsets.add(new BlockPos(x, 0, maxZ));
            }
        }
        // Vẽ các cạnh song song với trục Z (tránh trùng góc đã vẽ)
        for (int z = minZ + 1; z < maxZ; z++) {
            offsets.add(new BlockPos(minX, 0, z));
            if (minX != maxX) {
                offsets.add(new BlockPos(maxX, 0, z));
            }
        }
        return offsets;
    }

    /**
     * Tính toán các block tương đối (offset) tạo thành hình cầu rỗng trong không gian 3D.
     */
    public static List<BlockPos> getSphereOffsets(double radius) {
        List<BlockPos> offsets = new ArrayList<>();
        if (radius < 0.1) {
            return offsets;
        }

        int rLimit = (int) Math.ceil(radius + 0.5);
        double innerRadiusSq = Math.pow(radius - 0.5, 2);
        double outerRadiusSq = Math.pow(radius + 0.5, 2);

        for (int dx = -rLimit; dx <= rLimit; dx++) {
            for (int dy = -rLimit; dy <= rLimit; dy++) {
                for (int dz = -rLimit; dz <= rLimit; dz++) {
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq >= innerRadiusSq && distSq < outerRadiusSq) {
                        offsets.add(new BlockPos(dx, dy, dz));
                    }
                }
            }
        }
        return offsets;
    }

    /**
     * Tính toán các block tương đối (offset) tạo thành đường thẳng 3D nối 2 điểm.
     */
    public static List<BlockPos> getLineOffsets(BlockPos p1, BlockPos p2) {
        List<BlockPos> offsets = new ArrayList<>();
        if (p1 == null || p2 == null) return offsets;

        int dx = p2.getX() - p1.getX();
        int dy = p2.getY() - p1.getY();
        int dz = p2.getZ() - p1.getZ();

        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (steps == 0) {
            offsets.add(BlockPos.ORIGIN);
            return offsets;
        }

        double xStep = (double) dx / steps;
        double yStep = (double) dy / steps;
        double zStep = (double) dz / steps;

        double x = 0.0;
        double y = 0.0;
        double z = 0.0;

        for (int i = 0; i <= steps; i++) {
            offsets.add(new BlockPos((int) Math.round(x), (int) Math.round(y), (int) Math.round(z)));
            x += xStep;
            y += yStep;
            z += zStep;
        }
        return offsets;
    }

    /**
     * Tính toán các block tương đối tạo thành hình hộp chữ nhật rỗng 3D.
     */
    public static List<BlockPos> getCuboidOffsets(BlockPos p1, BlockPos p2) {
        if (p1 == null || p2 == null) return new ArrayList<>();

        int dx = p2.getX() - p1.getX();
        int dy = p2.getY() - p1.getY();
        int dz = p2.getZ() - p1.getZ();

        int minX = Math.min(0, dx);
        int maxX = Math.max(0, dx);
        int minY = Math.min(0, dy);
        int maxY = Math.max(0, dy);
        int minZ = Math.min(0, dz);
        int maxZ = Math.max(0, dz);

        java.util.Set<BlockPos> set = new java.util.LinkedHashSet<>();

        // Mặt X
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                set.add(new BlockPos(minX, y, z));
                set.add(new BlockPos(maxX, y, z));
            }
        }

        // Mặt Y
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                set.add(new BlockPos(x, minY, z));
                set.add(new BlockPos(x, maxY, z));
            }
        }

        // Mặt Z
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                set.add(new BlockPos(x, y, minZ));
                set.add(new BlockPos(x, y, maxZ));
            }
        }

        return new ArrayList<>(set);
    }

    /**
     * Tính toán các block tương đối tạo thành hình trụ rỗng 3D (có nắp trên và nắp dưới).
     */
    public static List<BlockPos> getCylinderOffsets(double radius, int height) {
        List<BlockPos> offsets = new ArrayList<>();
        if (radius < 0.1) return offsets;

        int rLimit = (int) Math.ceil(radius + 0.5);
        double innerRadiusSq = Math.pow(radius - 0.5, 2);
        double outerRadiusSq = Math.pow(radius + 0.5, 2);

        int minY = Math.min(0, height);
        int maxY = Math.max(0, height);

        for (int y = minY; y <= maxY; y++) {
            boolean isCap = (y == minY || y == maxY);
            for (int dx = -rLimit; dx <= rLimit; dx++) {
                for (int dz = -rLimit; dz <= rLimit; dz++) {
                    double distSq = dx * dx + dz * dz;
                    if (isCap) {
                        if (distSq < outerRadiusSq) {
                            offsets.add(new BlockPos(dx, y, dz));
                        }
                    } else {
                        if (distSq >= innerRadiusSq && distSq < outerRadiusSq) {
                            offsets.add(new BlockPos(dx, y, dz));
                        }
                    }
                }
            }
        }
        return offsets;
    }

    /**
     * Tính toán các block tương đối tạo thành hình nón rỗng 3D.
     */
    public static List<BlockPos> getConeOffsets(double radius, int height) {
        List<BlockPos> offsets = new ArrayList<>();
        if (radius < 0.1) return offsets;

        int absHeight = Math.abs(height);
        if (absHeight == 0) {
            return getCircleOffsets(radius);
        }

        int minY = Math.min(0, height);
        int maxY = Math.max(0, height);
        int baseY = (height >= 0) ? minY : maxY;

        for (int y = minY; y <= maxY; y++) {
            int h = Math.abs(y - baseY);
            double fraction = 1.0 - ((double) h / absHeight);
            double layerRadius = radius * fraction;

            if (layerRadius < 0.1) {
                if (h == absHeight) {
                    offsets.add(new BlockPos(0, y, 0));
                }
                continue;
            }

            int rLimit = (int) Math.ceil(layerRadius + 0.5);
            double innerRadiusSq = Math.pow(layerRadius - 0.5, 2);
            double outerRadiusSq = Math.pow(layerRadius + 0.5, 2);

            boolean isBase = (y == baseY);
            for (int dx = -rLimit; dx <= rLimit; dx++) {
                for (int dz = -rLimit; dz <= rLimit; dz++) {
                    double distSq = dx * dx + dz * dz;
                    if (isBase) {
                        if (distSq < outerRadiusSq) {
                            offsets.add(new BlockPos(dx, y, dz));
                        }
                    } else {
                        if (distSq >= innerRadiusSq && distSq < outerRadiusSq) {
                            offsets.add(new BlockPos(dx, y, dz));
                        }
                    }
                }
            }
        }
        return offsets;
    }

    /**
     * Tính toán các block tương đối tạo thành hình chóp tứ giác rỗng 3D.
     */
    public static List<BlockPos> getPyramidOffsets(int rx, int ry, int rz) {
        List<BlockPos> offsets = new ArrayList<>();
        int absRy = Math.abs(ry);
        int absRx = Math.abs(rx);
        int absRz = Math.abs(rz);

        if (absRy == 0) {
            for (int x = -absRx; x <= absRx; x++) {
                for (int z = -absRz; z <= absRz; z++) {
                    if (x == -absRx || x == absRx || z == -absRz || z == absRz) {
                        offsets.add(new BlockPos(x, 0, z));
                    }
                }
            }
            return offsets;
        }

        int minY = Math.min(0, ry);
        int maxY = Math.max(0, ry);
        int baseY = (ry >= 0) ? minY : maxY;

        for (int y = minY; y <= maxY; y++) {
            int h = Math.abs(y - baseY);
            double fraction = 1.0 - ((double) h / absRy);
            int currentRx = (int) Math.round(absRx * fraction);
            int currentRz = (int) Math.round(absRz * fraction);

            for (int x = -currentRx; x <= currentRx; x++) {
                for (int z = -currentRz; z <= currentRz; z++) {
                    if (x == -currentRx || x == currentRx || z == -currentRz || z == currentRz) {
                        offsets.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return offsets;
    }

    /**
     * Tính toán các block tương đối tạo thành hình lăng trụ ngôi sao 3D.
     */
    public static List<BlockPos> getStarOffsets(double radius, int height) {
        if (radius < 0.1) return new ArrayList<>();

        int minY = Math.min(0, height);
        int maxY = Math.max(0, height);
        java.util.Set<BlockPos> offsets = new java.util.LinkedHashSet<>();

        for (int y = minY; y <= maxY; y++) {
            List<BlockPos> vertices = new ArrayList<>();
            for (int k = 0; k < 10; k++) {
                double angle = k * Math.PI / 5.0 - Math.PI / 2.0;
                double r = (k % 2 == 0) ? radius : radius * 0.4;
                int vx = (int) Math.round(r * Math.cos(angle));
                int vz = (int) Math.round(r * Math.sin(angle));
                vertices.add(new BlockPos(vx, y, vz));
            }

            for (int i = 0; i < 10; i++) {
                BlockPos p1 = vertices.get(i);
                BlockPos p2 = vertices.get((i + 1) % 10);
                List<BlockPos> line = getLineOffsets(p1, p2);
                for (BlockPos bp : line) {
                    offsets.add(p1.add(bp));
                }
            }
        }
        return new ArrayList<>(offsets);
    }

    /**
     * Tính toán các block tương đối tạo thành hình lăng trụ trái tim 3D.
     */
    public static List<BlockPos> getHeartOffsets(double radius, int height) {
        if (radius < 0.1) return new ArrayList<>();

        int minY = Math.min(0, height);
        int maxY = Math.max(0, height);
        java.util.Set<BlockPos> offsets = new java.util.LinkedHashSet<>();

        // Dynamic step count based on radius to prevent overlaps and gaps
        int numSteps = Math.max(100, (int) (2.0 * Math.PI * radius * 3.0));

        for (int y = minY; y <= maxY; y++) {
            for (int i = 0; i < numSteps; i++) {
                double t = (2.0 * Math.PI * i) / numSteps;
                double sinT = Math.sin(t);
                double hx = 16.0 * sinT * sinT * sinT;
                double hz = 13.0 * Math.cos(t) - 5.0 * Math.cos(2.0 * t) - 2.0 * Math.cos(3.0 * t) - Math.cos(4.0 * t);

                double sx = (hx / 16.0) * radius;
                double sz = (hz / 17.0) * radius;

                int vx = (int) Math.round(sx);
                int vz = (int) Math.round(-sz);
                offsets.add(new BlockPos(vx, y, vz));
            }
        }
        return new ArrayList<>(offsets);
    }

    /**
     * Tính toán các block tương đối tạo thành hình phao (Torus) 3D rỗng.
     */
    public static List<BlockPos> getTorusOffsets(double majorRadius, double minorRadius) {
        List<BlockPos> offsets = new ArrayList<>();
        if (majorRadius < 0.1) return offsets;
        if (minorRadius < 0.1) minorRadius = 1.0;

        int limitXZ = (int) Math.ceil(majorRadius + minorRadius + 0.5);
        int limitY = (int) Math.ceil(minorRadius + 0.5);

        double innerTubeSq = Math.pow(minorRadius - 0.5, 2);
        double outerTubeSq = Math.pow(minorRadius + 0.5, 2);

        for (int dx = -limitXZ; dx <= limitXZ; dx++) {
            for (int dy = -limitY; dy <= limitY; dy++) {
                for (int dz = -limitXZ; dz <= limitXZ; dz++) {
                    double distXZ = Math.sqrt(dx * dx + dz * dz);
                    double distToTorusSq = Math.pow(distXZ - majorRadius, 2) + dy * dy;
                    if (distToTorusSq >= innerTubeSq && distToTorusSq < outerTubeSq) {
                        offsets.add(new BlockPos(dx, dy, dz));
                    }
                }
            }
        }
        return offsets;
    }

    /**
     * Tính toán các block tương đối tạo thành hình lăng trụ đa giác đều (Hexagon, Octagon, ...).
     */
    public static List<BlockPos> getPolygonOffsets(double radius, int height, int edges) {
        if (radius < 0.1 || edges < 3) return new ArrayList<>();

        int minY = Math.min(0, height);
        int maxY = Math.max(0, height);
        java.util.Set<BlockPos> offsets = new java.util.LinkedHashSet<>();

        for (int y = minY; y <= maxY; y++) {
            List<BlockPos> vertices = new ArrayList<>();
            for (int i = 0; i < edges; i++) {
                double angle = (2.0 * Math.PI * i) / edges - Math.PI / 2.0;
                int vx = (int) Math.round(radius * Math.cos(angle));
                int vz = (int) Math.round(radius * Math.sin(angle));
                vertices.add(new BlockPos(vx, y, vz));
            }

            for (int i = 0; i < edges; i++) {
                BlockPos p1 = vertices.get(i);
                BlockPos p2 = vertices.get((i + 1) % edges);
                List<BlockPos> line = getLineOffsets(p1, p2);
                for (BlockPos bp : line) {
                    offsets.add(p1.add(bp));
                }
            }
        }
        return new ArrayList<>(offsets);
    }

    /**
     * Tính toán các block tương đối tạo thành hình Cổng Vòm (Arch).
     */
    public static List<BlockPos> getArchOffsets(double radius, int legHeight) {
        List<BlockPos> offsets = new ArrayList<>();
        if (radius < 0.1) return offsets;

        int rLimit = (int) Math.ceil(radius + 0.5);
        double innerRadiusSq = Math.pow(radius - 0.5, 2);
        double outerRadiusSq = Math.pow(radius + 0.5, 2);

        // Vòm bán nguyệt ở trên (dy >= 0, dịch chuyển lên trên legHeight)
        for (int dx = -rLimit; dx <= rLimit; dx++) {
            for (int dy = 0; dy <= rLimit; dy++) {
                double distSq = dx * dx + dy * dy;
                if (distSq >= innerRadiusSq && distSq < outerRadiusSq) {
                    offsets.add(new BlockPos(dx, dy + legHeight, 0));
                }
            }
        }

        // Chân vòm thẳng đứng từ y = 0 đến legHeight
        int rInt = (int) Math.round(radius);
        for (int dy = 0; dy < legHeight; dy++) {
            offsets.add(new BlockPos(-rInt, dy, 0));
            offsets.add(new BlockPos(rInt, dy, 0));
        }

        return offsets;
    }

    /**
     * Tính toán các block tương đối tạo thành hình Móng Ngựa (Horseshoe).
     */
    public static List<BlockPos> getHorseshoeOffsets(double radius, int legHeight) {
        List<BlockPos> offsets = new ArrayList<>();
        if (radius < 0.1) return offsets;

        double theta = Math.toRadians(30.0); // Độ phình góc 30 độ ở dưới
        int numSteps = Math.max(100, (int) (2.0 * Math.PI * radius * 3.0));
        java.util.Set<BlockPos> set = new java.util.LinkedHashSet<>();

        // Phần vòm tròn lượng giác từ -theta đến PI + theta
        for (int i = 0; i <= numSteps; i++) {
            double angle = -theta + (Math.PI + 2.0 * theta) * i / numSteps;
            double u = radius * Math.cos(angle);
            double v = legHeight + radius * Math.sin(angle);
            set.add(new BlockPos((int) Math.round(u), (int) Math.round(v), 0));
        }

        // Hai chân thẳng đứng từ v = legHeight - radius * sin(theta) đi xuống v = 0
        double legX = radius * Math.cos(-theta);
        double startY = legHeight - radius * Math.sin(theta);
        int lx1 = (int) Math.round(-legX);
        int lx2 = (int) Math.round(legX);

        for (int y = 0; y <= startY; y++) {
            set.add(new BlockPos(lx1, y, 0));
            set.add(new BlockPos(lx2, y, 0));
        }

        return new ArrayList<>(set);
    }

    /**
     * Tính toán các block tương đối dựa trên 3 công thức toán học tham số t.
     */
    public static List<BlockPos> getMathOffsets(String exprX, String exprY, String exprZ, double tMin, double tMax, double tStep) {
        List<BlockPos> offsets = new ArrayList<>();
        if (tStep <= 0.001) tStep = 0.1;
        
        java.util.Set<BlockPos> set = new java.util.LinkedHashSet<>();
        
        for (double t = tMin; t <= tMax + 0.0001; t += tStep) {
            try {
                double valX = MathParser.eval(exprX, t, t);
                double valY = MathParser.eval(exprY, t, t);
                double valZ = MathParser.eval(exprZ, t, t);
                
                int x = (int) Math.round(valX);
                int y = (int) Math.round(valY);
                int z = (int) Math.round(valZ);
                
                set.add(new BlockPos(x, y, z));
            } catch (Exception e) {
                // Bỏ qua điểm nếu công thức không hợp lệ hoặc chia cho 0
            }
        }
        return new ArrayList<>(set);
    }

    /**
     * Extrude a 2D shape offset list along a direction vector.
     */
    public static List<BlockPos> extrudeOffsets(List<BlockPos> baseOffsets, int length, BlockPos dirVec) {
        if (length <= 1 || baseOffsets == null || baseOffsets.isEmpty() || dirVec.equals(BlockPos.ORIGIN)) {
            return baseOffsets;
        }
        java.util.Set<BlockPos> extruded = new java.util.LinkedHashSet<>();
        for (BlockPos offset : baseOffsets) {
            for (int i = 0; i < length; i++) {
                extruded.add(new BlockPos(
                    offset.getX() + dirVec.getX() * i,
                    offset.getY() + dirVec.getY() * i,
                    offset.getZ() + dirVec.getZ() * i
                ));
            }
        }
        return new ArrayList<>(extruded);
    }

    /**
     * Bộ phân tích cú pháp toán học tự chứa hỗ trợ các hàm: sin, cos, tan, sqrt, abs, pow.
     */
    public static class MathParser {
        public static double eval(final String str, final double t, final double x) {
            if (str == null || str.trim().isEmpty()) return 0.0;
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < str.length()) ? str.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double xVal = parseExpression();
                    if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                    return xVal;
                }

                double parseExpression() {
                    double xVal = parseTerm();
                    for (;;) {
                        if      (eat('+')) xVal += parseTerm();
                        else if (eat('-')) xVal -= parseTerm();
                        else return xVal;
                    }
                }

                double parseTerm() {
                    double xVal = parseFactor();
                    for (;;) {
                        if      (eat('*')) xVal *= parseFactor();
                        else if (eat('/')) {
                            double denom = parseFactor();
                            if (Math.abs(denom) < 1e-9) throw new RuntimeException("Division by zero");
                            xVal /= denom;
                        }
                        else return xVal;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) return -parseFactor();

                    double xVal;
                    int startPos = this.pos;
                    if (eat('(')) {
                        xVal = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        xVal = Double.parseDouble(str.substring(startPos, this.pos));
                    } else if (ch >= 'a' && ch <= 'z') {
                        while (ch >= 'a' && ch <= 'z') nextChar();
                        String name = str.substring(startPos, this.pos);
                        if (name.equals("t")) {
                            xVal = t;
                        } else if (name.equals("x")) {
                            xVal = x;
                        } else if (name.equals("pi")) {
                            xVal = Math.PI;
                        } else if (name.equals("e")) {
                            xVal = Math.E;
                        } else {
                            xVal = parseFactor();
                            if (name.equals("sin")) xVal = Math.sin(xVal);
                            else if (name.equals("cos")) xVal = Math.cos(xVal);
                            else if (name.equals("tan")) xVal = Math.tan(xVal);
                            else if (name.equals("sqrt")) xVal = Math.sqrt(xVal);
                            else if (name.equals("abs")) xVal = Math.abs(xVal);
                            else throw new RuntimeException("Unknown function: " + name);
                        }
                    } else {
                        throw new RuntimeException("Unexpected: " + (char)ch);
                    }

                    if (eat('^')) xVal = Math.pow(xVal, parseFactor());

                    return xVal;
                }
            }.parse();
        }
    }
}
