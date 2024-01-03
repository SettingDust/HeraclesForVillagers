package settingdust.heraclesforvillagers;

import earth.terrarium.heracles.common.utils.RegistryValue;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.VillagerProfession;

public final class RegistryValues {
    public static final RegistryValue<VillagerProfession> VILLAGER_PROFESSION_NONE =
            registryValue(Registries.VILLAGER_PROFESSION.getEntry(VillagerProfession.NONE));

    public static <T> RegistryValue<T> registryValue(RegistryEntry<T> value) {
        return new RegistryValue<>(value);
    }
}
