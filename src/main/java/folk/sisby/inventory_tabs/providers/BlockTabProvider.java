package folk.sisby.inventory_tabs.providers;

import folk.sisby.inventory_tabs.InventoryTabs;
import folk.sisby.inventory_tabs.tabs.BlockTab;
import folk.sisby.inventory_tabs.tabs.Tab;
import folk.sisby.inventory_tabs.util.BlockUtil;
import folk.sisby.inventory_tabs.util.PlayerUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public abstract class BlockTabProvider extends RegistryTabProvider<Block> {
    public final Map<Identifier, BiPredicate<Level, BlockPos>> preclusions = new HashMap<>();

    public BlockTabProvider() {
        preclusions.put(InventoryTabs.id("player_in_range"), (w, p) -> Minecraft.getInstance().player != null && !PlayerUtil.inRange(Minecraft.getInstance().player, p));
    }

    @Override
    public void addAvailableTabs(LocalPlayer player, Consumer<Tab> addTab) {
        Level world = player.level();
        Set<Block> blocksAdded = new HashSet<>();
        for (BlockPos pos : BlockUtil.getBlocksInRadius(player.blockPosition(), PlayerUtil.REACH)) {
            Block block = world.getBlockState(pos).getBlock();
            if (values.contains(block) && preclusions.values().stream().noneMatch(p -> p.test(world, pos))) {
                if (isUnique() && !blocksAdded.add(block)) continue;
                addTab.accept(createTab(world, pos));
            }
        }
    }

    public Tab createTab(Level world, BlockPos pos) {
        return new BlockTab(world, pos, preclusions, getTabOrderPriority(world, pos), isUnique());
    }

    public abstract int getTabOrderPriority(Level world, BlockPos pos);
}
