package folk.sisby.inventory_tabs.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShulkerBoxBlock.class)
public interface ShulkerBoxBlockAccessor {
	@Invoker("canOpen")
	static boolean canOpen(BlockState state, Level world, BlockPos pos, ShulkerBoxBlockEntity entity) {
		throw new NotImplementedException();
	}
}
