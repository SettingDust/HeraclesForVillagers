package settingdust.heraclesforvillagers.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.Codec;
import earth.terrarium.heracles.api.quests.Quest;
import earth.terrarium.heracles.api.tasks.QuestTask;
import earth.terrarium.heracles.api.tasks.QuestTaskType;
import earth.terrarium.heracles.common.handlers.progress.QuestsProgress;
import earth.terrarium.heracles.common.handlers.quests.QuestHandler;
import earth.terrarium.heracles.common.network.NetworkHandler;
import earth.terrarium.heracles.common.network.packets.quests.ClientboundUpdateQuestPacket;
import earth.terrarium.heracles.common.network.packets.quests.data.NetworkQuestData;
import java.util.Objects;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = QuestsProgress.class)
public class QuestsProgressMixin {
    @Inject(
            method = "testAndProgressTaskType",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Learth/terrarium/heracles/common/handlers/progress/TaskProgress;progress()Lnet/minecraft/nbt/NbtElement;",
                            ordinal = 0))
    private void saveBeforeTask(
            ServerPlayerEntity player,
            Object input,
            QuestTaskType<?> taskType,
            CallbackInfo ci,
            @Local String id,
            @Local QuestTask<?, ?, ?> task,
            @Share("beforeTask") LocalRef<NbtElement> beforeTask) {
        Codec<QuestTask<?, ?, ?>> codec =
                (Codec<QuestTask<?, ?, ?>>) task.type().codec(task.id());
        beforeTask.set(codec.encodeStart(
                        RegistryOps.of(NbtOps.INSTANCE, player.getWorld().getRegistryManager()), task)
                .result()
                .orElseThrow());
    }

    @Inject(
            method = "testAndProgressTaskType",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Learth/terrarium/heracles/common/handlers/progress/TaskProgress;progress()Lnet/minecraft/nbt/NbtElement;",
                            ordinal = 1))
    private void compareTask(
            ServerPlayerEntity player,
            Object input,
            QuestTaskType<?> taskType,
            CallbackInfo ci,
            @Local String id,
            @Local QuestTask<?, ?, ?> task,
            @Share("needSync") LocalBooleanRef needSync,
            @Share("beforeTask") LocalRef<NbtElement> beforeTask) {
        Codec<QuestTask<?, ?, ?>> codec =
                (Codec<QuestTask<?, ?, ?>>) task.type().codec(task.id());
        final var afterTask = codec.encodeStart(
                        RegistryOps.of(NbtOps.INSTANCE, player.getWorld().getRegistryManager()), task)
                .result()
                .orElseThrow();
        if (!afterTask.equals(beforeTask)) {
            QuestHandler.markDirty(id);
            needSync.set(true);
        }
    }

    @Inject(
            method = "testAndProgressTaskType",
            at =
                    @At(
                            value = "INVOKE",
                            remap = false,
                            target =
                                    "Learth/terrarium/heracles/common/handlers/progress/QuestProgress;update(Learth/terrarium/heracles/api/quests/Quest;)V"))
    private void saveQuest(
            ServerPlayerEntity player,
            Object input,
            QuestTaskType<?> taskType,
            CallbackInfo ci,
            @Local String id,
            @Local Quest quest,
            @Share("needSync") LocalBooleanRef needSync) {
        if (needSync.get()) {
            var packet = new ClientboundUpdateQuestPacket(
                    id, NetworkQuestData.builder().tasks(quest.tasks()).build());
            NetworkHandler.CHANNEL.sendToAllPlayers(packet, Objects.requireNonNull(player.getServer()));
        }
    }
}
