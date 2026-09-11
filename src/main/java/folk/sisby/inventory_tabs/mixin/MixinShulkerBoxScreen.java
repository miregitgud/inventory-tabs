package folk.sisby.inventory_tabs.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxScreen.class)
public abstract class MixinShulkerBoxScreen extends AbstractContainerScreen<ChestMenu> {
    @Shadow @Final private static Identifier CONTAINER_TEXTURE;

    public MixinShulkerBoxScreen(ChestMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void containerTextHeight(ShulkerBoxMenu handler, Inventory inventory, Component title, CallbackInfo ci) {
        this.imageHeight -= 1;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    public void containerHeader(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        drawContext.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, (this.width - this.imageWidth) / 2, (this.height - this.imageHeight) / 2, 0f, 0f, this.imageWidth, 7, 256, 256);
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 0), index = 3)
    public int containerY(int original) {
        return original + 7;
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 0), index = 5)
    public float containerV(float original) {
        return original + 8f;
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 0), index = 7)
    public int containerHeight(int original) {
        return 64;
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    public void containerInventory(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        drawContext.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, (this.width - this.imageWidth) / 2, (this.height - this.imageHeight) / 2 + 71, 0f, 71f, this.imageWidth, 96, 256, 256);
    }
}
