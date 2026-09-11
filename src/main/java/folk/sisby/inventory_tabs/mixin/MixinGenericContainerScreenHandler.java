package folk.sisby.inventory_tabs.mixin;

import folk.sisby.inventory_tabs.InventoryTabs;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChestMenu.class)
public abstract class MixinGenericContainerScreenHandler {
    @Shadow @Final private int containerRows;

    @ModifyArg(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ChestMenu;addChestGrid(Lnet/minecraft/world/Container;II)V"), index = 2)
    public int raiseContainerSlotY(int original) {
        if (containerRows == 6 && InventoryTabs.CONFIG.compactLargeContainers) return original - 10;
        return original - 1;
    }

    @ModifyArg(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ChestMenu;addStandardInventorySlots(Lnet/minecraft/world/Container;II)V"), index = 2)
    public int raiseInventorySlotY(int original) {
        if (containerRows == 6 && InventoryTabs.CONFIG.compactLargeContainers) return original - 19;
        return original - 1;
    }
}
