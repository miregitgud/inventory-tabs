package folk.sisby.inventory_tabs.tabs;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class VehicleInventoryTab extends EntityTab {
    public VehicleInventoryTab(Entity entity, Map<Identifier, Predicate<Entity>> preclusions) {
        super(90, entity, preclusions, false);
    }

    @Override
    public void open(LocalPlayer player, ClientLevel world, AbstractContainerMenu handler, MultiPlayerGameMode interactionManager) {
        player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY));
    }
}
