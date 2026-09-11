package folk.sisby.inventory_tabs.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class PlayerInventoryTab implements Tab {
    public static final Component TITLE = Component.translatable("gui.inventory_tabs.tab.inventory");
    public ItemStack itemStack;

    public PlayerInventoryTab() {
        itemStack = new ItemStack(Blocks.PLAYER_HEAD);
        itemStack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(Minecraft.getInstance().player.getGameProfile()));
    }

    @Override
    public void open(LocalPlayer player, ClientLevel world, AbstractContainerMenu handler, MultiPlayerGameMode interactionManager) {
        Minecraft.getInstance().setScreenAndShow(new InventoryScreen(player));
    }

    @Override
    public void close(LocalPlayer player, ClientLevel world, AbstractContainerMenu handler, MultiPlayerGameMode interactionManager) {
        if (player != null) player.inventoryMenu.setCarried(ItemStack.EMPTY);
    }

    @Override
    public boolean shouldBeRemoved(Level world, boolean current) {
        return false;
    }

    @Override
    public Component getHoverText() {
        return TITLE;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean isInstant() {
        return true;
    }

    @Override
    public ItemStack getTabIcon() {
        return itemStack;
    }

    @Override
    public boolean equals(Object other) {
        return other != null && getClass() == other.getClass();
    }
}
