package settingdust.heraclesforvillagers.client

import earth.terrarium.heracles.api.client.settings.CustomizableQuestElementSettings
import earth.terrarium.heracles.api.client.settings.SettingInitializer
import earth.terrarium.heracles.api.client.settings.base.BooleanSetting
import earth.terrarium.heracles.api.client.settings.base.IntSetting
import earth.terrarium.heracles.api.client.settings.base.TextSetting
import java.util.*
import settingdust.heraclesforvillagers.RegistryValueSettings
import settingdust.heraclesforvillagers.RegistryValues
import settingdust.heraclesforvillagers.VillagerInteractTask

object VillagerInteractSettings :
    SettingInitializer<VillagerInteractTask>,
    CustomizableQuestElementSettings<VillagerInteractTask> {
    override fun create(task: VillagerInteractTask?) =
        super.create(task).apply {
            put(
                "profession",
                RegistryValueSettings.VILLAGER_PROFESSION,
                task?.profession ?: RegistryValues.VILLAGER_PROFESSION_NONE
            )
            put("min_reputation", IntSetting.ONE, task?.reputationRange?.first ?: 1)
            put("max_reputation", IntSetting.ZERO, task?.reputationRange?.last ?: 0)
            put("bind_to_first", BooleanSetting.TRUE, task?.bindToFirst ?: true)
            put("bound", TextSetting.INSTANCE, task?.bound.toString())
        }!!

    override fun create(
        id: String,
        task: VillagerInteractTask?,
        data: SettingInitializer.Data
    ): VillagerInteractTask {
        val profession =
            data
                .get("profession", RegistryValueSettings.VILLAGER_PROFESSION)
                .orElse(task?.profession ?: RegistryValues.VILLAGER_PROFESSION_NONE)
        val minReputation =
            data.get("min_reputation", IntSetting.ONE).orElse(task?.reputationRange?.first ?: 1)
        val maxReputation =
            data.get("max_reputation", IntSetting.ZERO).orElse(task?.reputationRange?.last ?: 0)
        val bindToFirst =
            data.get("bind_to_first", BooleanSetting.TRUE).orElse(task?.bindToFirst ?: true)
        val bound = data.get("bound", TextSetting.INSTANCE).orElse(task?.bound.toString())
        return create(task, data) { title, icon ->
            VillagerInteractTask(
                id,
                title,
                icon,
                profession,
                minReputation..maxReputation,
                bindToFirst,
                try {
                    UUID.fromString(bound)
                } catch (t: Throwable) {
                    null
                }
            )
        }
    }
}
