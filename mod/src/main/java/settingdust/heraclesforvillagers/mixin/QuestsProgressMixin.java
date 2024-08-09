package settingdust.heraclesforvillagers.mixin;

import earth.terrarium.heracles.api.tasks.QuestTaskType;
import earth.terrarium.heracles.common.handlers.progress.QuestsProgress;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import settingdust.heraclesforvillagers.EntrypointKt;

@Mixin(value = QuestsProgress.class)
public class QuestsProgressMixin {
    @Inject(
        method = "testAndProgressTaskType",
        at = @At(
            value = "INVOKE",
            target = "Learth/terrarium/heracles/common/handlers/progress/QuestsProgress;sendOutQuestComplete(Learth/terrarium/heracles/api/quests/QuestEntry;Lnet/minecraft/server/network/ServerPlayerEntity;)V"
        )
    )
    private <I> void heraclesforvillagers$getTestResult(
        final ServerPlayerEntity player, final I input, final QuestTaskType<?> taskType, final CallbackInfo ci
    ) {
        EntrypointKt.setTaskTestFlag(true);
    }
}
