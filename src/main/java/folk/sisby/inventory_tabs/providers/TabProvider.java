package folk.sisby.inventory_tabs.providers;

import folk.sisby.inventory_tabs.tabs.Tab;
import java.util.function.Consumer;
import net.minecraft.client.player.LocalPlayer;

public interface TabProvider {
    void addAvailableTabs(LocalPlayer player, Consumer<Tab> addTab);
}
