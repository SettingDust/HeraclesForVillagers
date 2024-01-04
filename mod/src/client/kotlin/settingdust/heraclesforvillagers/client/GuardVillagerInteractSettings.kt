package settingdust.heraclesforvillagers.client

import earth.terrarium.heracles.api.client.settings.CustomizableQuestElementSettings
import earth.terrarium.heracles.api.client.settings.SettingInitializer
import earth.terrarium.heracles.api.client.settings.base.BooleanSetting
import earth.terrarium.heracles.api.client.settings.base.IntSetting
import earth.terrarium.heracles.api.client.settings.base.TextSetting
import java.util.*
import settingdust.heraclesforvillagers.GuardVillagerInteractTask

object GuardVillagerInteractSettings :
    SettingInitializer<GuardVillagerInteractTask>,
    CustomizableQuestElementSettings<GuardVillagerInteractTask> {
    override fun create(task: GuardVillagerInteractTask?) =
        super.create(task).apply {
            put("min_reputation", IntSetting.ONE, task?.reputationRange?.first ?: 1)
            put("max_reputation", IntSetting.ZERO, task?.reputationRange?.last ?: 0)
            put("bind_to_first", BooleanSetting.TRUE, task?.bindToFirst ?: true)
            put("bound", TextSetting.INSTANCE, task?.bound.toString())
        }!!

    override fun create(
        id: String,
        task: GuardVillagerInteractTask?,
        data: SettingInitializer.Data
    ): GuardVillagerInteractTask {
        val minReputation =
            data.get("min_reputation", IntSetting.ONE).orElse(task?.reputationRange?.first ?: 1)
        val maxReputation =
            data.get("max_reputation", IntSetting.ZERO).orElse(task?.reputationRange?.last ?: 0)
        val bindToFirst =
            data.get("bind_to_first", BooleanSetting.TRUE).orElse(task?.bindToFirst ?: true)
        val bound = data.get("bound", TextSetting.INSTANCE).orElse(task?.bound.toString())
        return create(task, data) { title, icon ->
            GuardVillagerInteractTask(
                id,
                title,
                icon,
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
