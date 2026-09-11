package folk.sisby.inventory_tabs.providers;

import folk.sisby.inventory_tabs.InventoryTabs;
import folk.sisby.inventory_tabs.tabs.ChestBlockTab;
import folk.sisby.inventory_tabs.tabs.Tab;
import folk.sisby.inventory_tabs.util.ChestUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.ChestBlock;

public class ChestBlockTabProvider extends BlockTabProvider {
    public ChestBlockTabProvider() {
        super();
        matches.put(InventoryTabs.id("abstract_chest_block"), b -> b instanceof AbstractChestBlock<?>);
        preclusions.put(InventoryTabs.id("chest_blocked"), (w, p) -> ChestUtil.getChestMultiblockPos(w, p).stream().anyMatch(mbp -> ChestBlock.isChestBlockedAt(w, mbp)));
    }

    @Override
    public Tab createTab(Level world, BlockPos pos) {
        return new ChestBlockTab(world, pos, preclusions, getTabOrderPriority(world, pos));
    }

    @Override
    public int getTabOrderPriority(Level world, BlockPos pos) {
        return -10;
    }

    @Override
    public int getRegistryPriority() {
        return 50;
    }
}
