package folk.sisby.inventory_tabs.providers;

import folk.sisby.inventory_tabs.InventoryTabs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;

public class SimpleStorageBlockTabProvider extends BlockTabProvider {
    public SimpleStorageBlockTabProvider() {
        super();
        matches.put(InventoryTabs.id("barrel_block"), b -> b instanceof BarrelBlock);
    }

    @Override
    public int getTabOrderPriority(Level world, BlockPos pos) {
        return -50;
    }

    @Override
    public int getRegistryPriority() {
        return 10;
    }
}
