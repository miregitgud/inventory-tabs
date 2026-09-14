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
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BackpackUtil {
    private static final Set<String> MISSING_CLASSES = ConcurrentHashMap.newKeySet();
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Field CLICK_COUNT_FIELD;

    static {
        Field f = null;
        try {
            f = KeyMapping.class.getDeclaredField("clickCount");
            f.setAccessible(true);
        } catch (Throwable ignored) {}
        CLICK_COUNT_FIELD = f;
    }

    private static Class<?> findClass(String name) {
        if (MISSING_CLASSES.contains(name)) return null;
        Class<?> cached = CLASS_CACHE.get(name);
        if (cached != null) return cached;
        try {
            Class<?> clazz = Class.forName(name);
            CLASS_CACHE.put(name, clazz);
            return clazz;
        } catch (Throwable t) {
            MISSING_CLASSES.add(name);
            return null;
        }
    }
    public static boolean isBackpack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id != null) {
            String ns = id.getNamespace().toLowerCase();
            String path = id.getPath().toLowerCase();
            if (ns.contains("travelersbackpack") || ns.contains("backpack") || ns.contains("inmis") || ns.contains("sophisticatedbackpacks") || ns.contains("beansbackpacks")) {
                return true;
            }
            if (path.contains("backpack") || path.contains("traveler") || path.contains("pack") || path.contains("bag")) {
                return true;
            }
        }
        if (stack.is(TagKey.create(Registries.ITEM, Identifier.parse("c:backpacks")))
                || stack.is(TagKey.create(Registries.ITEM, Identifier.parse("c:bags")))
                || stack.is(TagKey.create(Registries.ITEM, Identifier.parse("travelersbackpack:backpacks")))
                || stack.is(TagKey.create(Registries.ITEM, Identifier.parse("travelersbackpack:travelers_backpack")))) {
            return true;
        }
        String className = stack.getItem().getClass().getName().toLowerCase();
        return className.contains("travelersbackpack") || className.contains("backpack");
    }

    public static ItemStack getEquippedBackpack(Player player) {
        if (player == null) return ItemStack.EMPTY;

        // 1. Check Chest armor slot
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (isBackpack(chest)) return chest;

        // 2. Check all equipment slots
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.CHEST) continue;
            ItemStack item = player.getItemBySlot(slot);
            if (isBackpack(item)) return item;
        }

        // 3. Check Traveler's Backpack helper classes & capabilities
        ItemStack tbStack = getFromTravelersBackpackUtils(player);
        if (!tbStack.isEmpty()) return tbStack;

        // 4. Check Cardinal Components on player
        ItemStack ccaStack = getFromCardinalComponents(player);
        if (!ccaStack.isEmpty()) return ccaStack;

        // 5. Check Trinkets integration
        ItemStack trinketStack = getFromTrinkets(player);
        if (!trinketStack.isEmpty()) return trinketStack;

        // 6. Check Accessories integration
        ItemStack accStack = getFromAccessories(player);
        if (!accStack.isEmpty()) return accStack;

        // 7. Check Curios integration
        ItemStack curiosStack = getFromCurios(player);
        if (!curiosStack.isEmpty()) return curiosStack;

        return ItemStack.EMPTY;
    }

    private static ItemStack extractBackpackFromObject(Object obj) {
        if (obj == null) return ItemStack.EMPTY;
        if (obj instanceof ItemStack stack) {
            return isBackpack(stack) ? stack : ItemStack.EMPTY;
        }
        if (obj instanceof Optional<?> opt) {
            if (opt.isPresent()) {
                return extractBackpackFromObject(opt.get());
            }
            return ItemStack.EMPTY;
        }
        try {
            // Inspect 0-parameter methods
            for (Method m : obj.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && !Modifier.isStatic(m.getModifiers()) && m.getDeclaringClass() != Object.class) {
                    String name = m.getName().toLowerCase();
                    if (name.contains("backpack") || name.contains("stack") || name.contains("item") || name.contains("wrapper") || name.contains("container") || name.contains("inventory") || name.contains("get")) {
                        try {
                            Object res = m.invoke(obj);
                            if (res instanceof ItemStack s && isBackpack(s)) {
                                return s;
                            }
                            if (res != null && res != obj && !(res instanceof Number) && !(res instanceof Boolean) && !(res instanceof String)) {
                                if (res instanceof Optional<?> o && o.isPresent()) {
                                    ItemStack s = extractBackpackFromObject(o.get());
                                    if (!s.isEmpty()) return s;
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }
            // Inspect fields
            for (Field f : obj.getClass().getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    Object val = f.get(obj);
                    if (val instanceof ItemStack s && isBackpack(s)) {
                        return s;
                    }
                }
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    private static ItemStack getFromTravelersBackpackUtils(Player player) {
        String[] candidateClasses = new String[]{
                "com.tiviacz.travelersbackpack.capability.CapabilityUtils",
                "com.tiviacz.travelersbackpack.util.CapabilityUtils",
                "com.tiviacz.travelersbackpack.util.BackpackUtils",
                "com.tiviacz.travelersbackpack.inventory.BackpackWrapper",
                "com.tiviacz.travelersbackpack.component.ModComponents",
                "com.tiviacz.travelersbackpack.common.BackpackAbilities",
                "com.tiviacz.travelersbackpack.compat.trinkets.TrinketsUtils",
                "com.tiviacz.travelersbackpack.compat.accessories.AccessoriesUtils",
                "com.tiviacz.travelersbackpack.compat.curios.CuriosUtils"
        };
        for (String className : candidateClasses) {
            try {
                Class<?> clazz = findClass(className);
                if (clazz == null) continue;
                for (Method m : clazz.getMethods()) {
                    if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 1) {
                        Class<?> paramType = m.getParameterTypes()[0];
                        if (paramType.isAssignableFrom(player.getClass()) || paramType == LivingEntity.class || paramType == Player.class) {
                            try {
                                Object result = m.invoke(null, player);
                                ItemStack stack = extractBackpackFromObject(result);
                                if (!stack.isEmpty()) return stack;
                            } catch (Throwable ignored) {}
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack getFromCardinalComponents(Player player) {
        try {
            for (Method m : player.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && m.getName().toLowerCase().contains("component")) {
                    try {
                        Object container = m.invoke(player);
                        if (container != null) {
                            if (container instanceof Map<?, ?> map) {
                                for (Object val : map.values()) {
                                    ItemStack s = extractBackpackFromObject(val);
                                    if (!s.isEmpty()) return s;
                                }
                            } else if (container instanceof Iterable<?> iterable) {
                                for (Object val : iterable) {
                                    ItemStack s = extractBackpackFromObject(val);
                                    if (!s.isEmpty()) return s;
                                }
                            } else {
                                ItemStack s = extractBackpackFromObject(container);
                                if (!s.isEmpty()) return s;
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    private static ItemStack getFromTrinkets(Player player) {
        try {
            Class<?> trinketsApi = findClass("dev.emi.trinkets.api.TrinketsApi");
            if (trinketsApi == null) return ItemStack.EMPTY;
            Method getTrinketComp = trinketsApi.getMethod("getTrinketComponent", LivingEntity.class);
            Optional<?> opt = (Optional<?>) getTrinketComp.invoke(null, player);
            if (opt != null && opt.isPresent()) {
                Object comp = opt.get();
                Method getAllEquipped = comp.getClass().getMethod("getAllEquipped");
                Object res = getAllEquipped.invoke(comp);
                if (res instanceof Collection<?> list) {
                    for (Object tuple : list) {
                        ItemStack s = extractBackpackFromObject(tuple);
                        if (!s.isEmpty()) return s;
                    }
                }
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    private static ItemStack getFromAccessories(Player player) {
        try {
            Class<?> accCap = findClass("io.wispforest.accessories.api.AccessoriesCapability");
            if (accCap == null) return ItemStack.EMPTY;
            Method getCap = accCap.getMethod("get", LivingEntity.class);
            Object cap = getCap.invoke(null, player);
            if (cap != null) {
                for (Method m : cap.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && (m.getName().contains("Equipped") || m.getName().contains("Containers"))) {
                        try {
                            Object res = m.invoke(cap);
                            if (res instanceof Collection<?> list) {
                                for (Object entry : list) {
                                    ItemStack s = extractBackpackFromObject(entry);
                                    if (!s.isEmpty()) return s;
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    private static ItemStack getFromCurios(Player player) {
        try {
            Class<?> curiosApi = findClass("top.theillusivec4.curios.api.CuriosApi");
            if (curiosApi == null) return ItemStack.EMPTY;
            Method getHelper = curiosApi.getMethod("getCuriosHelper");
            Object helper = getHelper.invoke(null);
            if (helper != null) {
                for (Method m : helper.getClass().getMethods()) {
                    if (m.getParameterCount() == 1 && LivingEntity.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        try {
                            Object res = m.invoke(helper, player);
                            ItemStack s = extractBackpackFromObject(res);
                            if (!s.isEmpty()) return s;
                        } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    public static void openEquippedBackpack(LocalPlayer player) {
        if (player == null) return;

        // 1. Try Traveler's Backpack network payload via reflection
        String[] candidatePacketClasses = new String[] {
                "com.tiviacz.travelersbackpack.network.ServerboundOpenScreenPacket",
                "com.tiviacz.travelersbackpack.network.ServerboundOpenBackpackPacket",
                "com.tiviacz.travelersbackpack.network.OpenBackpackPacket",
                "com.tiviacz.travelersbackpack.network.client.ServerboundOpenBackpackPacket",
                "com.tiviacz.travelersbackpack.network.client.ServerboundOpenScreenPacket",
                "com.tiviacz.travelersbackpack.network.ScreenPacket"
        };
        for (String className : candidatePacketClasses) {
            try {
                Class<?> packetClass = findClass(className);
                if (packetClass == null) continue;
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
                    } else if (ctor.getParameterCount() == 2) {
                        Class<?>[] pTypes = ctor.getParameterTypes();
                        if ((pTypes[0] == byte.class || pTypes[0] == Byte.class) && (pTypes[1] == byte.class || pTypes[1] == Byte.class)) {
                            packetInstance = ctor.newInstance((byte) 0, (byte) 0);
                            break;
                        }
                    }
                }
                if (packetInstance instanceof CustomPacketPayload payload) {
                    try {
                        Class<?> cpnClass = findClass("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
                        if (cpnClass != null) {
                            Method sendMethod = cpnClass.getMethod("send", CustomPacketPayload.class);
                            sendMethod.invoke(null, payload);
                            return;
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        }

        // 2. Try Traveler's Backpack network dispatcher methods
        String[] networkClasses = new String[] {
                "com.tiviacz.travelersbackpack.network.ModNetwork",
                "com.tiviacz.travelersbackpack.network.PacketHandler",
                "com.tiviacz.travelersbackpack.network.NetworkHandler"
        };
        for (String netClass : networkClasses) {
            try {
                Class<?> clazz = findClass(netClass);
                if (clazz == null) continue;
                for (Method m : clazz.getMethods()) {
                    if (Modifier.isStatic(m.getModifiers()) && m.getName().toLowerCase().contains("open")) {
                        if (m.getParameterCount() == 0) {
                            m.invoke(null);
                            return;
                        } else if (m.getParameterCount() == 1 && (m.getParameterTypes()[0] == byte.class || m.getParameterTypes()[0] == int.class)) {
                            m.invoke(null, 0);
                            return;
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        // 3. Try triggering registered KeyMapping (e.g. Traveler's Backpack "Open Backpack")
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
    }

    private static void triggerKeyMapping(KeyMapping keyMapping) {
        keyMapping.setDown(true);
        try {
            if (CLICK_COUNT_FIELD != null) {
                CLICK_COUNT_FIELD.setInt(keyMapping, CLICK_COUNT_FIELD.getInt(keyMapping) + 1);
            }
        } catch (Throwable ignored) {}
        keyMapping.setDown(false);
    }
}
