package folk.sisby.inventory_tabs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BlockUtil {
    public static List<BlockPos> getBlocksInRadius(BlockPos center, int radius) {
        List<BlockPos> outList = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    outList.add(new BlockPos(center.getX() + x, center.getY() + y, center.getZ() + z));
                }
            }
        }
        return outList;
    }

    public static <T> List<T> getAttachedBlocks(Level world, BlockPos pos, BiFunction<Level, BlockPos, T> mapper) {
        List<T> outList = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!direction.getAxis().isHorizontal()) continue;
            BlockPos attachedPos = pos.relative(direction, 1);
            BlockState attachedState = world.getBlockState(attachedPos);
            if (attachedState.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && attachedState.getValue(BlockStateProperties.HORIZONTAL_FACING) == direction) {
                T mappedValue = mapper.apply(world, attachedPos);
                if (mappedValue != null) outList.add(mappedValue);
            }
        }
        return outList;
    }
}
