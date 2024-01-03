package settingdust.heraclesforvillagers

import com.mojang.datafixers.util.Pair
import earth.terrarium.heracles.api.tasks.QuestTasks
import earth.terrarium.heracles.common.handlers.progress.QuestProgress
import earth.terrarium.heracles.common.handlers.progress.QuestProgressHandler
import earth.terrarium.heracles.common.handlers.progress.QuestsProgress
import earth.terrarium.heracles.common.handlers.quests.QuestHandler
import java.util.*
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import settingdust.heraclesforvillagers.mixin.QuestProgressHandlerAccessor

private val QuestProgressHandler.progress: Map<UUID, QuestsProgress>
    get() = (this as QuestProgressHandlerAccessor).progress

fun init() {
    QuestTasks.register(VillagerInteractTask.TYPE)

    UseEntityCallback.EVENT.register { player, _, _, entity, _ ->
        if (player !is ServerPlayerEntity) return@register ActionResult.PASS
        if (entity !is VillagerEntity) return@register ActionResult.PASS
        QuestProgressHandler.getProgress(player.server, player.uuid)
            .testAndProgressTaskType(player, Pair(player, entity), VillagerInteractTask.TYPE)
        return@register ActionResult.PASS
    }

    ServerLivingEntityEvents.AFTER_DEATH.register { entity, _ ->
        if (entity !is VillagerEntity) return@register
        val server = entity.server ?: return@register
        val questProgressHandler = QuestProgressHandler.read(server)
        for ((uuid, questProgresses) in questProgressHandler.progress) {
            for (questId in questProgresses.completableQuests.getQuests(questProgresses)) {
                val quest = QuestHandler.get(questId)
                val questProgress = questProgresses.progress.getOrDefault(questId, QuestProgress())
                for (task in quest.tasks.values.filterIsInstance<VillagerInteractTask>()) {
                    if (entity.uuid != task.bound) continue
                    /*
                     TODO Need record the players offline for message later.
                    */
                    server.playerManager
                        .getPlayer(uuid)
                        ?.sendMessage(
                            Text.translatable(
                                "${HeraclesForVillagers.NAMESPACE}.task.failed.bound_entity_died",
                                quest.display.title(),
                                entity.displayName
                            )
                        )
                    questProgress.reset()
                    task.bound = null

                    break
                }
            }
        }
    }
}

object HeraclesForVillagers {
    const val NAMESPACE = "heracles_for_villagers"
}
