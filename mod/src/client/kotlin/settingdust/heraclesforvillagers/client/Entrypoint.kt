package settingdust.heraclesforvillagers.client

import com.terraformersmc.modmenu.api.ModMenuApi
import dev.sterner.guardvillagers.GuardVillagers
import earth.terrarium.heracles.api.client.settings.Settings
import earth.terrarium.heracles.api.tasks.QuestTaskDisplayFormatter
import earth.terrarium.heracles.api.tasks.client.QuestTaskWidgets
import earth.terrarium.heracles.api.tasks.client.display.TaskTitleFormatter
import earth.terrarium.heracles.api.tasks.client.display.TaskTitleFormatters
import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import settingdust.heraclesforvillagers.GuardVillagerInteractTask
import settingdust.heraclesforvillagers.VillagerInteractTask
import settingdust.heraclesforvillagers.compatGuardVillager

fun init() {
    Settings.register(VillagerInteractTask.TYPE, VillagerInteractSettings)
    QuestTaskWidgets.registerSimple(VillagerInteractTask.TYPE, ::VillagerInteractTaskWidget)
    TaskTitleFormatter.register(VillagerInteractTask.TYPE) { task ->
        Text.translatable(
            TaskTitleFormatters.toTranslationKey(task, true),
            task.profession.getDisplayName { profession ->
                Text.translatable(
                    "${EntityType.VILLAGER.translationKey}.${
                        Registries.VILLAGER_PROFESSION.getId(
                            profession,
                        ).path
                    }",
                )
            },
        )
    }
    QuestTaskDisplayFormatter.register(VillagerInteractTask.TYPE) { progress, task ->
        String.format(
            "%d/%d",
            if (task.storage().read(progress.progress()).getBoolean("progress")) 1 else 0,
            1,
        )
    }

    if (compatGuardVillager) {
        Settings.register(GuardVillagerInteractTask.TYPE, GuardVillagerInteractSettings)
        QuestTaskWidgets.registerSimple(
            GuardVillagerInteractTask.TYPE,
            ::GuardVillagerInteractTaskWidget
        )
        TaskTitleFormatter.register(GuardVillagerInteractTask.TYPE) { task ->
            Text.translatable(
                TaskTitleFormatters.toTranslationKey(task, true),
                GuardVillagers.GUARD_VILLAGER.name
            )
        }
        QuestTaskDisplayFormatter.register(GuardVillagerInteractTask.TYPE) { progress, task ->
            String.format(
                "%d/%d",
                if (task.storage().read(progress.progress()).getBoolean("progress")) 1 else 0,
                1,
            )
        }
    }
}

object ModMenuEntrypoint : ModMenuApi {}
