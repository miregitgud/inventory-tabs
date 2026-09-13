package folk.sisby.inventory_tabs.util;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class BackpackUtil {
    public static boolean isBackpack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null) {
            if ("travelersbackpack".equals(id.getNamespace())) return true;
            if (id.getPath().contains("backpack") || id.getPath().contains("traveler")) return true;
        }
        if (stack.is(TagKey.create(Registries.ITEM, Identifier.parse("c:backpacks")))
                || stack.is(TagKey.create(Registries.ITEM, Identifier.parse("travelersbackpack:backpacks")))
                || stack.is(TagKey.create(Registries.ITEM, Identifier.parse("travelersbackpack:travelers_backpack")))
                || stack.is(TagKey.create(Registries.ITEM, Identifier.parse("c:bags")))) {
            return true;
        }
        String className = stack.getItem().getClass().getName();
        return className.contains("TravelersBackpack") || className.contains("BackpackItem");
    }

    public static ItemStack getEquippedBackpack(Player player) {
        if (player == null) return ItemStack.EMPTY;

        // 1. Check Chest armor slot
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (isBackpack(chest)) return chest;

        // 2. Check Trinkets integration if present
        try {
            Class<?> trinketsApi = Class.forName("dev.emi.trinkets.api.TrinketsApi");
            Method getTrinketComp = trinketsApi.getMethod("getTrinketComponent", LivingEntity.class);
            Optional<?> opt = (Optional<?>) getTrinketComp.invoke(null, player);
            if (opt.isPresent()) {
                Object comp = opt.get();
                Method getAllEquipped = comp.getClass().getMethod("getAllEquipped");
                List<?> list = (List<?>) getAllEquipped.invoke(comp);
                for (Object tuple : list) {
                    for (Method m : tuple.getClass().getMethods()) {
                        if (m.getParameterCount() == 0 && ItemStack.class.isAssignableFrom(m.getReturnType())) {
                            ItemStack s = (ItemStack) m.invoke(tuple);
                            if (isBackpack(s)) return s;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 3. Check Accessories integration if present
        try {
            Class<?> accCap = Class.forName("io.wispforest.accessories.api.AccessoriesCapability");
            Method getCap = accCap.getMethod("get", LivingEntity.class);
            Object cap = getCap.invoke(null, player);
            if (cap != null) {
                Method getEquipped = cap.getClass().getMethod("getEquipped");
                List<?> list = (List<?>) getEquipped.invoke(cap);
                for (Object acc : list) {
                    for (Method m : acc.getClass().getMethods()) {
                        if (m.getParameterCount() == 0 && ItemStack.class.isAssignableFrom(m.getReturnType())) {
                            ItemStack s = (ItemStack) m.invoke(acc);
                            if (isBackpack(s)) return s;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 4. Check Traveler's Backpack CapabilityUtils / BackpackUtils
        try {
            Class<?> capUtils = Class.forName("com.tiviacz.travelersbackpack.capability.CapabilityUtils");
            for (Method m : capUtils.getMethods()) {
                if (m.getParameterCount() == 1 && LivingEntity.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    if (ItemStack.class.isAssignableFrom(m.getReturnType())) {
                        ItemStack s = (ItemStack) m.invoke(null, player);
                        if (isBackpack(s)) return s;
                    }
                }
            }
        } catch (Throwable ignored) {}

        // 5. Check remaining equipment slots
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.CHEST) continue;
            ItemStack item = player.getItemBySlot(slot);
            if (isBackpack(item)) return item;
        }

        return ItemStack.EMPTY;
    }

    public static void openEquippedBackpack(LocalPlayer player) {
        if (player == null) return;

        // 1. Try triggering registered KeyMapping (e.g. Traveler's Backpack "Open Backpack")
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null && mc.options.keyMappings != null) {
            for (KeyMapping keyMapping : mc.options.keyMappings) {
                String name = keyMapping.getName().toLowerCase();
                String cat = keyMapping.getCategory() != null ? keyMapping.getCategory().toString().toLowerCase() : "";
                if (name.contains("travelersbackpack") || cat.contains("travelersbackpack") ||
                        ((name.contains("backpack") || cat.contains("backpack")) && (name.contains("open") || name.contains("gui") || name.contains("inventory") || name.contains("key")))) {
                    triggerKeyMapping(keyMapping);
                    return;
                }
            }
        }

        // 2. Try Traveler's Backpack network payload via reflection
        String[] candidatePacketClasses = new String[] {
                "com.tiviacz.travelersbackpack.network.ServerboundOpenBackpackPacket",
                "com.tiviacz.travelersbackpack.network.OpenBackpackPacket",
                "com.tiviacz.travelersbackpack.network.ServerboundOpenScreenPacket"
        };
        for (String className : candidatePacketClasses) {
            try {
                Class<?> packetClass = Class.forName(className);
                Object packetInstance = null;
                for (Constructor<?> ctor : packetClass.getConstructors()) {
                    if (ctor.getParameterCount() == 0) {
                        packetInstance = ctor.newInstance();
                        break;
                    } else if (ctor.getParameterCount() == 1) {
                        Class<?> pType = ctor.getParameterTypes()[0];
                        if (pType == byte.class || pType == Byte.class) {
                            packetInstance = ctor.newInstance((byte) 0);
                            break;
                        } else if (pType == int.class || pType == Integer.class) {
                            packetInstance = ctor.newInstance(0);
                            break;
                        } else if (pType == boolean.class || pType == Boolean.class) {
                            packetInstance = ctor.newInstance(false);
                            break;
                        }
                    }
                }
                if (packetInstance instanceof CustomPacketPayload payload) {
                    try {
                        Class<?> cpnClass = Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
                        Method sendMethod = cpnClass.getMethod("send", CustomPacketPayload.class);
                        sendMethod.invoke(null, payload);
                        return;
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        }
    }

    private static void triggerKeyMapping(KeyMapping keyMapping) {
        keyMapping.setDown(true);
        try {
            Field clickCount = KeyMapping.class.getDeclaredField("clickCount");
            clickCount.setAccessible(true);
            clickCount.setInt(keyMapping, clickCount.getInt(keyMapping) + 1);
        } catch (Throwable ignored) {}
        keyMapping.setDown(false);
    }
}
