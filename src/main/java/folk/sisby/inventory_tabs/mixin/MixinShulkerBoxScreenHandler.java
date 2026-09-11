package folk.sisby.inventory_tabs.mixin;

import net.minecraft.world.inventory.ShulkerBoxMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ShulkerBoxMenu.class)
public abstract class MixinShulkerBoxScreenHandler {
    @ModifyArg(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ShulkerBoxSlot;<init>(Lnet/minecraft/world/Container;III)V"), index = 3)
    public int raiseContainerSlotY(int original) {
        return original - 1;
    }
}
