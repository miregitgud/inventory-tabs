package folk.sisby.inventory_tabs.duck;

import folk.sisby.inventory_tabs.InventoryTabs;
import folk.sisby.inventory_tabs.ScreenSupport;
import folk.sisby.inventory_tabs.mixin.HandledScreenAccessor;
import folk.sisby.inventory_tabs.util.Tuple;
import folk.sisby.inventory_tabs.util.WidgetPosition;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public interface InventoryTabsScreen {
    boolean inventoryTabs$allowTabs();

    default List<WidgetPosition> getTabPositions(int tabWidth) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) this;
        List<WidgetPosition> list = new ArrayList<>();
        Identifier screenHandlerId = BuiltInRegistries.MENU.getKey(ScreenSupport.getScreenHandlerType(screen.getMenu()));
        Tuple<Integer, Integer> offsets = ScreenSupport.SCREEN_BOUND_OFFSETS.getOrDefault(screenHandlerId, new Tuple<>(0,0));
        boolean invert = ScreenSupport.SCREEN_INVERTS.getOrDefault(screenHandlerId, InventoryTabs.CONFIG.invertTabsByDefault);
        int width = ((HandledScreenAccessor) screen).getBackgroundWidth() + offsets.getA() + offsets.getB();
        int left = Math.max(((HandledScreenAccessor) screen).getX() - offsets.getA(), 0);

        int count = width / tabWidth;
        int margins = width - tabWidth * count;

        for (int i = 0; i < count; i++) {
            list.add(new WidgetPosition(left + margins / 2 + i * tabWidth, invert ? ((HandledScreenAccessor) screen).getY() + ((HandledScreenAccessor) screen).getBackgroundHeight() : ((HandledScreenAccessor) screen).getY(), !invert));
        }

        return list;
    }
}
