package settingdust.heraclesforvillagers

import com.mojang.datafixers.util.Pair
import dev.sterner.guardvillagers.common.entity.GuardEntity
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
import net.minecraft.util.ActionResult
import settingdust.heraclesforvillagers.mixin.QuestProgressHandlerAccessor

private val QuestProgressHandler.progress: Map<UUID, QuestsProgress>
    get() = (this as QuestProgressHandlerAccessor).progress

val compatGuardVillager: Boolean =
    try {
        Class.forName("dev.sterner.guardvillagers.common.entity.GuardEntity")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

fun init() {
    QuestTasks.register(VillagerInteractTask.TYPE)
    if (compatGuardVillager) QuestTasks.register(GuardVillagerInteractTask.TYPE)

    UseEntityCallback.EVENT.register { player, _, _, entity, _ ->
        if (player !is ServerPlayerEntity) return@register ActionResult.PASS
        if (entity is VillagerEntity)
            QuestProgressHandler.getProgress(player.server, player.uuid)
                .testAndProgressTaskType(player, Pair(player, entity), VillagerInteractTask.TYPE)
        if (compatGuardVillager && entity is GuardEntity)
            QuestProgressHandler.getProgress(player.server, player.uuid)
                .testAndProgressTaskType(
                    player,
                    Pair(player, entity),
                    GuardVillagerInteractTask.TYPE
                )
        return@register ActionResult.PASS
    }

    ServerLivingEntityEvents.AFTER_DEATH.register { entity, _ ->
        if (entity !is VillagerEntity) return@register
        val server = entity.server ?: return@register
        val questProgressHandler = QuestProgressHandler.read(server)

        data class Task(val task: BindableEntityTask, val player: UUID, val quest: String)

        val tasksNeedRebind = mutableSetOf<Task>()
        for ((uuid, questProgresses) in questProgressHandler.progress) {
            for (questId in questProgresses.completableQuests.getQuests(questProgresses)) {
                val quest = QuestHandler.get(questId)
                val questProgress = questProgresses.progress.getOrDefault(questId, QuestProgress())
                for (task in quest.tasks.values.filterIsInstance<VillagerInteractTask>()) {
                    if (entity.uuid != task.bound) continue
                    val taskProgress = questProgress.getTask(task)
                    taskProgress.reset()
                    taskProgress.progress().putBoolean("dead", true)
                    tasksNeedRebind += Task(task, uuid, questId)
                }

                if (compatGuardVillager)
                    for (task in quest.tasks.values.filterIsInstance<GuardVillagerInteractTask>()) {
                        if (entity.uuid != task.bound) continue
                        val taskProgress = questProgress.getTask(task)
                        taskProgress.reset()
                        taskProgress.progress().putBoolean("dead", true)
                        tasksNeedRebind += Task(task, uuid, questId)
                    }
            }
        }

        if (tasksNeedRebind.isNotEmpty()) {
            for ((task) in tasksNeedRebind) {
                task.bound = null
            }

            for ((player, quest) in tasksNeedRebind.associateBy({ it.player }, { it.quest })) {
                server.playerManager.getPlayer(player)?.let {
                    QuestProgressHandler.sync(it, listOf(quest))
                }
            }
        }
    }
}

object HeraclesForVillagers {
    const val NAMESPACE = "heracles_for_villagers"
}
