package folk.sisby.inventory_tabs.mixin;

import folk.sisby.inventory_tabs.TabManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraftClient {
    @Inject(method = "setScreenAndShow", at = @At("HEAD"))
    public void discardNextTabOnScreenClose(Screen screen, CallbackInfo ci) {
        if (screen == null || !(screen instanceof AbstractContainerScreen)) {
            TabManager.screenDiscarded();
        }
    }
}
