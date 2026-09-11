package folk.sisby.inventory_tabs.providers;

import folk.sisby.inventory_tabs.InventoryTabs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;

public class EnderChestTabProvider extends BlockTabProvider {
    public EnderChestTabProvider() {
        super();
        matches.put(InventoryTabs.id("ender_chest_block"), b -> b instanceof EnderChestBlock);
        preclusions.put(InventoryTabs.id("chest_blocked"), ChestBlock::isChestBlockedAt);
    }

    @Override
    public int getTabOrderPriority(Level world, BlockPos pos) {
        return -5;
    }

    @Override
    public int getRegistryPriority() {
        return 60;
    }
}
