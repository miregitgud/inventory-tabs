package folk.sisby.inventory_tabs.providers;

import folk.sisby.inventory_tabs.InventoryTabs;
import folk.sisby.inventory_tabs.TabProviders;
import folk.sisby.inventory_tabs.tabs.EntityTab;
import folk.sisby.inventory_tabs.tabs.Tab;
import folk.sisby.inventory_tabs.util.PlayerUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public abstract class EntityTabProvider extends RegistryTabProvider<EntityType<?>> {
    public final Map<Identifier, Predicate<Entity>> warmMatches = new HashMap<>();
    public final Set<EntityType<?>> failedMatches = new HashSet<>();
    public final Map<Identifier, Predicate<Entity>> preclusions = new HashMap<>();

    public EntityTabProvider() {
        preclusions.put(InventoryTabs.id("removed"), Entity::isRemoved);
        preclusions.put(InventoryTabs.id("player_in_range"), (e) -> Minecraft.getInstance().player != null && !PlayerUtil.inRange(Minecraft.getInstance().player, e));
        preclusions.put(InventoryTabs.id("vehicle"), e -> Minecraft.getInstance().player != null && e == Minecraft.getInstance().player.getVehicle());
    }

    @Override
    public void addAvailableTabs(LocalPlayer player, Consumer<Tab> addTab) {
        Level world = player.level();
        for (Entity entity : world.getEntitiesOfClass(Entity.class, AABB.ofSize(player.position(), PlayerUtil.REACH * 2, PlayerUtil.REACH * 2, PlayerUtil.REACH * 2))) {
            EntityType<?> type = entity.getType();
            if (!values.contains(type) && !failedMatches.contains(type)) {
                if (TabProviders.warmEntities.contains(type) && warmMatches.values().stream().anyMatch(t -> t.test(entity))) {
                    TabProviders.warmEntities.remove(type);
                    values.add(type);
                } else {
                    failedMatches.add(type);
                }
            }
            if ((values.contains(type) && preclusions.values().stream().noneMatch(p -> p.test(entity)))) {
                addTab.accept(createTab(entity));
            }
        }
    }

    public Tab createTab(Entity entity) {
        return new EntityTab(getTabOrderPriority(entity), entity, preclusions, doSneakInteract());
    }

    public abstract int getTabOrderPriority(Entity entity);

    public abstract boolean doSneakInteract();
}
