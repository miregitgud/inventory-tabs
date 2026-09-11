package folk.sisby.inventory_tabs.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public class MixinBlockEntity {
    @Inject(method = "getUpdateTag", at = @At("RETURN"))
    public void sendCustomNames(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        if (((BlockEntity) (Object) this) instanceof BaseContainerBlockEntity lcbe && lcbe.getCustomName() != null) {
            ComponentSerialization.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), lcbe.getCustomName())
                    .ifSuccess(tag -> cir.getReturnValue().put("CustomName", tag));
        }
    }
}
