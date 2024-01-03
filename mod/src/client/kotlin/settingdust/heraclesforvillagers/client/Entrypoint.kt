package settingdust.heraclesforvillagers.client

import com.terraformersmc.modmenu.api.ModMenuApi
import earth.terrarium.heracles.api.client.settings.Settings
import earth.terrarium.heracles.api.tasks.QuestTaskDisplayFormatter
import earth.terrarium.heracles.api.tasks.client.QuestTaskWidgets
import earth.terrarium.heracles.api.tasks.client.display.TaskTitleFormatter
import earth.terrarium.heracles.api.tasks.client.display.TaskTitleFormatters
import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import settingdust.heraclesforvillagers.VillagerInteractTask

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
                            profession
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
            1
        )
    }
}

object ModMenuEntrypoint : ModMenuApi {}
