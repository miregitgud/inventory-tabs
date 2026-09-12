package folk.sisby.inventory_tabs.util;

import folk.sisby.inventory_tabs.InventoryTabs;
import folk.sisby.inventory_tabs.TabManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PlayerUtil {
    public static final int REACH = 5;
    public static final double BLOCK_REACH_SQUARE = REACH * REACH;

    private static final Vec3[] SAMPLE_OFFSETS = new Vec3[]{
            new Vec3(0.5D, 0.5D, 0.5D),
            new Vec3(0.5D, 0.9D, 0.5D),
            new Vec3(0.5D, 0.1D, 0.5D),
            new Vec3(0.1D, 0.5D, 0.5D),
            new Vec3(0.9D, 0.5D, 0.5D),
            new Vec3(0.5D, 0.5D, 0.1D),
            new Vec3(0.5D, 0.5D, 0.9D),
            new Vec3(0.2D, 0.8D, 0.2D),
            new Vec3(0.8D, 0.8D, 0.8D)
    };

    public static boolean inRange(Player player, BlockPos pos) {
        if (Vec3.atCenterOf(pos).distanceToSqr(player.getEyePosition()) > BLOCK_REACH_SQUARE) return false;
        BlockHitResult result = raycast(player, pos);
        return pos.equals(result.getBlockPos());
    }

    public static boolean inRange(Player player, Entity entity) {
        if (entity.position().distanceToSqr(player.getEyePosition()) > BLOCK_REACH_SQUARE) return false;
        if (InventoryTabs.CONFIG.ignoreWalls) return true;
        EntityHitResult result = raycast(player, entity);
        return result != null && entity.equals(result.getEntity());
    }

    public static BlockHitResult raycast(Player player, BlockPos pos) {
        RaycastCache raycastCache = TabManager.blockRaycastCache.get(pos);
        if (raycastCache != null && raycastCache.lastValidOffset != null) {
            BlockHitResult cachedHit = player.level().clip(new ClipContext(player.getEyePosition(), Vec3.atLowerCornerOf(pos).add(raycastCache.lastValidOffset), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            if (cachedHit.getType() != HitResult.Type.MISS && cachedHit.getBlockPos().equals(pos)) {
                raycastCache.hit(raycastCache.lastValidOffset);
                return cachedHit;
            }
        }
        for (Vec3 offset : SAMPLE_OFFSETS) {
            BlockHitResult hitResult = player.level().clip(new ClipContext(player.getEyePosition(), Vec3.atLowerCornerOf(pos).add(offset), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            if (hitResult.getType() != HitResult.Type.MISS && hitResult.getBlockPos().equals(pos)) {
                TabManager.blockRaycastCache.computeIfAbsent(pos, p -> new RaycastCache()).hit(offset);
                return hitResult;
            }
        }
        return BlockHitResult.miss(player.position(), Direction.EAST, player.blockPosition());
    }

    public static EntityHitResult raycast(Player player, Entity entity) {
        return ProjectileUtil.getEntityHitResult(player, player.getEyePosition(), entity.position(), player.getBoundingBox().expandTowards(entity.getViewVector(1.0F).scale(REACH)).inflate(1.0, 1.0, 1.0), e -> true, BLOCK_REACH_SQUARE);
    }
}
