package folk.sisby.inventory_tabs.mixin;

import folk.sisby.inventory_tabs.TabManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinMinecraftClient {
    @Inject(method = "setScreen", at = @At("HEAD"))
    public void discardNextTabOnScreenClose(Screen screen, CallbackInfo ci) {
        if (screen == null) {
            TabManager.screenDiscarded();
        }
    }
}
