package settingdust.heraclesforvillagers

import java.util.*

interface ReputationEntityTask {
    val reputationRange: IntRange
}

interface BindableEntityTask {
    val bindToFirst: Boolean
    var bound: UUID?
}
