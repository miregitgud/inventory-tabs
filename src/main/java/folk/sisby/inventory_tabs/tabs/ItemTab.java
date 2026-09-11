package folk.sisby.inventory_tabs.tabs;

import folk.sisby.inventory_tabs.util.HandlerSlotUtil;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemTab implements Tab {
    public final ItemStack stack;
    public int slot;
    public final boolean unique;
    public final boolean sneakInteract;
    public final Map<Identifier, Predicate<ItemStack>> preclusions;
    public ItemStack swappedStack = null;
    public int swappedSlot = -1;

    public ItemTab(ItemStack stack, int slot, Map<Identifier, Predicate<ItemStack>> preclusions, boolean unique, boolean sneakInteract) {
        this.stack = stack;
        this.slot = slot;
        this.preclusions = preclusions;
        this.unique = unique;
        this.sneakInteract = sneakInteract;
    }

    public ItemTab(ItemStack stack, int slot, Map<Identifier, Predicate<ItemStack>> preclusions, boolean unique) {
        this(stack, slot, preclusions, unique, false);
    }

    @Override
    public void close(LocalPlayer player, ClientLevel world, AbstractContainerMenu handler, MultiPlayerGameMode interactionManager) {
        if (player == null) return;
        if (swappedSlot != -1) {
            ItemStack inSwappedSlot = player.getInventory().getItem(swappedSlot);
            if (ItemStack.matches(inSwappedSlot, swappedStack)) {
                int slotIndex = handler.findSlot(player.getInventory(), swappedSlot).getAsInt();
                interactionManager.handleContainerInput(handler.containerId, slotIndex, player.getInventory().selected, ContainerInput.SWAP, player);
            }
        }
    }

    @Override
    public void open(LocalPlayer player, ClientLevel world, AbstractContainerMenu handler, MultiPlayerGameMode interactionManager) {
        int slotIndex = handler.findSlot(player.getInventory(), slot).getAsInt();
        if (slotIndex != player.getInventory().selected) interactionManager.handleContainerInput(handler.containerId, slotIndex, player.getInventory().selected, ContainerInput.SWAP, player);
        if (sneakInteract) player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        interactionManager.useItem(player, InteractionHand.MAIN_HAND);
        if (sneakInteract) player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        if (unique && slotIndex != player.getInventory().selected) HandlerSlotUtil.mainHandSwapSlot = slot; // Can't swap back for non-uniques
        if (!unique) {
            this.swappedSlot = this.slot;
            this.swappedStack = player.getInventory().getItem(this.slot);
            this.slot = player.getInventory().selected;
        }
    }

    @Override
    public boolean shouldBeRemoved(Level world, boolean current) {
        if (current) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return true;
        if (!player.getInventory().getItem(slot).equals(stack)) return true;
        if (preclusions.values().stream().anyMatch(p -> p.test(stack))) return true;
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> hs && hs.getMenu().findSlot(player.getInventory(), slot).isEmpty()) return true;
        return false;
    }

    @Override
    public boolean isBuffered() {
        return true;
    }

    @Override
    public Component getHoverText() {
        return !stack.getHoverName().equals(stack.getItem().getName(stack)) ? stack.getHoverName().copy().withStyle(ChatFormatting.ITALIC) : stack.getHoverName();
    }

    @Override
    public ItemStack getTabIcon() {
        return stack;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (unique) {
            return other instanceof ItemTab it && Objects.equals(stack.getItem(), it.stack.getItem()) ||
                    other instanceof BlockTab bt && Objects.equals(stack.getItem(), bt.block.asItem());
        } else {
            return other instanceof ItemTab it && Objects.equals(slot, it.slot);
        }
    }
}
