package net.machado0.mapbannermod.util;

import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.map.MapBannerMarker;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class MapCoordinateConverter {

    public static class MapClickData {
        public final Vec3d hitPos;
        public final int mapPixelX;
        public final int mapPixelY;
        public final BlockPos worldPos;
        public final MapBannerMarker nearestBanner;

        public MapClickData(Vec3d hitPos, int mapPixelX, int mapPixelY, BlockPos worldPos, MapBannerMarker nearestBanner) {
            this.hitPos = hitPos;
            this.mapPixelX = mapPixelX;
            this.mapPixelY = mapPixelY;
            this.worldPos = worldPos;
            this.nearestBanner = nearestBanner;
        }
    }

    public static MapClickData convertItemFrameClickToMapCoordinates(
            ItemFrameEntity itemFrame, Vec3d hitPos, MapState mapData) {

        Direction facing = itemFrame.getHorizontalFacing();

        Vec3d framePos = itemFrame.getPos();
        Vec3d localHit = hitPos.subtract(framePos);

        Vec2d frameCoords = convert3DToFrameCoords(localHit, facing);

        int mapPixelX = (int) ((frameCoords.x + 0.5) * 128);
        int mapPixelY = (int) ((frameCoords.y + 0.5) * 128);

        mapPixelX = MathHelper.clamp(mapPixelX, 0, 127);
        mapPixelY = MathHelper.clamp(mapPixelY, 0, 127);

        BlockPos worldPos = convertMapPixelToWorldPos(mapPixelX, mapPixelY, mapData);

        MapBannerMarker nearestBanner = findNearestBanner(mapPixelX, mapPixelY, mapData);

        return new MapClickData(hitPos, mapPixelX, mapPixelY, worldPos, nearestBanner);
    }

    private static Vec2d convert3DToFrameCoords(Vec3d localHit, Direction facing) {
        double x, y;

        y = switch (facing) {
            case NORTH -> {
                x = -localHit.x;  // Flip X for north-facing
                yield -localHit.y; // Facing negative Z
                // Y is vertical
            }
            case SOUTH -> {
                x = localHit.x;   // X as-is for south-facing
                yield -localHit.y; // Facing positive Z
                // Y is vertical
            }
            case WEST -> {
                x = localHit.z;   // Z becomes X for west-facing
                yield -localHit.y; // Facing negative X
                // Y is vertical
            }
            case EAST -> {
                x = -localHit.z;  // Flip Z for east-facing
                yield -localHit.y; // Facing positive X
                // Y is vertical
            }
            default -> {
                x = localHit.x;
                yield localHit.z;
            }
        };

        return new Vec2d(x, y);
    }

    private static BlockPos convertMapPixelToWorldPos(int mapPixelX, int mapPixelY, MapState mapData) {
        int centerX = mapData.centerX;
        int centerZ = mapData.centerZ;
        byte scale = mapData.scale;

        int pixelSize = 1 << scale; // 2^scale blocks per pixel

        int worldX = centerX + (mapPixelX - 64) * pixelSize;
        int worldZ = centerZ + (mapPixelY - 64) * pixelSize;

        int worldY = 64;

        return new BlockPos(worldX, worldY, worldZ);
    }

    private static MapBannerMarker findNearestBanner(int clickPixelX, int clickPixelY, MapState mapData) {
        MapBannerMarker closestBanner = null;
        double closestDistance = Double.MAX_VALUE;

        for (MapBannerMarker banner : mapData.getBanners()) {
            Vec2d bannerPixel = convertWorldPosToMapPixel(banner.pos(), mapData);

            double distance = Math.sqrt(
                    Math.pow(clickPixelX - bannerPixel.x, 2) +
                            Math.pow(clickPixelY - bannerPixel.y, 2)
            );

            if (distance < closestDistance) {
                closestDistance = distance;
                closestBanner = banner;
            }
        }

        return closestBanner;
    }

    private static Vec2d convertWorldPosToMapPixel(BlockPos worldPos, MapState mapData) {
        int centerX = mapData.centerX;
        int centerZ = mapData.centerZ;
        byte scale = mapData.scale;
        int pixelSize = 1 << scale;

        double pixelX = 64 + (double) (worldPos.getX() - centerX) / pixelSize;
        double pixelY = 64 + (double) (worldPos.getZ() - centerZ) / pixelSize;

        return new Vec2d(pixelX, pixelY);
    }

    public static boolean isClickNearBanner(MapClickData clickData, MapState mapData, double tolerancePixels) {
        if (clickData.nearestBanner == null) return false;

        Vec2d bannerPixel = convertWorldPosToMapPixel(
                clickData.nearestBanner.pos(),
                mapData
        );

        double distance = Math.sqrt(
                Math.pow(clickData.mapPixelX - bannerPixel.x, 2) +
                        Math.pow(clickData.mapPixelY - bannerPixel.y, 2)
        );

        return distance <= tolerancePixels;
    }

    public static class Vec2d {
        public final double x;
        public final double y;

        public Vec2d(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}