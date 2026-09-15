package folk.sisby.inventory_tabs;

import com.mojang.blaze3d.platform.InputConstants;
import folk.sisby.inventory_tabs.api.InventoryTabsApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryTabs implements ClientModInitializer {
    public static final String ID = "inventory_tabs";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final InventoryTabsConfig CONFIG = InventoryTabsConfig.createToml(FabricLoader.getInstance().getConfigDir(), "", ID, InventoryTabsConfig.class);

    public static KeyMapping NEXT_TAB;
    public static KeyMapping PREV_TAB;
    public static KeyMapping TOGGLE_TABS;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    @Override
    public void onInitializeClient() {
        CommonLifecycleEvents.TAGS_LOADED.register((manager, success) -> TabProviders.reload(manager));
        ClientTickEvents.END_LEVEL_TICK.register(TabManager::tick);
        NEXT_TAB = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.inventory_tabs.key.next_tab",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB,
                KeyMapping.Category.INVENTORY
        ));
        PREV_TAB = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.inventory_tabs.key.prev_tab",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                KeyMapping.Category.INVENTORY
        ));
        TOGGLE_TABS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.inventory_tabs.key.toggle_tabs",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                KeyMapping.Category.INVENTORY
        ));
        invokeEntrypoints();
    }

    private void invokeEntrypoints() {
        for (String entrypointKey : new String[]{"inventory_tabs", "inventory-tabs"}) {
            FabricLoader.getInstance().getEntrypointContainers(entrypointKey, InventoryTabsApi.class).forEach(container -> {
                try {
                    container.getEntrypoint().onInit();
                } catch (Throwable t) {
                    LOGGER.error("Failed to initialize InventoryTabsApi plugin from mod: {}", container.getProvider().getMetadata().getId(), t);
                }
            });
        }
    }
}
