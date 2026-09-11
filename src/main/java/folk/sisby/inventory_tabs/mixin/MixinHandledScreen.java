package folk.sisby.inventory_tabs.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import folk.sisby.inventory_tabs.InventoryTabs;
import folk.sisby.inventory_tabs.ScreenSupport;
import folk.sisby.inventory_tabs.TabManager;
import folk.sisby.inventory_tabs.duck.InventoryTabsScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinHandledScreen extends Screen implements InventoryTabsScreen {
    @Unique Boolean inventoryTabs$allowTabs = false;

    protected MixinHandledScreen(Component title) {
        super(title);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V", at = @At("TAIL"))
    private void checkSupported(AbstractContainerMenu handler, Inventory inventory, Component title, int imageWidth, int imageHeight, CallbackInfo ci) {
        inventoryTabs$allowTabs = ScreenSupport.allowTabs(this);
    }
    
    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo callbackInfo) {
        if (!inventoryTabs$allowTabs) return;
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        TabManager.initScreen(minecraft, self);
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    protected void render(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!inventoryTabs$allowTabs) return;
        TabManager.render(drawContext, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!inventoryTabs$allowTabs) return;
        if (TabManager.mouseClicked(event.x(), event.y(), event.button())) {
            callbackInfo.setReturnValue(true);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void mouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!inventoryTabs$allowTabs) return;
        if (TabManager.mouseReleased(event.x(), event.y(), event.button())) {
            callbackInfo.setReturnValue(true);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (!inventoryTabs$allowTabs) return;
        if (TabManager.keyPressed(event)) {
            callbackInfo.setReturnValue(true);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "hasClickedOutside", at = @At("RETURN"), cancellable = true)
    protected void isClickOutsideBounds(double mouseX, double mouseY, int left, int top, CallbackInfoReturnable<Boolean> cir) {
        if (inventoryTabs$allowTabs && cir.getReturnValue()) {
            cir.setReturnValue(TabManager.isClickOutsideBounds(mouseX, mouseY));
        }
    }

	@ModifyExpressionValue(method = "<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getDisplayName()Lnet/minecraft/network/chat/Component;"))
	private Component removeCompactPlayerInventoryTitle(Component original) {
		AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
		if (InventoryTabs.CONFIG.compactLargeContainers && self.getMenu() instanceof ChestMenu gcsh && gcsh.getRowCount() == 6) {
			return Component.empty();
		}
		return original;
	}

    @Override
    public boolean inventoryTabs$allowTabs() {
        return inventoryTabs$allowTabs;
    }
}
