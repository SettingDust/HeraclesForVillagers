package settingdust.heraclesforvillagers

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.sterner.guardvillagers.common.entity.GuardEntity
import earth.terrarium.heracles.api.CustomizableQuestElement
import earth.terrarium.heracles.api.quests.QuestIcon
import earth.terrarium.heracles.api.quests.QuestIcons
import earth.terrarium.heracles.api.quests.defaults.ItemQuestIcon
import earth.terrarium.heracles.api.tasks.PairQuestTask
import earth.terrarium.heracles.api.tasks.QuestTaskType
import earth.terrarium.heracles.api.tasks.storage.defaults.BooleanTaskStorage
import java.util.*
import kotlin.jvm.optionals.getOrNull
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.util.Identifier
import net.minecraft.util.Uuids
import org.quiltmc.qkl.library.serialization.CodecFactory
import org.quiltmc.qkl.library.serialization.annotation.CodecSerializable

private class GuardVillagerInteractTaskType : QuestTaskType<GuardVillagerInteractTask> {
    override fun id() = Identifier(HeraclesForVillagers.NAMESPACE, "guard_villager_interaction")

    override fun codec(id: String): Codec<GuardVillagerInteractTask> {

        return RecordCodecBuilder.create { instance ->
            instance
                .group(
                    RecordCodecBuilder.point(id),
                    Codec.STRING.fieldOf("title").orElse("").forGetter { it.title },
                    QuestIcons.CODEC.fieldOf("icon").orElse(ItemQuestIcon(Items.AIR)).forGetter {
                        it.icon
                    },
                    Codec.INT.fieldOf("min_reputation").orElse(1).forGetter {
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
                .apply(instance) { id, title, icon, minReputation, maxReputation, bindToFirst, bound
                    ->
                    GuardVillagerInteractTask(
                        id,
                        title,
                        icon,
                        minReputation..maxReputation,
                        bindToFirst,
                        bound.getOrNull(),
                    )
                }
        }
    }
}

private val codecFactory = CodecFactory {}

data class GuardVillagerInteractTask(
    val id: String,
    val title: String,
    val icon: QuestIcon<*>,
    override val reputationRange: IntRange = IntRange.EMPTY,
    override val bindToFirst: Boolean = true,
    override var bound: UUID? = null
) :
    PairQuestTask<PlayerEntity, GuardEntity, NbtCompound, GuardVillagerInteractTask>,
    CustomizableQuestElement,
    BindableEntityTask,
    ReputationEntityTask {
    companion object {
        val TYPE: QuestTaskType<GuardVillagerInteractTask> = GuardVillagerInteractTaskType()
    }

    override fun id() = id

    override fun test(
        type: QuestTaskType<*>,
        nbt: NbtCompound,
        player: PlayerEntity,
        villager: GuardEntity
    ): NbtCompound {
        val progress =
            Progress.CODEC.parse(NbtOps.INSTANCE, storage().read(nbt)).result().orElseThrow()
        if (progress.dead && bound != null) {
            return Progress.CODEC.encodeStart(NbtOps.INSTANCE, Progress(dead = true))
                .result()
                .orElseThrow() as NbtCompound
        }
        bound =
            if (bound == null && bindToFirst) {
                villager.uuid
            } else bound
        val isBoundVillager = bound?.equals(villager.uuid) ?: true
        return Progress.CODEC.encodeStart(
                NbtOps.INSTANCE,
                Progress(progress.progress || isBoundVillager, progress.dead)
            )
            .result()
            .orElseThrow() as NbtCompound
    }

    override fun getProgress(progress: NbtCompound) =
        if (
            Progress.CODEC.parse(NbtOps.INSTANCE, storage().read(progress))
                .result()
                .orElseThrow()
                .progress
        )
            1.0F
        else 0.0F

    override fun storage() =
        CompoundTaskStorage(
            "progress" to BooleanTaskStorage.INSTANCE,
            "dead" to BooleanTaskStorage.INSTANCE
        )

    override fun type() = TYPE

    override fun title() = title

    override fun icon() = icon

    @CodecSerializable
    data class Progress(val progress: Boolean = false, val dead: Boolean = false) {
        companion object {
            val CODEC = codecFactory.create<Progress>()
        }
    }
}
