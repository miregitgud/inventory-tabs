package folk.sisby.inventory_tabs.tabs;

import folk.sisby.inventory_tabs.util.ChestUtil;
import java.util.Map;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import static folk.sisby.inventory_tabs.util.ChestUtil.isDouble;

public class ChestBlockTab extends BlockTab {
    public ChestBlockTab(Level world, BlockPos pos, Map<Identifier, BiPredicate<Level, BlockPos>> preclusions, int priority) {
        super(world, pos, preclusions, priority, false);
    }

    @Override
    protected Component getDefaultHoverText(Level world) {
        return Component.translatable(isDouble(world, pos) ? "container.chestDouble" : "container.chest");
    }

    @Override
    protected void refreshMultiblock(Level world) {
        multiblockPositions.clear();
        multiblockPositions.addAll(ChestUtil.getChestMultiblockPos(world, pos));
    }
}
