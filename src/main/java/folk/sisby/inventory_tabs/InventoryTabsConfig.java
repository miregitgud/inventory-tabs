package folk.sisby.inventory_tabs;

import folk.sisby.kaleido.api.WrappedConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.IntegerRange;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;

import java.util.Map;

public class InventoryTabsConfig extends WrappedConfig {
    @Comment("How many ticks to wait before moving to the next tab when the keybind is held")
    @Comment("Used to smooth out the visual difference between instant screens (like the player inventory) and ping-dependent screens")
    @Comment("Can be lowered for shenanigans")
    public int holdTabCooldown = 3;

    @Comment("Whether to allow interacting with entities through blocks")
    @Comment("More seamless, but may be considered cheating by the server")
    public boolean ignoreWalls = true;

    @Comment("Whether to rotate the player towards the clicked tab")
    @Comment("Less seamless, but trips anti-cheat less often")
    public boolean rotatePlayer = false;

    @Comment("Remove the 1 unit padding in container screens them slightly taller and offset from other screens")
    @Comment("If you're seeing stacks in the wrong places (offset up by 1 unit) on modded containers, turn this off")
    public boolean consistentContainers = true;

    @Comment("Strip titles from 6-row container screens to fit them onto minimum-ratio viewports")
    public boolean compactLargeContainers = false;

    @Comment("Emits helpful information for setting up this config when joining a world")
    @Comment("Logs all registered screen handler IDs for use in screen overrides")
    @Comment("Logs all registry tab provider contents (blocks etc) to help find bad tabs")
    public boolean configLogging = true;

    @Comment("Whether to log vanilla tab provider contents")
    @Comment("For development purposes, not modpack configuration")
    public boolean configLoggingVanilla = false;

    @Comment("Whether to show tabs on screens that aren't specified below")
    public boolean allowScreensByDefault = true;

    @Comment("Whether to show tabs on the bottom of screens that aren't specified below")
    public boolean invertTabsByDefault = false;

    @Comment("How many ticks to keep a tab on-screen for when its block is obstructed.")
    @Comment("Prevents tab 'flickering' with specific setups - raising to 4 should be plenty.")
    @IntegerRange(min = 0, max = 5)
    public int blockRaycastTimeout = 0;

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("Manually set whether tabs should be shown on a given screen")
    @Comment("")
    public Map<String, Boolean> screenOverrides = ValueMap.builder(true)
            .put("minecraft:smoker", true)
            .put("fwaystones:waystone", false)
            .put("waystones:waystone", false)
            .build();

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("Manually set the left bound offset for a given screen")
    @Comment("Positive values expand the left boundary, allowing more tabs to be drawn")
    @Comment("null means the player inventory")
    @Comment("")
    public Map<String, Integer> leftBoundOffsetOverride = ValueMap.builder(0).put("minecraft:loom", 0).build();

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("Manually set the right bound offset for a given screen")
    @Comment("Positive values expand the right boundary, allowing more tabs to be drawn")
    @Comment("null means the player inventory")
    @Comment("")
    public Map<String, Integer> rightBoundOffsetOverride = ValueMap.builder(0).put("minecraft:loom", 0).build();

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("Manually choose where to place tabs on a given screen")
    @Comment("false means above, true means below")
    @Comment("null key means the player inventory")
    @Comment("")
    public Map<String, Boolean> invertedTabsOverride = ValueMap.builder(false).put("minecraft:beacon", false).build();

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("Manually set the Tab Provider for a given block or tag")
    @Comment("| -Provider ID-        | -Tab Behaviour-                            | -Default Blocks Added-")
    @Comment("| block_shulker_box    | Checks shulker blocked state               | Adds all ShulkerBoxBlocks")
    @Comment("| block_ender_chest    | Unique, checks chest blocked state         | Adds all EnderChestBlocks")
    @Comment("| block_chest          | L/R Multiblock, checks chest blocked state | Adds all AbstractChestBlocks")
    @Comment("| block_unique         | Only one tab can exist per block type      | Adds all CraftingTableBlocks, AnvilBlocks, CartographyTableBlocks, GrindstoneBlocks, LoomBlocks, and StonecutterBlocks")
    @Comment("| block_simple_storage | Sorted alongside other chest-like tabs     | Adds all BarrelBlocks")
    @Comment("| block_simple         | No special logic                           | Adds all BlockWithEntities, except 25 known-bad vanilla block classes - this adds lots of invalid modded blocks!")
    @Comment("| \"\"                   | No tab at all!                             | Use this to disable bad block tabs in your pack")
    @Comment("")
    public Map<String, String> blockProviderOverrides = ValueMap.builder("")
            .put("minecraft:crafting_table", "inventory_tabs:block_unique")
            .put("#minecraft:doors", "")
            .put("minecraft:fletching_table", "")
            .put("farmersdelight:cutting_board", "")
            .put("#farmersdelight:cutting_boards", "")
            .put("create:depot", "")
            .put("create:basin", "")
            .put("create:chute", "")
            .put("create:smart_chute", "")
            .put("create:deployer", "")
            .put("create:mechanical_arm", "")
            .put("waystones:waystone", "")
            .put("fwaystones:waystone", "")
            .put("waystones:sharestone", "")
            .put("waystones:warp_plate", "")
            .put("comforts:sleeping_bag*", "")
            .put("comforts:hammock*", "")
            .put("supplementaries:pedestal", "")
            .put("supplementaries:notice_board", "")
            .put("supplementaries:globe", "")
            .put("supplementaries:sign_post", "")
            .put("#c:crafting_tables", "inventory_tabs:block_unique")
            .put("#c:chests", "inventory_tabs:block_chest")
            .put("#c:wooden_chests", "inventory_tabs:block_chest")
            .put("#c:barrels", "inventory_tabs:block_simple_storage")
            .put("#c:shulker_boxes", "inventory_tabs:block_shulker_box")
            .build();

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("Manually set the Tab Provider for a given entity or tag")
    @Comment("| -Provider ID- | -Tab Behaviour-                          | -Default Entities Added-")
    @Comment("| entity_sneak  | Uses sneak-interact, checks tamed/riding | Adds all RideableOpenableInventories")
    @Comment("| entity_simple | Checks player isn't riding the entity    | Adds all StorageMinecartEntities")
    @Comment("| \"\"            | No tab at all!                           | Use this to disable bad entity tabs in your pack")
    @Comment("")
    public Map<String, String> entityProviderOverrides = ValueMap.builder("")
            .put("minecraft:chest_minecart", "inventory_tabs:entity_simple")
            .build();

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("Manually set the Tab Provider for a given item or tag")
    @Comment("| -Provider ID- | -Tab Behaviour-                      | -Default Items Added-")
    @Comment("| item_unique   | Only one tab can exist per item type | Adds nothing")
    @Comment("| item_simple   | No special logic                     | Adds nothing")
    @Comment("| \"\"            | No tab at all!                       | Use this to disable bad item tabs in your pack")
    @Comment("")
    public Map<String, String> itemProviderOverrides = ValueMap.builder("")
            .put("minecraft:dirt", "")
            .build();

    @Comment("")
    @Comment("-------------------------------")
    @Comment("")
    @Comment("Enable or disable the default added logic for each tab provider (see above)")
    @Comment("Non-registry Tab Providers like player_inventory and vehicle_inventory can't be disabled here")
    @Comment("")
    public Map<String, Boolean> registryProviderDefaults = ValueMap.builder(true)
            .put("inventory_tabs:block_simple", true)
            .build();
}
