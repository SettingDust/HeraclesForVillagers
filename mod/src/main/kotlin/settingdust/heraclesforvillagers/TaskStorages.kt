package settingdust.heraclesforvillagers

import earth.terrarium.heracles.api.tasks.storage.TaskStorage
import java.util.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtIntArray

class CompoundTaskStorage(vararg storages: Pair<String, TaskStorage<*, *>>) :
    TaskStorage<NbtCompound, NbtCompound> {

    private var storages: List<Pair<String, TaskStorage<*, *>>> = storages.toList()

    override fun createDefault() =
        NbtCompound().also {
            for (storage in storages) {
                it.put(storage.first, storage.second.createDefault())
            }
        }

    override fun read(nbt: NbtCompound) = nbt
}

object UUIDTaskStorage : TaskStorage<UUID?, NbtIntArray> {
    override fun createDefault() = NbtIntArray(IntArray(0))

    override fun read(nbt: NbtIntArray) =
        try {
            NbtHelper.toUuid(nbt)
        } catch (ignored: Throwable) {
            null
        }
}
