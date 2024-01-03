package settingdust.heraclesforvillagers

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import earth.terrarium.heracles.api.CustomizableQuestElement
import earth.terrarium.heracles.api.quests.QuestIcon
import earth.terrarium.heracles.api.quests.QuestIcons
import earth.terrarium.heracles.api.quests.defaults.ItemQuestIcon
import earth.terrarium.heracles.api.tasks.PairQuestTask
import earth.terrarium.heracles.api.tasks.QuestTaskType
import earth.terrarium.heracles.api.tasks.storage.defaults.BooleanTaskStorage
import earth.terrarium.heracles.common.utils.RegistryValue
import java.util.*
import kotlin.jvm.optionals.getOrNull
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.nbt.NbtByte
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.util.Uuids
import net.minecraft.village.VillagerProfession

private class VillagerInteractTaskType : QuestTaskType<VillagerInteractTask> {
    override fun id() = Identifier(HeraclesForVillagers.NAMESPACE, "villager_interaction")

    override fun codec(id: String): Codec<VillagerInteractTask> {

        return RecordCodecBuilder.create { instance ->
            instance
                .group(
                    RecordCodecBuilder.point(id),
                    Codec.STRING.fieldOf("title").orElse("").forGetter { it.title },
                    QuestIcons.CODEC.fieldOf("icon").orElse(ItemQuestIcon(Items.AIR)).forGetter {
                        it.icon
                    },
                    RegistryValue.codec(RegistryKeys.VILLAGER_PROFESSION)
                        .fieldOf("profession")
                        .orElse(RegistryValues.VILLAGER_PROFESSION_NONE)
                        .forGetter { it.profession },
                    Codec.INT.fieldOf("reputationRange").orElse(1).forGetter {
                        it.reputationRange.first
                    },
                    Codec.INT.fieldOf("max_reputation").orElse(0).forGetter {
                        it.reputationRange.last
                    },
                    Codec.BOOL.fieldOf("bind_to_first").orElse(true).forGetter { it.bindToFirst },
                    Uuids.STRING_CODEC.optionalFieldOf("bound").forGetter {
                        Optional.ofNullable(it.bound)
                    },
                )
                .apply(instance) {
                    id,
                    title,
                    icon,
                    profession,
                    minReputation,
                    maxReputation,
                    bindToFirst,
                    bound ->
                    VillagerInteractTask(
                        id,
                        title,
                        icon,
                        profession,
                        minReputation..maxReputation,
                        bindToFirst,
                        bound.getOrNull(),
                    )
                }
        }
    }
}

data class VillagerInteractTask(
    val id: String,
    val title: String,
    val icon: QuestIcon<*>,
    val profession: RegistryValue<VillagerProfession> = RegistryValues.VILLAGER_PROFESSION_NONE,
    val reputationRange: IntRange = IntRange.EMPTY,
    val bindToFirst: Boolean = true,
    var bound: UUID? = null
) :
    PairQuestTask<PlayerEntity, VillagerEntity, NbtByte, VillagerInteractTask>,
    CustomizableQuestElement {
    companion object {
        val TYPE: QuestTaskType<VillagerInteractTask> = VillagerInteractTaskType()
    }

    override fun id() = id

    override fun test(
        type: QuestTaskType<*>,
        nbt: NbtByte,
        player: PlayerEntity,
        villager: VillagerEntity
    ): NbtByte {
        val isMatchProfession =
            profession.`is`(
                Registries.VILLAGER_PROFESSION.getEntry(villager.villagerData.profession),
            )
        bound =
            if (isMatchProfession && bound == null && bindToFirst) {

                villager.uuid
            } else bound
        val isBoundVillager = bound?.equals(villager.uuid) ?: true
        return BooleanTaskStorage.INSTANCE.of(nbt, isMatchProfession && isBoundVillager)
    }

    override fun getProgress(progress: NbtByte) =
        if (storage().readBoolean(progress)) 1.0F else 0.0F

    override fun storage() = BooleanTaskStorage.INSTANCE!!

    override fun type() = TYPE

    override fun title() = title

    override fun icon() = icon
}
