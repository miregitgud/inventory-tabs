package folk.sisby.inventory_tabs.mixin;

import folk.sisby.inventory_tabs.TabManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinScreenHandler {
    @Unique private boolean inventoryTabs$freshlyConstructed = true;

    @Inject(method = "initializeContents", at = @At("TAIL"))
    public void finishChangingTabs(int revision, List<ItemStack> stacks, ItemStack cursorStack, CallbackInfo ci) {
        if ((revision == 1 || inventoryTabs$freshlyConstructed) && Minecraft.getInstance().player != null) {
            inventoryTabs$freshlyConstructed = false;
            TabManager.finishOpeningScreen((AbstractContainerMenu) (Object) this);
        }
    }
}
