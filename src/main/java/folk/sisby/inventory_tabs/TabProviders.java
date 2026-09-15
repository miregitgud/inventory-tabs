package folk.sisby.inventory_tabs;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import folk.sisby.inventory_tabs.api.InventoryTabsApi;
import folk.sisby.inventory_tabs.providers.BlockTabProvider;
import folk.sisby.inventory_tabs.providers.ChestBlockTabProvider;
import folk.sisby.inventory_tabs.providers.EnderChestTabProvider;
import folk.sisby.inventory_tabs.providers.EntityTabProvider;
import folk.sisby.inventory_tabs.providers.ItemTabProvider;
import folk.sisby.inventory_tabs.providers.PlayerInventoryTabProvider;
import folk.sisby.inventory_tabs.providers.RegistryTabProvider;
import folk.sisby.inventory_tabs.providers.ShulkerBoxTabProvider;
import folk.sisby.inventory_tabs.providers.SimpleBlockTabProvider;
import folk.sisby.inventory_tabs.providers.SimpleEntityTabProvider;
import folk.sisby.inventory_tabs.providers.SimpleItemTabProvider;
import folk.sisby.inventory_tabs.providers.SimpleStorageBlockTabProvider;
import folk.sisby.inventory_tabs.providers.SneakEntityTabProvider;
import folk.sisby.inventory_tabs.providers.SneakItemTabProvider;
import folk.sisby.inventory_tabs.providers.TabProvider;
import folk.sisby.inventory_tabs.providers.UniqueBlockTabProvider;
import folk.sisby.inventory_tabs.providers.UniqueItemTabProvider;
import folk.sisby.inventory_tabs.providers.VehicleInventoryTabProvider;
import folk.sisby.inventory_tabs.util.RegistryMatcher;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;

public class TabProviders {
    // Tab Provider Registry.
    public static final BiMap<Identifier, TabProvider> REGISTRY = HashBiMap.create();

    // Registry Providers - Can be expanded via #matches
    public static final ShulkerBoxTabProvider BLOCK_SHULKER_BOX = register(InventoryTabs.id("block_shulker_box"), new ShulkerBoxTabProvider());
    public static final EnderChestTabProvider BLOCK_ENDER_CHEST = register(InventoryTabs.id("block_ender_chest"), new EnderChestTabProvider());
    public static final ChestBlockTabProvider BLOCK_CHEST = register(InventoryTabs.id("block_chest"), new ChestBlockTabProvider());
    public static final UniqueBlockTabProvider BLOCK_UNIQUE = register(InventoryTabs.id("block_unique"), new UniqueBlockTabProvider());
    public static final SimpleStorageBlockTabProvider BLOCK_SIMPLE_CONTAINER = register(InventoryTabs.id("block_simple_storage"), new SimpleStorageBlockTabProvider());
    public static final SimpleBlockTabProvider BLOCK_SIMPLE = register(InventoryTabs.id("block_simple"), new SimpleBlockTabProvider());

    public static final SneakEntityTabProvider ENTITY_SNEAK = register(InventoryTabs.id("entity_sneak"), new SneakEntityTabProvider());
    public static final SimpleEntityTabProvider ENTITY_SIMPLE = register(InventoryTabs.id("entity_simple"), new SimpleEntityTabProvider());

    public static final ItemTabProvider ITEM_UNIQUE = register(InventoryTabs.id("item_unique"), new UniqueItemTabProvider());
    public static final ItemTabProvider ITEM_SNEAK = register(InventoryTabs.id("item_sneak"), new SneakItemTabProvider());
    public static final ItemTabProvider ITEM_SIMPLE = register(InventoryTabs.id("item_simple"), new SimpleItemTabProvider());

    // Single-Purpose
    public static final PlayerInventoryTabProvider PLAYER_INVENTORY = register(InventoryTabs.id("player_inventory"), new PlayerInventoryTabProvider());
    public static final VehicleInventoryTabProvider VEHICLE_INVENTORY = register(InventoryTabs.id("vehicle_inventory"), new VehicleInventoryTabProvider());

    public static Set<EntityType<?>> warmEntities = new HashSet<>();

    public static void reload(RegistryAccess manager) {
        InventoryTabs.LOGGER.info("[InventoryTabs] Reloading tab providers.");
        for (String entrypointKey : new String[]{"inventory_tabs", "inventory-tabs"}) {
            FabricLoader.getInstance().getEntrypointContainers(entrypointKey, InventoryTabsApi.class).forEach(container -> {
                try {
                    container.getEntrypoint().onRegisterTabProviders(TabProviders::register);
                } catch (Throwable t) {
                    InventoryTabs.LOGGER.error("Failed to register TabProviders via InventoryTabsApi plugin from mod: {}", container.getProvider().getMetadata().getId(), t);
                }
            });
        }
        refreshConfigPlaceholders();
        if (InventoryTabs.CONFIG.configLogging) {
            Map<String, List<ResourceKey<MenuType<?>>>> types = manager.lookupOrThrow(Registries.MENU).listElementIds().filter(k -> ScreenSupport.allowTabs(k) == null && InventoryTabs.CONFIG.allowScreensByDefault).collect(Collectors.groupingBy(k -> k.identifier().getNamespace()));
            if (!types.isEmpty()) {
                InventoryTabs.LOGGER.warn("[Inventory Tabs] {} Automatically tabbed screen handlers:", types.values().stream().mapToInt(Collection::size).sum());
                types.forEach((namespace, ids) -> InventoryTabs.LOGGER.info(" | {}: {}", namespace, ids.stream().map(ResourceKey::identifier).map(Identifier::getPath).collect(Collectors.joining(", "))));
            }
        }
        reloadRegistryProviders(manager, Registries.BLOCK, getProviders(BlockTabProvider.class), InventoryTabs.CONFIG.blockProviderOverrides);
        warmEntities = reloadRegistryProviders(manager, Registries.ENTITY_TYPE, getProviders(EntityTabProvider.class), InventoryTabs.CONFIG.entityProviderOverrides);
        reloadRegistryProviders(manager, Registries.ITEM, getProviders(ItemTabProvider.class), InventoryTabs.CONFIG.itemProviderOverrides);
        TabManager.clearTabs();
        InventoryTabs.LOGGER.info("[InventoryTabs] Finished reloading tab providers.");
    }

    public static <T extends RegistryTabProvider<?>> Map<Identifier, T> getProviders(Class<T> clazz) {
        return REGISTRY.values().stream().filter(p -> clazz.isAssignableFrom(p.getClass())).sorted(Comparator.comparingInt(p -> ((T) p).getRegistryPriority()).reversed()).collect(Collectors.toMap(p -> REGISTRY.inverse().get(p), p -> (T) p, (a, b) -> b, LinkedHashMap::new));
    }

    public static <T> Set<T> reloadRegistryProviders(RegistryAccess manager, ResourceKey<Registry<T>> registryKey, Map<Identifier, ? extends RegistryTabProvider<T>> providers, Map<String, String> overrideConfig) {
        Set<T> warmValues = new HashSet<>();
        Set<String> valueNamespaces = new HashSet<>();
        Multiset<TagKey<T>> tagSizes = HashMultiset.create();
        Map<TagKey<T>, Identifier> tagProviders = new HashMap<>();
        Multimap<Identifier, TagKey<T>> providerTags = HashMultimap.create();
        Multimap<Identifier, Identifier> providerValues = HashMultimap.create();
        providers.values().forEach(p -> p.values.clear());
        // Construct override map
        Map<RegistryMatcher<T>, RegistryTabProvider<T>> unsortedOverrides = new HashMap<>();
        for (Map.Entry<String, String> override : overrideConfig.entrySet()) {
            RegistryMatcher<T> registryMatcher = RegistryMatcher.fromRegistryString(manager, registryKey, override.getKey());
            if (registryMatcher == null) {
                InventoryTabs.LOGGER.warn("[Inventory Tabs] Unknown override registry value ID {}, skipping...", override.getKey());
                continue;
            }
            if (override.getValue().isEmpty()) {
                unsortedOverrides.put(registryMatcher, null);
                continue;
            }
            if (Identifier.tryParse(override.getValue()) == null || providers.get(Identifier.tryParse(override.getValue())) == null) {
                InventoryTabs.LOGGER.warn("[Inventory Tabs] Unknown override tab provider ID {}, skipping...", override.getValue());
                continue;
            }
            unsortedOverrides.put(registryMatcher, providers.get(Identifier.parse(override.getValue())));
        }
        Map<RegistryMatcher<T>, RegistryTabProvider<T>> overrides = new LinkedHashMap<>();
        unsortedOverrides.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getKey().priority())).forEach(e -> overrides.put(e.getKey(), e.getValue()));

        // Add values to providers
        for (Holder.Reference<T> holder : manager.lookupOrThrow(registryKey).listElements().toList()) {
            ResourceKey<T> key = holder.key();
            T value = holder.value();

            Optional<Map.Entry<RegistryMatcher<T>, RegistryTabProvider<T>>> override = overrides.entrySet().stream().filter(e -> e.getKey().is(holder)).findFirst();
            if (override.isPresent()) {
                if (override.get().getValue() != null) {
                    Identifier providerId = REGISTRY.inverse().get(override.get().getValue());
                    if (InventoryTabs.CONFIG.configLogging && (InventoryTabs.CONFIG.configLoggingVanilla || !key.identifier().getNamespace().equals("minecraft"))) {
                        holder.tags().forEach(tag -> {
                            if (tagProviders.containsKey(tag) && !providerId.equals(tagProviders.get(tag))) {
                                if (tagProviders.get(tag) != null) {
                                    providerTags.remove(tagProviders.get(tag), tag);
                                    tagProviders.put(tag, null);
                                }
                            } else {
                                tagProviders.put(tag, providerId);
                            }
                        });
                    }
                    override.get().getValue().values.add(value);
                }
                continue;
            }
            for (Map.Entry<Identifier, ? extends RegistryTabProvider<T>> provider : providers.entrySet()) {
                if (!InventoryTabs.CONFIG.registryProviderDefaults.getOrDefault(provider.getKey().toString(), true)) continue;
                if (provider.getValue().consumes(value)) {
                    if (InventoryTabs.CONFIG.configLogging && (InventoryTabs.CONFIG.configLoggingVanilla || !key.identifier().getNamespace().equals("minecraft"))) {
                        Identifier providerId = provider.getKey();
                        holder.tags().forEach(tag -> {
                            if (tagProviders.containsKey(tag) && !providerId.equals(tagProviders.get(tag))) {
                                if (tagProviders.get(tag) != null) {
                                    tagSizes.setCount(tag, 0);
                                    providerTags.remove(tagProviders.get(tag), tag);
                                    tagProviders.put(tag, null);
                                }
                            } else {
                                tagSizes.add(tag);
                                providerTags.put(providerId, tag);
                                tagProviders.put(tag, providerId);
                            }
                        });
                        providerValues.put(providerId, key.identifier());
                        valueNamespaces.add(key.identifier().getNamespace());
                    }
                    break;
                }
            }
            warmValues.add(value);
        }
        if (InventoryTabs.CONFIG.configLogging) {
	        Multimap<Identifier, TagKey<T>> reassignableTags = HashMultimap.create();
			providerTags.forEach((id, tag) -> {
				if ("c".equals(tag.location().getNamespace()) || valueNamespaces.contains(tag.location().getNamespace())) {
					reassignableTags.put(id, tag);
				}
			});
	        if (!reassignableTags.isEmpty()) {
                InventoryTabs.LOGGER.warn("[Inventory Tabs] {} Re-assignable provider tags for {}:", reassignableTags.size(), registryKey.identifier());
		        reassignableTags.asMap().forEach((provider, tags) -> {
                    if (!tags.isEmpty()) {
                        InventoryTabs.LOGGER.info(" | {}", provider);
                        tags.stream().collect(Collectors.groupingBy(t -> t.location().getNamespace())).entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
                            InventoryTabs.LOGGER.info(" |  | #{} - {}", e.getKey(), e.getValue().stream().sorted(Comparator.<TagKey<T>>comparingInt(tagSizes::count).reversed()).map(s -> "%s (%s)".formatted(s.location().getPath(), tagSizes.count(s))).collect(Collectors.joining(", ")));
                        });
                    }
                });
            }
            if (!providerValues.isEmpty()) {
                InventoryTabs.LOGGER.warn("[Inventory Tabs] {} Re-assignable provider values for {}:", providerValues.size(), registryKey.identifier());
                providerValues.asMap().forEach((provider, values) -> {
                    if (!values.isEmpty()) {
                        InventoryTabs.LOGGER.info(" | {}", provider);
                        values.stream().collect(Collectors.groupingBy(Identifier::getNamespace)).entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
                            InventoryTabs.LOGGER.info(" |  | {} - {}", e.getKey(), e.getValue().stream().map(Identifier::getPath).sorted().collect(Collectors.joining(", ")));
                        });
                    }
                });
            }
        }
        return warmValues;
    }

    public static void refreshConfigPlaceholders() {
        Map<String, Boolean> tempMap = new HashMap<>(InventoryTabs.CONFIG.registryProviderDefaults);
        InventoryTabs.CONFIG.registryProviderDefaults.clear();
        TabProviders.REGISTRY.keySet().forEach(id -> InventoryTabs.CONFIG.registryProviderDefaults.put(id.toString(), tempMap.getOrDefault(id.toString(), true)));
    }

    public static <T extends TabProvider> T register(Identifier id, T tabProvider) {
        REGISTRY.put(id, tabProvider);
        return tabProvider;
    }
}
