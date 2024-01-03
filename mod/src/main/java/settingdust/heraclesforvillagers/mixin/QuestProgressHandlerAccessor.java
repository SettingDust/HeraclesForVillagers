package settingdust.heraclesforvillagers.mixin;

import earth.terrarium.heracles.common.handlers.progress.QuestProgressHandler;
import earth.terrarium.heracles.common.handlers.progress.QuestsProgress;
import java.util.Map;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = QuestProgressHandler.class, remap = false)
public interface QuestProgressHandlerAccessor {
    @Accessor
    Map<UUID, QuestsProgress> getProgress();
}
