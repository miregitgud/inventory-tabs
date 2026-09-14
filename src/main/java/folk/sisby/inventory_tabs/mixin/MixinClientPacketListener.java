package folk.sisby.inventory_tabs.mixin;

import folk.sisby.inventory_tabs.TabManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handleContainerClose", at = @At("HEAD"), cancellable = true)
    private void inventoryTabs$handleContainerClose(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        if (TabManager.isLocked()) {
            ci.cancel();
            return;
        }
        if (this.minecraft.player != null && this.minecraft.gui.screen() instanceof AbstractContainerScreen<?> screen) {
            if (screen.getMenu() != null && screen.getMenu().containerId != packet.getContainerId()) {
                ci.cancel();
            }
        }
    }
}
