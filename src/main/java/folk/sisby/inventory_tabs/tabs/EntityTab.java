package folk.sisby.inventory_tabs.tabs;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityTab implements Tab {
    public final int priority;
    public final Entity entity;
    public final boolean sneakInteract;
    public final Map<Identifier, Predicate<Entity>> preclusions;
    public ItemStack itemStack;

    public EntityTab(int priority, Entity entity, Map<Identifier, Predicate<Entity>> preclusions, boolean sneakInteract) {
        this.priority = priority;
        this.entity = entity;
        this.preclusions = preclusions;
        this.sneakInteract = sneakInteract;
        this.itemStack = entity.getPickResult() != null ? entity.getPickResult() : Items.BARRIER.getDefaultInstance();
        refreshPreviewStack();
    }

    @Override
    public void open(LocalPlayer player, ClientLevel world, AbstractContainerMenu handler, MultiPlayerGameMode interactionManager) {
        player.connection.send(ServerboundInteractPacket.createInteractionPacket(entity, sneakInteract, player.getUsedItemHand()));
        if (sneakInteract) player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
    }

    @Override
    public boolean shouldBeRemoved(Level world, boolean current) {
        if (current) return false;
        return preclusions.values().stream().anyMatch(p -> p.test(entity));
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public Component getHoverText() {
        return entity.hasCustomName() ? entity.getCustomName().copy().withStyle(ChatFormatting.ITALIC) : entity.getName();
    }

    @Override
    public ItemStack getTabIcon() {
        return itemStack;
    }

    protected void refreshPreviewStack() {
    }

    @Override
    public boolean equals(Object other) {
        return other != null && getClass() == other.getClass() && Objects.equals(entity.getUUID(), ((EntityTab) other).entity.getUUID());
    }
}
