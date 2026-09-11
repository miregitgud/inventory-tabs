package folk.sisby.inventory_tabs.mixin;

import java.util.Map;
import net.minecraft.client.KeyMapping;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.platform.InputConstants;
import folk.sisby.inventory_tabs.InventoryTabs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class MixinKeyBinding {
	@Shadow private int clickCount;
	@Shadow private InputConstants.Key key;

	@Shadow @Final private static Map<String, KeyMapping> ALL;
	@Unique private static final Multimap<InputConstants.Key, KeyMapping> KEYS_TO_BINDINGS = ArrayListMultimap.create();

	@Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;)V", at = @At("TAIL"))
	private void saveConflictedBinds(String translationKey, InputConstants.Type type, int code, KeyMapping.Category category, CallbackInfo ci) {
		KEYS_TO_BINDINGS.put(key, (KeyMapping) (Object) this);
	}

	@Inject(method = "click", at = @At("HEAD"), cancellable = true)
	private static void allowTabConflictedOnKeyPressed(InputConstants.Key key, CallbackInfo ci) {
		if (!key.equals(((MixinKeyBinding) (Object) InventoryTabs.NEXT_TAB).key)) return;
		for (KeyMapping bind : KEYS_TO_BINDINGS.get(key)) {
			((MixinKeyBinding) (Object) bind).clickCount++;
		}
		ci.cancel();
	}

	@Inject(method = "set", at = @At("HEAD"), cancellable = true)
	private static void allowTabConflictedSetKeyPressed(InputConstants.Key key, boolean pressed$, CallbackInfo ci) {
		if (!key.equals(((MixinKeyBinding) (Object) InventoryTabs.NEXT_TAB).key)) return;
		for (KeyMapping bind : KEYS_TO_BINDINGS.get(key)) {
			bind.setDown(pressed$);
		}
		ci.cancel();
	}

	@Inject(method = "resetMapping", at = @At("HEAD"))
	private static void updateConflictedBinds(CallbackInfo ci) {
		KEYS_TO_BINDINGS.clear();
		for (KeyMapping bind : ALL.values()) {
			KEYS_TO_BINDINGS.put(((MixinKeyBinding) (Object) bind).key, bind);
		}
	}
}
