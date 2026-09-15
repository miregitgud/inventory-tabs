package folk.sisby.inventory_tabs.api;

import folk.sisby.inventory_tabs.providers.TabProvider;
import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface TabRegistry {
    void register(Identifier id, TabProvider provider);
}
