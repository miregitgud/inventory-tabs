package folk.sisby.inventory_tabs.util;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

public record RegistryMatcher<T>(Either<Either<Holder<T>, TagKey<T>>, Tuple<String, String>> value) {
    public int priority() {
        return value.left().isPresent() ? (value.left().orElseThrow().left().isPresent() ? 0 : 1) : 2;
    }

    public static @Nullable <T> RegistryMatcher<T> fromRegistryString(RegistryAccess manager, ResourceKey<? extends Registry<T>> registry, String value) {
        if (value.contains("*")) { // Prefix/Suffix
            String[] split = value.split("\\*");
            if (split.length == 1) {
                if (value.startsWith("*")) { // Suffix
                    return new RegistryMatcher<>(Either.right(new Tuple<>("", split[0])));
                } else if (value.endsWith("*")) { // Prefix
                    return new RegistryMatcher<>(Either.right(new Tuple<>(split[0], "")));
                }
            } else if (split.length == 2) {
                return new RegistryMatcher<>(Either.right(new Tuple<>(split[0], split[1])));
            }
            return null;
        } else if (value.startsWith("#")) {
            Identifier tagId = Identifier.tryParse(value.substring(1));
            return tagId != null ? new RegistryMatcher<>(Either.left(Either.right(TagKey.create(registry, tagId)))) : null;
        } else {
            Identifier id = Identifier.tryParse(value);
            if (id == null) return null;
            return manager.lookup(registry).flatMap(r -> r.get(ResourceKey.create(registry, id))).map(h -> new RegistryMatcher<>(Either.left(Either.left(h)))).orElse(null);
        }
    }

    public boolean is(Holder<T> value) {
        return this.value.map(e -> e.map((v) -> v.equals(value), value::is), pair -> value.unwrapKey().orElseThrow().identifier().toString().startsWith(pair.getA()) && value.unwrapKey().orElseThrow().identifier().toString().endsWith(pair.getB()));
    }
}
