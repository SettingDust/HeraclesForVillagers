package settingdust.heraclesforvillagers.client

import com.teamresourceful.resourcefullib.client.scissor.ScissorBoxStack
import earth.terrarium.heracles.api.client.settings.CustomizableQuestElementSettings
import earth.terrarium.heracles.api.client.settings.SettingInitializer
import earth.terrarium.heracles.api.client.settings.base.AutocompleteTextSetting
import earth.terrarium.heracles.api.client.settings.base.IntSetting
import earth.terrarium.heracles.api.client.theme.QuestScreenTheme
import earth.terrarium.heracles.api.rewards.client.defaults.BaseItemRewardWidget
import earth.terrarium.heracles.client.handlers.ClientQuests
import earth.terrarium.heracles.client.screens.quest.BaseQuestScreen
import earth.terrarium.heracles.common.handlers.progress.QuestProgress
import earth.terrarium.heracles.common.network.NetworkHandler
import earth.terrarium.heracles.common.network.packets.rewards.ClaimRewardsPacket
import kotlin.jvm.optionals.getOrDefault
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.village.VillageGossipType
import settingdust.heraclesforvillagers.HeraclesForVillagers
import settingdust.heraclesforvillagers.ReputationReward

object ReputationRewardSettings :
    SettingInitializer<ReputationReward>, CustomizableQuestElementSettings<ReputationReward> {
    val TYPES =
        AutocompleteTextSetting(
            { VillageGossipType.entries },
            { text, item ->
                item.toString().lowercase().contains(text.lowercase()) &&
                    !item.toString().equals(text, ignoreCase = true)
            },
            { it.name.lowercase() }
        )

    override fun create(reward: ReputationReward?): SettingInitializer.CreationData {
        val settings = super.create(reward)
        settings.put(
            "value",
            IntSetting.ONE,
            reward?.value ?: 1,
        )
        settings.put(
            "type",
            TYPES,
            reward?.type ?: VillageGossipType.MINOR_NEGATIVE,
        )
        return settings
    }

    override fun create(id: String, `object`: ReputationReward?, data: SettingInitializer.Data) =
        create(`object`, data) { title, icon ->
            ReputationReward(
                id,
                title,
                icon,
                data.get("value", IntSetting.ONE).getOrDefault(1),
                data.get("type", TYPES).getOrDefault(VillageGossipType.MINOR_NEGATIVE)
            )
        }
}

data class ReputationRewardWidget(
    val reward: ReputationReward,
    val quest: String,
    val progress: QuestProgress?,
    val interactive: Boolean = false
) : BaseItemRewardWidget {
    companion object {
        private const val TITLE_SINGULAR =
            "reward.${HeraclesForVillagers.NAMESPACE}.reputation.title.singular"
        private const val DESC_SINGULAR =
            "reward.${HeraclesForVillagers.NAMESPACE}.reputation.desc.singular"

        fun of(reward: ReputationReward, interactive: Boolean): ReputationRewardWidget {
            val screen = MinecraftClient.getInstance().currentScreen
            if (screen is BaseQuestScreen) {
                return ReputationRewardWidget(
                    reward,
                    screen.questId,
                    ClientQuests.getProgress(screen.questId),
                    interactive,
                )
            }
            return ReputationRewardWidget(reward, "", null, interactive)
        }
    }

    override fun getIconOverride() = reward.icon

    override fun getIcon() =
        ItemStack(Items.PAPER).apply { setCustomName(Text.translatable(TITLE_SINGULAR)) }

    override fun canClaim() = true

    override fun claimReward() {
        progress?.claimReward(reward.id)
        NetworkHandler.CHANNEL.sendToServer(
            ClaimRewardsPacket(
                this.quest,
                reward.id(),
            ),
        )
    }

    override fun isInteractive() = interactive

    override fun render(
        graphics: DrawContext,
        scissor: ScissorBoxStack?,
        x: Int,
        y: Int,
        width: Int,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        partialTicks: Float
    ) {
        val font = MinecraftClient.getInstance().textRenderer
        super.render(graphics, scissor, x, y, width, mouseX, mouseY, hovered, partialTicks)
        graphics.drawText(
            font,
            reward.titleOr(Text.translatable(TITLE_SINGULAR)),
            x + 48,
            y + 6,
            QuestScreenTheme.getRewardTitle(),
            false,
        )
        graphics.drawText(
            font,
            Text.translatable(DESC_SINGULAR, reward.type.toString().lowercase(), reward.value),
            x + 48,
            y + 8 + font.fontHeight,
            QuestScreenTheme.getRewardDescription(),
            false,
        )
    }
}
