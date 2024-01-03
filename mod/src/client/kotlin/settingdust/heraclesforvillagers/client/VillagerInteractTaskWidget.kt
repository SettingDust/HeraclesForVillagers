package settingdust.heraclesforvillagers.client

import com.teamresourceful.resourcefullib.client.scissor.ScissorBoxStack
import com.teamresourceful.resourcefullib.client.utils.RenderUtils
import earth.terrarium.heracles.api.client.DisplayWidget
import earth.terrarium.heracles.api.client.WidgetUtils
import earth.terrarium.heracles.api.client.theme.QuestScreenTheme
import earth.terrarium.heracles.api.tasks.client.display.TaskTitleFormatter
import earth.terrarium.heracles.common.handlers.progress.TaskProgress
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.EntityType
import net.minecraft.nbt.NbtByte
import net.minecraft.text.Text
import net.minecraft.village.VillagerProfession
import settingdust.heraclesforvillagers.HeraclesForVillagers
import settingdust.heraclesforvillagers.VillagerInteractTask

data class VillagerInteractTaskWidget(
    val task: VillagerInteractTask,
    val progress: TaskProgress<NbtByte>
) : DisplayWidget {
    companion object {
        private const val DESC = "task.${HeraclesForVillagers.NAMESPACE}.villager_interaction.desc"
    }

    override fun render(
        graphics: DrawContext,
        scissor: ScissorBoxStack,
        x: Int,
        y: Int,
        width: Int,
        mouseX: Int,
        mouseY: Int,
        hovered: Boolean,
        partialTicks: Float
    ) {
        val font = MinecraftClient.getInstance().textRenderer
        WidgetUtils.drawBackground(
            graphics,
            x,
            y,
            width,
            getHeight(width),
        )
        val iconSize = 32
        if (!task.icon().render(graphics, scissor, x + 5, y + 5, iconSize, iconSize)) {
            val type = EntityType.VILLAGER
            RenderUtils.createScissorBoxStack(
                    scissor,
                    MinecraftClient.getInstance(),
                    graphics.matrices,
                    x + 5,
                    y + 5,
                    iconSize,
                    iconSize,
                )
                .use { _ ->
                    WidgetUtils.drawEntity(
                        graphics,
                        x + 5,
                        y + 5,
                        iconSize,
                        type.create(MinecraftClient.getInstance().world).also {
                            it?.villagerData =
                                it?.villagerData?.withProfession(
                                    task.profession.value?.left()?.orElse(null)
                                        ?: VillagerProfession.NONE,
                                )
                        }
                    )
                }
            graphics.drawText(
                font,
                task.titleOr(TaskTitleFormatter.create(this.task)),
                x + iconSize + 16,
                y + 6,
                QuestScreenTheme.getTaskTitle(),
                false,
            )
            graphics.drawText(
                font,
                Text.translatable(DESC, Text.keybind("key.use")),
                x + iconSize + 16,
                y + 8 + font.fontHeight,
                QuestScreenTheme.getTaskDescription(),
                false,
            )
            WidgetUtils.drawProgressText(graphics, x, y, width, this.task, this.progress)

            val height = getHeight(width)
            WidgetUtils.drawProgressBar(
                graphics,
                x + iconSize + 16,
                y + height - font.fontHeight - 5,
                x + width - 5,
                y + height - 6,
                this.task,
                this.progress,
            )
        }
    }

    override fun getHeight(width: Int) = 42
}
