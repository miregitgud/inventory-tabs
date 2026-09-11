package folk.sisby.inventory_tabs.providers;

import folk.sisby.inventory_tabs.InventoryTabs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public class SneakEntityTabProvider extends EntityTabProvider {
    public SneakEntityTabProvider () {
        super();
        warmMatches.put(InventoryTabs.id("rideable_openable_inventory"), e -> e instanceof HasCustomInventoryScreen);
        preclusions.put(InventoryTabs.id("untamed"), e -> e instanceof AbstractHorse h && !h.isTamed());
    }

    @Override
    public int getRegistryPriority() {
        return 30;
    }

    @Override
    public int getTabOrderPriority(Entity entity) {
        return entity instanceof HasCustomInventoryScreen ? 45 : 40;
    }

    @Override
    public boolean doSneakInteract() {
        return true;
    }
}
