package folk.sisby.inventory_tabs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ChestUtil {
    public static boolean isDouble(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.hasProperty(BlockStateProperties.CHEST_TYPE) && blockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE;
    }

    public static BlockPos getOtherChestBlockPos(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getValue(ChestBlock.TYPE) == ChestType.LEFT) return pos.relative(blockState.getValue(ChestBlock.FACING).getClockWise());
        return pos.relative(blockState.getValue(ChestBlock.FACING).getCounterClockWise());
    }

    public static List<BlockPos> getChestMultiblockPos(Level world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (!blockState.hasProperty(BlockStateProperties.CHEST_TYPE) || blockState.getValue(ChestBlock.TYPE) == ChestType.SINGLE) return List.of(pos);
        List<BlockPos> list = new ArrayList<>();
        list.add(pos);
        list.add(getOtherChestBlockPos(world, pos));
        if (blockState.hasProperty(BlockStateProperties.CHEST_TYPE) && blockState.getValue(ChestBlock.TYPE) == ChestType.RIGHT) Collections.reverse(list);
        return list;
    }
}
