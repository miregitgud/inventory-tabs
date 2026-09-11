package folk.sisby.inventory_tabs.tabs;

import folk.sisby.inventory_tabs.InventoryTabs;
import folk.sisby.inventory_tabs.util.BlockUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockTab implements Tab {
    public final int priority;
    public final Block block;
    public final BlockPos pos;
    public final boolean unique;
    public final Map<Identifier, BiPredicate<Level, BlockPos>> preclusions;
    public List<BlockPos> multiblockPositions;
    public ItemStack itemStack;
    public Component hoverText;

    public BlockTab(Level world, BlockPos pos, Map<Identifier, BiPredicate<Level, BlockPos>> preclusions, int priority, boolean unique) {
        this.priority = priority;
        this.unique = unique;
        this.block = world.getBlockState(pos).getBlock();
        this.pos = pos;
        this.preclusions = preclusions;
        this.multiblockPositions = new ArrayList<>(List.of(pos));
        refreshMultiblock(world);
        refreshPreview(world);
    }

    @Override
    public void open(LocalPlayer player, ClientLevel world, AbstractContainerMenu handler, MultiPlayerGameMode interactionManager) {
        if (InventoryTabs.CONFIG.rotatePlayer) player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(pos));
        interactionManager.useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.EAST, pos, false));
    }

    @Override
    public boolean shouldBeRemoved(Level world, boolean current) {
        if (!world.getBlockState(pos).getBlock().equals(block)) return true;
        refreshMultiblock(world);
        refreshPreview(world);
        if (current) return false;
        return preclusions.values().stream().anyMatch(p -> p.test(world, pos));
    }

    @Override
    public ItemStack getTabIcon() {
        return itemStack;
    }

    @Override
    public Component getHoverText() {
        return hoverText;
    }

    protected void refreshPreviewAtPos(Level world, BlockPos previewPos) {
        List<ItemFrame> itemFrames = world.getEntitiesOfClass(ItemFrame.class, new AABB(Vec3.atCenterOf(previewPos), Vec3.atCenterOf(previewPos)).inflate(0.6, 0.3, 0.6));
        if (!itemFrames.isEmpty()) {
            itemStack = itemFrames.get(0).getItem();
            if (!itemStack.getHoverName().equals(itemStack.getItem().getName(itemStack))) hoverText = itemStack.getHoverName().copy().withStyle(ChatFormatting.ITALIC);
        }
        if (world.getBlockEntity(previewPos) instanceof BaseContainerBlockEntity lcbe && lcbe.hasCustomName()) {
            hoverText = lcbe.getCustomName().copy().withStyle(ChatFormatting.ITALIC);
        }
        List<SignBlockEntity> signs = BlockUtil.getAttachedBlocks(world, previewPos, (w, p) -> w.getBlockEntity(p) instanceof SignBlockEntity sbe ? sbe : null);
        if (!signs.isEmpty()) {
            String name = Arrays.stream(signs.get(0).getFrontText().getMessages(false)).map(Component::getString).filter(s -> !s.isBlank()).collect(Collectors.joining(" "));
            if (!name.isBlank()) hoverText = Component.literal(name).withStyle(ChatFormatting.ITALIC);
        }
    }

    protected void refreshPreview(Level world) {
        itemStack = new ItemStack(block);
        hoverText = getDefaultHoverText(world);
        for (BlockPos multiPos : multiblockPositions) {
            refreshPreviewAtPos(world, multiPos);
        }
    }

    protected Component getDefaultHoverText(Level world) {
        return block.getName();
    }

    protected void refreshMultiblock(Level world) {
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (unique) {
            return other instanceof ItemTab it && Objects.equals(block.asItem(), it.stack.getItem()) ||
                    other instanceof BlockTab bt && Objects.equals(block, bt.block);
        } else {
            return other instanceof BlockTab bt && Objects.equals(multiblockPositions.get(0), bt.multiblockPositions.get(0));
        }
    }
}
