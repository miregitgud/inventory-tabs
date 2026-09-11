package folk.sisby.inventory_tabs.providers;

import folk.sisby.inventory_tabs.InventoryTabs;
import folk.sisby.inventory_tabs.mixin.ShulkerBoxBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class ShulkerBoxTabProvider extends BlockTabProvider {
    public ShulkerBoxTabProvider() {
        super();
        matches.put(InventoryTabs.id("shulker_box_block"), b -> b instanceof ShulkerBoxBlock);
        preclusions.put(InventoryTabs.id("shulker_box_blocked"), (w, p) -> w.getBlockEntity(p) instanceof ShulkerBoxBlockEntity s && !ShulkerBoxBlockAccessor.canOpen(w.getBlockState(p), w, p, s));
    }

    @Override
    public int getTabOrderPriority(Level world, BlockPos pos) {
        return -50;
    }

    @Override
    public int getRegistryPriority() {
        return 70;
    }
}
