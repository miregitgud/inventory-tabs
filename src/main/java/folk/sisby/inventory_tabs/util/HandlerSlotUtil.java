package folk.sisby.inventory_tabs.util;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

public class HandlerSlotUtil {
    public static int stashSlot = -1;
    public static int mainHandSwapSlot = -1;

    public static void push(LocalPlayer player, MultiPlayerGameMode manager, AbstractContainerMenu handler, boolean doClient) {
        if (!handler.getCarried().isEmpty()) {
            stashSlot = player.getInventory().getFreeSlot();
            if (stashSlot != -1) {
                handler.findSlot(player.getInventory(), stashSlot).ifPresent((screenSlot) -> {
                    manager.handleContainerInput(
                            handler.containerId,
                            screenSlot,
                            0,
                            ContainerInput.PICKUP,
                            player
                    );
                });
            }
        }
    }

    public static void tryPop(LocalPlayer player, MultiPlayerGameMode manager, AbstractContainerMenu handler) {
        if (stashSlot != -1) {
            handler.findSlot(player.getInventory(), stashSlot).ifPresent((screenSlot) -> manager.handleContainerInput(
                    handler.containerId,
                    screenSlot,
                    0, // Mouse Left Click
                    ContainerInput.PICKUP,
                    player
            ));
            stashSlot = -1;
        }
        if (mainHandSwapSlot != -1) {
            handler.findSlot(player.getInventory(), mainHandSwapSlot).ifPresent((screenSlot) -> manager.handleContainerInput(
                    handler.containerId,
                    screenSlot,
                    player.getInventory().selected,
                    ContainerInput.SWAP,
                    player
            ));
            mainHandSwapSlot = -1;
        }
    }
}
