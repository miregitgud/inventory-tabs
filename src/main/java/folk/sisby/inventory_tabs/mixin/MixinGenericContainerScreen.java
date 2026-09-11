package folk.sisby.inventory_tabs.mixin;

import folk.sisby.inventory_tabs.InventoryTabs;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ContainerScreen.class)
public abstract class MixinGenericContainerScreen extends AbstractContainerScreen<ChestMenu> {
    @Shadow @Final private int containerRows;
    @Shadow @Final private static Identifier CONTAINER_BACKGROUND;

    public MixinGenericContainerScreen(ChestMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

	@ModifyArgs(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V"))
	private static void containerHideTitle(Args args) {
		if (((ChestMenu) args.get(0)).getRowCount() == 6 && InventoryTabs.CONFIG.compactLargeContainers) {
			args.set(2, Component.empty());
		}
	}

    @Inject(method = "<init>", at = @At("TAIL"))
    public void containerTextHeight(ChestMenu handler, Inventory inventory, Component title, CallbackInfo ci) {
        if (containerRows == 6 && InventoryTabs.CONFIG.compactLargeContainers) {
            ((HandledScreenAccessor) this).setImageHeight(this.imageHeight - 30);
        } else {
            ((HandledScreenAccessor) this).setImageHeight(this.imageHeight - 2);
            this.inventoryLabelY = this.imageHeight - 94;
        }
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    public void containerHeader(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        drawContext.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, (this.width - this.imageWidth) / 2, (this.height - this.imageHeight) / 2, 0f, 0f, this.imageWidth, 7, 256, 256);
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 0), index = 3)
    public int containerY(int original) {
        return original + 7;
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 0), index = 5)
    public float containerV(float original) {
        if (containerRows == 6 && InventoryTabs.CONFIG.compactLargeContainers) return original + 17f;
        return original + 8f;
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 0), index = 7)
    public int containerHeight(int original) {
        if (containerRows == 6 && InventoryTabs.CONFIG.compactLargeContainers) return original - 17;
        return original - 8;
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 1), index = 3)
    public int inventoryY(int original) {
        if (containerRows == 6 && InventoryTabs.CONFIG.compactLargeContainers) return original - 10;
        return original - 1;
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 1), index = 5)
    public float inventoryV(float original) {
        if (containerRows == 6 && InventoryTabs.CONFIG.compactLargeContainers) return original + 9f;
        return original;
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 1), index = 7)
    public int inventoryHeight(int original) {
        if (containerRows == 6 && InventoryTabs.CONFIG.compactLargeContainers) return original - 9;
        return original;
    }
}
