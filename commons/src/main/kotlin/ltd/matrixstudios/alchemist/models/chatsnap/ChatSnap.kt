package ltd.matrixstudios.alchemist.models.chatsnap

import java.util.UUID

data class ChatSnap(
    var id: UUID,
    var owner: UUID,
    var messages: List<String>,
    var createdAt: Long,
    var numericId: Int = 0
)
