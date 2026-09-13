package folk.sisby.inventory_tabs.tabs;

import folk.sisby.inventory_tabs.util.BackpackUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class EquippedBackpackTab implements Tab {
    public final ItemStack stack;

    public EquippedBackpackTab(ItemStack stack) {
        this.stack = stack.copy();
    }

    @Override
    public void open(LocalPlayer player, ClientLevel world, AbstractContainerMenu handler, MultiPlayerGameMode interactionManager) {
        BackpackUtil.openEquippedBackpack(player);
    }

    @Override
    public boolean shouldBeRemoved(Level world, boolean current) {
        if (current) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return true;
        ItemStack equipped = BackpackUtil.getEquippedBackpack(player);
        return equipped == null || equipped.isEmpty() || !ItemStack.isSameItemSameComponents(stack, equipped);
    }

    @Override
    public Component getHoverText() {
        if (!stack.isEmpty()) {
            Component hoverName = stack.getHoverName();
            if (!hoverName.equals(stack.getItem().getName(stack))) {
                return hoverName.copy().withStyle(ChatFormatting.ITALIC);
            }
            return hoverName;
        }
        return Component.translatable("gui.inventory_tabs.tab.equipped_backpack");
    }

    @Override
    public int getPriority() {
        return 90;
    }

    @Override
    public boolean isBuffered() {
        return true;
    }

    @Override
    public boolean isInstant() {
        return false;
    }

    @Override
    public ItemStack getTabIcon() {
        return stack;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        EquippedBackpackTab that = (EquippedBackpackTab) other;
        return ItemStack.isSameItemSameComponents(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack.getItem());
    }
}
