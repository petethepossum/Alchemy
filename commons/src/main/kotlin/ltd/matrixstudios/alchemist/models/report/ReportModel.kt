package ltd.matrixstudios.alchemist.models.report

import java.util.*

data class ReportModel(
    val id: UUID,
    val reason: String,
    val issuer: UUID,
    val issuedTo: UUID,
    val server: String,
    val issuedAt: Long,
    var status: ReportStatus = ReportStatus.OPEN,
    var handledBy: UUID? = null,
    var notes: MutableList<String> = mutableListOf(),
    var numericId: Int = -1,
) {
    val isArchived: Boolean
        get() = status == ReportStatus.CLOSED
}








enum class ReportStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}
