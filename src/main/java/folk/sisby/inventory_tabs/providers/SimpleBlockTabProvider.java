package folk.sisby.inventory_tabs.providers;

import folk.sisby.inventory_tabs.InventoryTabs;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.ConduitBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class SimpleBlockTabProvider extends BlockTabProvider {
    public final Map<Identifier, Predicate<Block>> blacklist = new HashMap<>();

    public SimpleBlockTabProvider() {
        super();
        blacklist.put(InventoryTabs.id("abstract_banner_block"), b -> b instanceof AbstractBannerBlock);
        blacklist.put(InventoryTabs.id("abstract_sign_block"), b -> b instanceof SignBlock);
        blacklist.put(InventoryTabs.id("abstract_skull_block"), b -> b instanceof AbstractSkullBlock);
        blacklist.put(InventoryTabs.id("beehive_block"), b -> b instanceof BeehiveBlock);
        blacklist.put(InventoryTabs.id("bed_block"), b -> b instanceof BedBlock);
        blacklist.put(InventoryTabs.id("bell_block"), b -> b instanceof BellBlock);
        blacklist.put(InventoryTabs.id("campfire_block"), b -> b instanceof CampfireBlock);
        blacklist.put(InventoryTabs.id("command_block"), b -> b instanceof CommandBlock);
        blacklist.put(InventoryTabs.id("comparator_block"), b -> b instanceof ComparatorBlock);
        blacklist.put(InventoryTabs.id("conduit_block"), b -> b instanceof ConduitBlock);
        blacklist.put(InventoryTabs.id("daylight_detector_block"), b -> b instanceof DaylightDetectorBlock);
        blacklist.put(InventoryTabs.id("end_gateway_block"), b -> b instanceof EndGatewayBlock);
        blacklist.put(InventoryTabs.id("end_portal_block"), b -> b instanceof EndPortalBlock);
        blacklist.put(InventoryTabs.id("jigsaw_block"), b -> b instanceof JigsawBlock);
        blacklist.put(InventoryTabs.id("jukebox_block"), b -> b instanceof JukeboxBlock);
        blacklist.put(InventoryTabs.id("piston_extension_block"), b -> b instanceof MovingPistonBlock);
        blacklist.put(InventoryTabs.id("sculk_sensor_block"), b -> b instanceof SculkSensorBlock);
        blacklist.put(InventoryTabs.id("spawner_block"), b -> b instanceof SpawnerBlock);
        blacklist.put(InventoryTabs.id("structure_block"), b -> b instanceof StructureBlock);
        blacklist.put(InventoryTabs.id("lectern_block"), b -> b instanceof LecternBlock);
        blacklist.put(InventoryTabs.id("sculk_catalyst_block"), b -> b instanceof SculkCatalystBlock);
        blacklist.put(InventoryTabs.id("sculk_shrieker_block"), b -> b instanceof SculkShriekerBlock);
        blacklist.put(InventoryTabs.id("chiseled_bookshelf_block"), b -> b instanceof ChiseledBookShelfBlock);
        blacklist.put(InventoryTabs.id("decorated_pot_block"), b -> b instanceof DecoratedPotBlock);
        blacklist.put(InventoryTabs.id("brushable_block"), b -> b instanceof BrushableBlock);
        blacklist.put(InventoryTabs.id("trial_spawner_block"), b -> b instanceof TrialSpawnerBlock);
        matches.put(InventoryTabs.id("block_entity_provider_blacklist"), b -> b instanceof EntityBlock && blacklist.values().stream().noneMatch(p -> p.test(b)));
    }

    @Override
    public int getTabOrderPriority(Level world, BlockPos pos) {
        return 0;
    }

    @Override
    public int getRegistryPriority() {
        return 0;
    }
}
