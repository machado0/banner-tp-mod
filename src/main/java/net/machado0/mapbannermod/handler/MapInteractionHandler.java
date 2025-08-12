package net.machado0.mapbannermod.handler;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.machado0.mapbannermod.util.MapCoordinateConverter;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapBannerMarker;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.BlockState;

public class MapInteractionHandler {

    private static final double BANNER_CLICK_TOLERANCE = 8.0;

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!player.isSneaking()) return ActionResult.PASS;

            if (entity instanceof ItemFrameEntity) {
                ItemFrameEntity itemFrame = (ItemFrameEntity) entity;
                ItemStack frameItem = itemFrame.getHeldItemStack();

                if (frameItem.getItem() instanceof FilledMapItem) {
                    MapState mapData = FilledMapItem.getMapState(frameItem, world);
                    if (mapData != null && hitResult != null) {
                        MapCoordinateConverter.MapClickData clickData = MapCoordinateConverter.convertItemFrameClickToMapCoordinates(
                                itemFrame, hitResult.getPos(), mapData
                        );

                        handleItemFrameMapClick(player, world, clickData);
                        return ActionResult.SUCCESS;
                    }
                }
            }

            return ActionResult.PASS;
        });
    }

    private static void handleItemFrameMapClick(PlayerEntity player, World
            world, MapCoordinateConverter.MapClickData clickData) {
        // Get map data to check banner proximity
        ItemFrameEntity itemFrame = getItemFramePlayerIsLookingAt(player, world, 10.0);
        if (itemFrame == null) return;

        ItemStack mapStack = itemFrame.getHeldItemStack();
        MapState mapData = FilledMapItem.getMapState(mapStack, world);

        if (MapCoordinateConverter.isClickNearBanner(clickData, mapData, BANNER_CLICK_TOLERANCE)) {
            MapBannerMarker banner = clickData.nearestBanner;

            player.sendMessage(Text.literal(
                    "Teleportando " + player.getName() + " para " + banner.pos()
            ), false);

            handleBannerInteraction(player, banner);

        }
//            else {
//                player.sendMessage(Text.literal(
//                        "Pixel (" + clickData.mapPixelX + ", " + clickData.mapPixelY + ") " +
//                                "worldPos: " + clickData.worldPos
//                ), false);
//
//            }
    }

    private static void handleBannerInteraction(PlayerEntity player, MapBannerMarker banner) {
        BlockPos bannerPos = banner.pos();

//            player.sendMessage(Text.literal(
//                    "Interagindo com banner " + banner.color().getName() + "!"
//            ));

        player.teleport(bannerPos.getX() + 0.5, bannerPos.getY() + 1.0, bannerPos.getZ() + 0.5, true);
    }

    private static ItemFrameEntity getItemFramePlayerIsLookingAt(PlayerEntity player, World world,
                                                                 double maxDistance) {
        Vec3d eyePos = player.getCameraPosVec(1.0F);
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d targetPos = eyePos.add(lookVec.multiply(maxDistance));

        EntityHitResult entityHit = ProjectileUtil.getEntityCollision(
                world, player, eyePos, targetPos,
                player.getBoundingBox().stretch(lookVec.multiply(maxDistance)).expand(1.0),
                entity -> entity instanceof ItemFrameEntity
        );

        if (entityHit != null && entityHit.getEntity() instanceof ItemFrameEntity) {
            return (ItemFrameEntity) entityHit.getEntity();
        }

        return null;
    }

    private static BlockPos getBlockPlayerIsLookingAt(PlayerEntity player, double maxDistance) {
        Vec3d eyePos = player.getCameraPosVec(1.0F);
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d targetPos = eyePos.add(lookVec.multiply(maxDistance));

        HitResult hitResult = player.getWorld().raycast(new RaycastContext(
                eyePos, targetPos,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return ((BlockHitResult) hitResult).getBlockPos();
        }

        return null;
    }

    private static boolean isBannerBlock(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BannerBlock || state.getBlock() instanceof WallBannerBlock;
    }

    private static boolean isBannerOnMap(MapState mapData, BlockPos pos) {
        return mapData.getBanners().stream()
                .anyMatch(banner -> banner.pos()
                        .equals(pos));
    }
}