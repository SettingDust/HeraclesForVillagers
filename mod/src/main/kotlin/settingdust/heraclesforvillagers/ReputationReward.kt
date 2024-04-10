package settingdust.heraclesforvillagers

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import earth.terrarium.heracles.api.CustomizableQuestElement
import earth.terrarium.heracles.api.quests.QuestIcon
import earth.terrarium.heracles.api.quests.QuestIcons
import earth.terrarium.heracles.api.quests.defaults.ItemQuestIcon
import earth.terrarium.heracles.api.rewards.QuestReward
import earth.terrarium.heracles.api.rewards.QuestRewardType
import java.util.stream.Stream
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.village.VillageGossipType
import settingdust.heraclesforblabber.HeraclesTaskInterlocutorTracker.Companion.heraclesTaskInterlocutorTracker

data class ReputationReward(
    val id: String,
    val title: String,
    val icon: QuestIcon<*>,
    val value: Int,
    val type: VillageGossipType
) : QuestReward<ReputationReward>, CustomizableQuestElement {
    override fun id() = id

    override fun title() = title

    override fun icon() = icon

    @Suppress("UnstableApiUsage")
    override fun reward(player: ServerPlayerEntity): Stream<ItemStack> {
        player.heraclesTaskInterlocutorTracker.rewardingQuest?.let {
            when (val entity = player.heraclesTaskInterlocutorTracker.data[it]) {
                is GossipHolder -> {
                    entity.`heraclesforvillagers$gossips`.startGossip(player.uuid, type, value)
                }
                null -> HeraclesForVillagers.LOGGER.error("Rewarding quest $it has no interlocutor")
                else -> HeraclesForVillagers.LOGGER.error("Entity $entity isn't support reputation")
            }
        }
        return Stream.empty()
    }

    override fun type() = Type

    data object Type : QuestRewardType<ReputationReward> {
        override fun id() = HeraclesForVillagers.identifier("reputation")

        override fun codec(id: String): Codec<ReputationReward> =
            RecordCodecBuilder.create { instance ->
                instance
                    .group(
                        RecordCodecBuilder.point(id),
                        Codec.STRING.fieldOf("title").orElse("").forGetter { it.title },
                        QuestIcons.CODEC.fieldOf("icon")
                            .orElse(ItemQuestIcon(Items.AIR))
                            .forGetter { it.icon },
                        Codec.INT.fieldOf("value").forGetter(ReputationReward::value),
                        Codec.STRING.comapFlatMap(
                                {
                                    try {
                                        DataResult.success(
                                            VillageGossipType.valueOf(it.uppercase())
                                        )
                                    } catch (e: Exception) {
                                        DataResult.error {
                                            "Failed to parse gossip type $it: ${e.message}"
                                        }
                                    }
                                },
                                { it.name.lowercase() }
                            )
                            .fieldOf("type")
                            .forGetter { it.type }
                    )
                    .apply(instance, ::ReputationReward)
            }
    }
}
