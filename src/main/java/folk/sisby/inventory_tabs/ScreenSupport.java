package folk.sisby.inventory_tabs;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;
import folk.sisby.inventory_tabs.util.Tuple;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ScreenSupport {
    public static Map<Identifier, Predicate<AbstractContainerScreen<?>>> DENY = new HashMap<>();
    public static Map<Identifier, Predicate<AbstractContainerScreen<?>>> ALLOW = new HashMap<>();
    public static Map<Identifier, Tuple<Integer, Integer>> SCREEN_BOUND_OFFSETS = new HashMap<>();
    public static Map<Identifier, Boolean> SCREEN_INVERTS = new HashMap<>();

    public static Boolean allowTabs(ResourceKey<MenuType<?>> type) {
        if (InventoryTabs.CONFIG.screenOverrides.entrySet().stream().filter(e -> !e.getValue()).anyMatch(e -> Objects.equals(e.getKey(), type.identifier().toString()))) return false;
        if (InventoryTabs.CONFIG.screenOverrides.entrySet().stream().filter(Map.Entry::getValue).anyMatch(e -> Objects.equals(e.getKey(), type.identifier().toString()))) return true;
        return null;
    }

	public static MenuType<?> getScreenHandlerType(AbstractContainerMenu handler) {
		try {
			return handler.getType();
		} catch (UnsupportedOperationException | NoSuchElementException ignored) {
			return null;
		}
	}

    public static boolean allowTabs(Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> hs && hs.getMenu() != null) {
            if (DENY.values().stream().anyMatch(p -> p.test(hs))) return false;
            if (ALLOW.values().stream().anyMatch(p -> p.test(hs))) return true;
	        MenuType<?> type = getScreenHandlerType(hs.getMenu());
	        if (type != null) {
		        ResourceKey<MenuType<?>> key = BuiltInRegistries.MENU.getResourceKey(type).orElse(null);
				if (key != null) {
					Boolean override = allowTabs(key);
					if (override != null) return override;
				}
	        }
            return InventoryTabs.CONFIG.allowScreensByDefault;
        }
        return false;
    }

    static {
        DENY.put(InventoryTabs.id("creative_screen"), hs -> hs instanceof CreativeModeInventoryScreen);
        ALLOW.put(InventoryTabs.id("horse_screen"), hs -> hs instanceof HorseInventoryScreen);
        ALLOW.put(InventoryTabs.id("backpack_screen"), hs -> {
            String screenClass = hs.getClass().getName();
            String menuClass = hs.getMenu() != null ? hs.getMenu().getClass().getName() : "";
            return screenClass.contains("Backpack") || menuClass.contains("Backpack") ||
                    screenClass.contains("travelersbackpack") || menuClass.contains("travelersbackpack") ||
                    screenClass.contains("inmis") || menuClass.contains("inmis") ||
                    screenClass.contains("sophisticatedbackpacks") || menuClass.contains("sophisticatedbackpacks");
        });
        InventoryTabs.CONFIG.leftBoundOffsetOverride.forEach((screenHandlerId, offset) -> SCREEN_BOUND_OFFSETS.put(screenHandlerId.equals("null") ? null : Identifier.parse(screenHandlerId), new Tuple<>(offset, 0)));
        InventoryTabs.CONFIG.rightBoundOffsetOverride.forEach((screenHandlerId, offset) -> SCREEN_BOUND_OFFSETS.merge(screenHandlerId.equals("null") ? null : Identifier.parse(screenHandlerId), new Tuple<>(0, offset), (o, n) -> new Tuple<>(o.getA(), n.getB())));
        InventoryTabs.CONFIG.invertedTabsOverride.forEach((screenHandlerId, doInvert) -> SCREEN_INVERTS.put(screenHandlerId.equals("null") ? null : Identifier.parse(screenHandlerId), doInvert));
    }
}
