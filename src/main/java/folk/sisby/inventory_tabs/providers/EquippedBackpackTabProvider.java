package folk.sisby.inventory_tabs.providers;

import folk.sisby.inventory_tabs.tabs.EquippedBackpackTab;
import folk.sisby.inventory_tabs.tabs.Tab;
import folk.sisby.inventory_tabs.util.BackpackUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class EquippedBackpackTabProvider implements TabProvider {
    @Override
    public void addAvailableTabs(LocalPlayer player, Consumer<Tab> addTab) {
        if (player == null) return;
        ItemStack backpack = BackpackUtil.getEquippedBackpack(player);
        if (backpack != null && !backpack.isEmpty()) {
            addTab.accept(new EquippedBackpackTab(backpack));
        }
    }
}
