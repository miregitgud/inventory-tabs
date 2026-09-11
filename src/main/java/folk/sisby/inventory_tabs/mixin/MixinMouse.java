package folk.sisby.inventory_tabs.mixin;

import folk.sisby.inventory_tabs.TabManager;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouse {
    @Inject(method = "releaseMouse", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"), cancellable = true)
    public void keepCursorWhenChangingTabs(CallbackInfo ci) {
        if (TabManager.nextTab != null) {
            ci.cancel();
        }
    }
}
