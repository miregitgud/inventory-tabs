package folk.sisby.inventory_tabs;

import folk.sisby.inventory_tabs.duck.InventoryTabsScreen;
import folk.sisby.inventory_tabs.tabs.BlockTab;
import folk.sisby.inventory_tabs.tabs.EntityTab;
import folk.sisby.inventory_tabs.tabs.ItemTab;
import folk.sisby.inventory_tabs.tabs.PlayerInventoryTab;
import folk.sisby.inventory_tabs.tabs.Tab;
import folk.sisby.inventory_tabs.tabs.VehicleInventoryTab;
import folk.sisby.inventory_tabs.util.RaycastCache;
import folk.sisby.inventory_tabs.util.HandlerSlotUtil;
import folk.sisby.inventory_tabs.util.WidgetPosition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class TabManager {
    public static final Identifier BUTTONS_TEXTURE = InventoryTabs.id("textures/gui/buttons.png");
    public static final int TAB_WIDTH = 24;
    public static final int TAB_HEIGHT = 21; // Without Inset
    public static final int BUTTON_WIDTH = 10;
    public static final int BUTTON_HEIGHT = 18;

    public static final Map<Identifier, BiFunction<AbstractContainerScreen<?>, List<Tab>, Tab>> tabGuessers = new HashMap<>();

    public static Tab nextTab;
    public static AbstractContainerScreen<?> currentScreen;
    public static final List<Tab> tabs = new ArrayList<>();
    public static int currentPage = 0;
    public static Tab currentTab;
    public static List<WidgetPosition> tabPositions = new ArrayList<>();
    public static int holdTabCooldown = 0;
    public static boolean enabled = true;
    public static Map<BlockPos, RaycastCache> blockRaycastCache = new HashMap<>();

    public static void initScreen(Minecraft client, AbstractContainerScreen<?> screen) {
        currentScreen = screen;
        tabPositions = ((InventoryTabsScreen) currentScreen).getTabPositions(TAB_WIDTH);
        if (nextTab == null) {
            nextTab = guessOpenedTab(client, screen);
            finishOpeningScreen(screen.getMenu());
        }
    }

    public static void finishOpeningScreen(AbstractContainerMenu handler) {
        if (nextTab != null) {
            try {
                if (currentTab != null && currentTab != nextTab) currentTab.close(Minecraft.getInstance().player, Minecraft.getInstance().level, handler, Minecraft.getInstance().gameMode);
                HandlerSlotUtil.tryPop(Minecraft.getInstance().player, Minecraft.getInstance().gameMode, handler);
                currentTab = nextTab;
                setCurrentPage(tabPositions.isEmpty() ? 0 : tabs.indexOf(nextTab) / tabPositions.size());
            } catch (Throwable t) {
                InventoryTabs.LOGGER.error("Failed while transitioning screen for tab: {}", nextTab, t);
            } finally {
                nextTab = null;
            }
        }
    }

    public static void screenDiscarded() {
        if (currentTab != null) {
            Minecraft client = Minecraft.getInstance();
            if (client != null && client.player != null) {
                currentTab.close(client.player, client.level, client.player.containerMenu, client.gameMode);
            }
            currentTab = null;
        }
        currentScreen = null;
        tabPositions.clear();
        nextTab = null;
        currentPage = 0;
    }

    public static void tick(ClientLevel world) {
        blockRaycastCache.values().removeIf(timer -> !timer.validThisTick && timer.ticksInvalid >= InventoryTabs.CONFIG.blockRaycastTimeout);
        blockRaycastCache.values().forEach(RaycastCache::tick);
        if (holdTabCooldown > 0) {
            if (InventoryTabs.NEXT_TAB.isDown() || (InventoryTabs.PREV_TAB != null && InventoryTabs.PREV_TAB.isDown())) {
                holdTabCooldown--;
            } else {
                holdTabCooldown = 0;
            }
        }
        if (tabs.removeIf(t -> t.shouldBeRemoved(world, t == currentTab))) {
            sortTabs();
        }
        TabProviders.REGISTRY.values().forEach(tabProvider -> tabProvider.addAvailableTabs(Minecraft.getInstance().player, TabManager::tryAddTab));
        if (currentTab != null && !tabs.contains(currentTab)) currentTab = null;
    }

    public static void openTabImmediate(Tab tab, LocalPlayer player, MultiPlayerGameMode interactionManager, ClientLevel world) {
        nextTab = tab;
        try {
            if (currentScreen != null && currentScreen.getMenu() != null) {
                HandlerSlotUtil.push(player, interactionManager, currentScreen.getMenu(), tab.isInstant());
                player.connection.send(new ServerboundContainerClosePacket(currentScreen.getMenu().containerId));
            }
            tab.open(player, world, currentScreen != null ? currentScreen.getMenu() : null, interactionManager);
            if (tab.isInstant()) { // Instant screens don't have slot updates to wait for, so finish now.
                finishOpeningScreen(currentScreen != null ? currentScreen.getMenu() : null);
            }
        } catch (Throwable t) {
            InventoryTabs.LOGGER.error("Failed to open tab: {}", tab, t);
            nextTab = null;
        }
    }


    public static void openTab(Tab tab) {
        if (tab != currentTab) {
            LocalPlayer player = Minecraft.getInstance().player;
            MultiPlayerGameMode interactionManager = Minecraft.getInstance().gameMode;
            ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
            if (player != null && interactionManager != null && networkHandler != null && player.level() instanceof ClientLevel world) {
                if (!tab.shouldBeRemoved(world, false)) {
                    if (tab.isBuffered() && currentScreen != null && !(currentScreen instanceof InventoryScreen)) {
                        if (currentScreen.getMenu() != null) {
                            HandlerSlotUtil.push(player, interactionManager, currentScreen.getMenu(), false);
                            player.connection.send(new ServerboundContainerClosePacket(currentScreen.getMenu().containerId));
                        }
                        player.containerMenu = player.inventoryMenu;
                    }
                    openTabImmediate(tab, player, interactionManager, world);
                }
            }
        }
    }

    public static Tab guessOpenedTab(Minecraft client, AbstractContainerScreen<?> screen) {
        Level world = client.player.level();
        // "Open Inventory" Guesses
        if (currentScreen instanceof InventoryScreen) return tabs.get(0);
        if (client.player.isPassenger()) {
            for (Tab tab : tabs) {
                if (tab instanceof VehicleInventoryTab vit) {
                    if (client.player.getVehicle().equals(vit.entity)) {
                        return tab;
                    }
                }
            }
        }
        for (BiFunction<AbstractContainerScreen<?>, List<Tab>, Tab> guesser : tabGuessers.values()) {
            Tab guessedTab = guesser.apply(screen, tabs);
            if (guessedTab != null) return guessedTab;
        }
        // Crosshair Guesses
        if (client.hitResult instanceof BlockHitResult result) {
            BlockPos pos = result.getBlockPos();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            for (Tab tab : tabs) {
                if (tab instanceof BlockTab bt) {
                    if (pos.equals(bt.pos) || blockEntity == world.getBlockEntity(bt.pos) || bt.multiblockPositions.contains(pos))
                        return tab;
                }
            }
        } else if (client.hitResult instanceof EntityHitResult result) {
            Entity entity = result.getEntity();
            for (Tab tab : tabs) {
                if (tab instanceof EntityTab et) {
                    if (entity.equals(et.entity)) {
                        return tab;
                    }
                }
            }
        }
        // Hand Guesses
        for (int slot : List.of(client.player.getInventory().getSelectedSlot(), Inventory.SLOT_OFFHAND)) {
            for (Tab tab : tabs) {
                if (tab instanceof ItemTab it) {
                    if (slot == it.slot) {
                        return tab;
                    }
                }
            }
        }
        return null;
    }

    public static void tryAddTab(Tab tab) {
        if (!tabs.contains(tab)) {
            tabs.add(tab);
            sortTabs();
        }
    }

    public static void sortTabs() {
        tabs.sort(Comparator.comparingInt(Tab::getPriority).reversed().thenComparing(t -> t.getHoverText().getString()));
    }

    public static void clearTabs() {
        tabs.clear();
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isLocked()) return true;
        if (isHidden() || tabPositions.isEmpty()) return false;
        if (button == 0) {
            if (getPageButton(true).contains((int) mouseX, (int) mouseY)) {
                if (currentPage > 0) {
                    setCurrentPage(currentPage - 1);
                    playClick();
                }
                return true;
            }

            if (getPageButton(false).contains((int) mouseX, (int) mouseY)) {
                if (currentPage < getMaximumPage()) {
                    setCurrentPage(currentPage + 1);
                    playClick();
                }
                return true;
            }

            for (int i = 0; i < Math.min(tabPositions.size(), tabs.size() - currentPage * tabPositions.size()); i++) {
                WidgetPosition pos = tabPositions.get(i);
                Tab tab = tabs.get(currentPage * tabPositions.size() + i);
                if (pos != null && tab != null && tab != currentTab) {
                    if (getTabArea(pos).contains((int) mouseX, (int) mouseY)) {
                        openTab(tab);
                        playClick();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isLocked() || isHidden() || tabPositions.isEmpty()) return false;

        boolean overLeftBtn = getPageButton(true).contains((int) mouseX, (int) mouseY);
        boolean overRightBtn = getPageButton(false).contains((int) mouseX, (int) mouseY);
        boolean overTabs = tabPositions.stream().anyMatch(pos -> getTabArea(pos).contains((int) mouseX, (int) mouseY));

        if (overLeftBtn || overRightBtn) {
            if (verticalAmount > 0 && currentPage > 0) {
                setCurrentPage(currentPage - 1);
                playClick();
                return true;
            } else if (verticalAmount < 0 && currentPage < getMaximumPage()) {
                setCurrentPage(currentPage + 1);
                playClick();
                return true;
            }
        } else if (overTabs) {
            if (tabs.size() > 1) {
                int currentIdx = tabs.indexOf(currentTab);
                if (currentIdx == -1) currentIdx = 0;
                if (verticalAmount < 0) {
                    int nextIdx = (currentIdx + 1) % tabs.size();
                    openTab(tabs.get(nextIdx));
                    return true;
                } else if (verticalAmount > 0) {
                    int prevIdx = (currentIdx - 1 + tabs.size()) % tabs.size();
                    openTab(tabs.get(prevIdx));
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean mouseReleased(double mouseX, double mouseY, int button) {
        return isLocked();
    }

    public static boolean isClickOutsideBounds(double mouseX, double mouseY) {
        return tabPositions.isEmpty() || !getPageButton(true).contains((int) mouseX, (int) mouseY) && !getPageButton(false).contains((int) mouseX, (int) mouseY) && tabPositions.stream().noneMatch(pos -> getTabArea(pos).contains((int) mouseX, (int) mouseY));
    }

    public static boolean keyPressed(KeyEvent event) {
        if (InventoryTabs.TOGGLE_TABS.matches(event)) {
            enabled = !enabled;
            if (!enabled) Minecraft.getInstance().gui.toastManager().addToast(new ControlHintToast(Component.translatable("toast.inventory_tabs.disabled.title").withStyle(ChatFormatting.BOLD), InventoryTabs.TOGGLE_TABS));
        }
        if (isHidden() || isLocked()) return false;
        if (event.hasAltDown() && event.key() >= 49 && event.key() <= 57) { // Keys 1-9
            int index = event.key() - 49;
            if (index < tabs.size()) {
                openTab(tabs.get(index));
                playClick();
                return true;
            }
        }
        if (holdTabCooldown <= 0) {
            boolean isNext = InventoryTabs.NEXT_TAB.matches(event);
            boolean isPrev = InventoryTabs.PREV_TAB != null && InventoryTabs.PREV_TAB.matches(event);
            if (isNext || isPrev) {
                holdTabCooldown = InventoryTabs.CONFIG.holdTabCooldown;
                boolean goBack = isPrev || event.hasShiftDown();
                if (goBack) {
                    if (tabs.indexOf(currentTab) <= 0) {
                        openTab(tabs.get(tabs.size() - 1));
                    } else {
                        openTab(tabs.get(tabs.indexOf(currentTab) - 1));
                    }
                } else {
                    if (tabs.indexOf(currentTab) >= tabs.size() - 1) {
                        openTab(tabs.get(0));
                    } else {
                        openTab(tabs.get(tabs.indexOf(currentTab) + 1));
                    }
                }
                return true;
            }
        }

        return false;
    }

    public static void setCurrentPage(int page) {
        if (page == 0 || tabs.size() >= tabPositions.size()) currentPage = page;
    }

    public static int getMaximumPage() {
        return tabPositions.isEmpty() || tabs.isEmpty() ? 0 : (tabs.size() - 1) / tabPositions.size();
    }

    public static void render(GuiGraphicsExtractor drawContext, double mouseX, double mouseY) {
        if (isHidden() || tabPositions.isEmpty()) return;
        for (int i = 0; i < Math.min(tabPositions.size(), tabs.size() - currentPage * tabPositions.size()); i++) {
            WidgetPosition pos = tabPositions.get(i);
            Tab tab = tabs.get(currentPage * tabPositions.size() + i);
            if (pos != null && tab != null) tab.render(drawContext, pos, TAB_WIDTH, TAB_HEIGHT, mouseX, mouseY, tab == currentTab);
        }
        if (getMaximumPage() > 0) {
            drawButton(drawContext, mouseX, mouseY, true);
            drawButton(drawContext, mouseX, mouseY, false);
        }
    }

    public static Rect2i getPageButton(boolean left) {
        if (tabPositions.isEmpty()) return new Rect2i(0, 0, 0, 0);
        WidgetPosition pos = tabPositions.get(left ? 0 : tabPositions.size() - 1);
        return new Rect2i(pos.x + (left ? -BUTTON_WIDTH : TAB_WIDTH), pos.y - (pos.up ? BUTTON_HEIGHT : 0), BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public static Rect2i getTabArea(WidgetPosition pos) {
        return new Rect2i(pos.x, pos.y + (pos.up ? -TAB_HEIGHT : 0), TAB_WIDTH, TAB_HEIGHT);
    }

    public static void drawButton(GuiGraphicsExtractor drawContext, double mouseX, double mouseY, boolean left) {
        Rect2i rect = getPageButton(left);
        boolean hovered = rect.contains((int) mouseX, (int) mouseY);
        boolean active = left ? currentPage > 0 : currentPage < getMaximumPage();
        int u = BUTTON_WIDTH * (left ? 0 : 1);
        int v = BUTTON_HEIGHT * (active ? hovered ? 2 : 1 : 0);
        drawContext.blit(RenderPipelines.GUI_TEXTURED, BUTTONS_TEXTURE, rect.getX(), rect.getY(), u, v, rect.getWidth(), rect.getHeight(), 256, 256);
        if (hovered) drawContext.setTooltipForNextFrame(Minecraft.getInstance().font, Component.translatable("gui.inventory_tabs.page_indicator", currentPage + 1, getMaximumPage() + 1), (int) mouseX, (int) mouseY);
    }

    public static void playClick() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
    }

    public static boolean isHidden() {
        return !enabled || currentScreen == null;
    }

    public static boolean isLocked() {
        return nextTab != null;
    }
}


