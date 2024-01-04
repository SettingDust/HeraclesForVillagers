package settingdust.heraclesforvillagers.client

import com.teamresourceful.resourcefullib.client.scissor.ScissorBoxStack
import com.teamresourceful.resourcefullib.client.utils.RenderUtils
import dev.sterner.guardvillagers.GuardVillagers
import earth.terrarium.heracles.api.client.DisplayWidget
import earth.terrarium.heracles.api.client.WidgetUtils
import earth.terrarium.heracles.api.client.theme.QuestScreenTheme
import earth.terrarium.heracles.api.tasks.client.display.TaskTitleFormatter
import earth.terrarium.heracles.common.handlers.progress.TaskProgress
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import settingdust.heraclesforvillagers.GuardVillagerInteractTask
import settingdust.heraclesforvillagers.HeraclesForVillagers

data class GuardVillagerInteractTaskWidget(
    val task: GuardVillagerInteractTask,
    val progress: TaskProgress<NbtCompound>
) : DisplayWidget {
    companion object {
        private const val DESC =
            "task.${HeraclesForVillagers.NAMESPACE}.guard_villager_interaction.desc.singular"
        private const val TITLE_DEAD =
            "task.${HeraclesForVillagers.NAMESPACE}.guard_villager_interaction.title.dead"
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
        val dead = progress.progress().getBoolean("dead")
        WidgetUtils.drawBackground(
            graphics,
            x,
            y,
            width,
            getHeight(width),
        )
        val iconSize = 32
        if (!task.icon().render(graphics, scissor, x + 5, y + 5, iconSize, iconSize)) {
            val type = GuardVillagers.GUARD_VILLAGER
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
                        type.create(MinecraftClient.getInstance().world)
                    )
                }
            graphics.drawText(
                font,
                if (dead)
                    Text.translatable(
                        TITLE_DEAD,
                        task.titleOr(TaskTitleFormatter.create(this.task))
                    )
                else task.titleOr(TaskTitleFormatter.create(this.task)),
                x + iconSize + 16,
                y + 6,
                QuestScreenTheme.getTaskTitle(),
                false,
            )
            graphics.drawText(
                font,
                Text.translatable("$DESC${if (dead) ".dead" else ""}", Text.keybind("key.use")),
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
