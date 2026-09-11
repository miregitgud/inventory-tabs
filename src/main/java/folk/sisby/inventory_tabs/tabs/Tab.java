package folk.sisby.inventory_tabs.tabs;

import folk.sisby.inventory_tabs.util.DrawUtil;
import folk.sisby.inventory_tabs.util.WidgetPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface Tab {
    Identifier[] TABS_TEXTURE = new Identifier[]{
            Identifier.withDefaultNamespace("textures/gui/sprites/container/creative_inventory/tab_top_unselected_2.png"),
            Identifier.withDefaultNamespace("textures/gui/sprites/container/creative_inventory/tab_top_selected_2.png"),
            Identifier.withDefaultNamespace("textures/gui/sprites/container/creative_inventory/tab_bottom_unselected_2.png"),
            Identifier.withDefaultNamespace("textures/gui/sprites/container/creative_inventory/tab_bottom_selected_2.png")
    };
    int TEXTURE_WIDTH = 26;
    int TEXTURE_HEIGHT = 32;
    int TEXTURE_U = 0;
    int[] TEXTURE_V = new int[]{2, 0, 0, 0};
    int REGION_WIDTH = 26;
    int[] REGION_HEIGHT = new int[]{30, 32, 28, 32};
    int[] Y_OFFSET = new int[]{0, 0, 0, -4};
    int[] ITEM_Y_OFFSET = new int[]{0, 0, -3, -3};
    int[] HEIGHT_OFFSET = new int[]{0, 4, 0, 4};

    /**
     * Opens the screen associated with the tab.
     */
    void open(LocalPlayer player, ClientLevel world, AbstractContainerMenu handler, MultiPlayerGameMode interactionManager);

    /**
     * @return true if the tab should stop being displayed. Should be synced up with the provider that provides this tab.
     */
    boolean shouldBeRemoved(Level world, boolean current);

    /**
     * @return the stack to render as an icon in the default rendering method.
     */
    ItemStack getTabIcon();

    /**
     * @return the text that's displayed when hovering over the tab.
     */
    Component getHoverText();

    /**
     * Called when the screen associated with the tab is closed (for handlers that aren't destroyed when closed on the servers)
     */
    default void close(LocalPlayer player, ClientLevel world, AbstractContainerMenu handler, MultiPlayerGameMode interactionManager) {
    }

    /**
     * @return the tab's left-priority when being displayed. The player's inventory is at 100.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * @return whether the tabs open method instantly opens the screen on the client side without slot sync.
     * Used for the survival inventory. Helps preserve cursor stacks.
     */
    default boolean isInstant() {
        return false;
    }

    /**
     * @return whether the tab can only be safely opened through the player inventory screen.
     * Helps prevent lockups, but might flicker.
     */
    default boolean isBuffered() {
        return false;
    }

    default void render(GuiGraphicsExtractor drawContext, WidgetPosition pos, int width, int height, double mouseX, double mouseY, boolean current) {
        int type = pos.up ? (!current ? 0 : 1) : (!current ? 2 : 3);
        int y = pos.y + (pos.up ? -height : 0);
        int drawHeight = height + HEIGHT_OFFSET[type];
        int drawY = y + Y_OFFSET[type];
        DrawUtil.drawCrunched(drawContext, TABS_TEXTURE[type], pos.x, drawY, width, drawHeight, REGION_WIDTH, REGION_HEIGHT[type], TEXTURE_U, TEXTURE_V[type], TEXTURE_WIDTH, TEXTURE_HEIGHT);
        int itemPadding = Math.max(0, (width - 16) / 2);
        int itemX = pos.x + itemPadding;
        int itemY = y + itemPadding + ITEM_Y_OFFSET[type];
        drawContext.item(getTabIcon(), itemX, itemY);
        if (new Rect2i(itemX, itemY, 16, 16).contains((int) mouseX, (int) mouseY)) {
            drawContext.setTooltipForNextFrame(Minecraft.getInstance().font, getHoverText(), (int) mouseX, (int) mouseY);
        }
    }
}
