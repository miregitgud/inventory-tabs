package folk.sisby.inventory_tabs.api;

public interface InventoryTabsApi {
    /**
     * Called when tab providers are registered or reloaded.
     * Third-party mods can register custom TabProviders using the registry.
     *
     * @param registry the TabRegistry to register providers into
     */
    default void onRegisterTabProviders(TabRegistry registry) {}

    /**
     * Called during mod initialization to register tab guessers or other custom handlers.
     */
    default void onInit() {}
}
