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
}
