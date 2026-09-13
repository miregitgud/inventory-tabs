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

public class EquippedBackpackTab implements Tab {
    public ItemStack stack;

    public EquippedBackpackTab(ItemStack stack) {
        this.stack = stack != null ? stack.copy() : ItemStack.EMPTY;
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
        return equipped == null || equipped.isEmpty() || !BackpackUtil.isBackpack(equipped);
    }

    @Override
    public Component getHoverText() {
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack current = player != null ? BackpackUtil.getEquippedBackpack(player) : stack;
        if (current != null && !current.isEmpty()) {
            Component hoverName = current.getHoverName();
            if (!hoverName.equals(current.getItem().getName(current))) {
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
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack equipped = BackpackUtil.getEquippedBackpack(player);
            if (equipped != null && !equipped.isEmpty()) {
                return equipped;
            }
        }
        return stack;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof EquippedBackpackTab;
    }

    @Override
    public int hashCode() {
        return EquippedBackpackTab.class.hashCode();
    }
}
