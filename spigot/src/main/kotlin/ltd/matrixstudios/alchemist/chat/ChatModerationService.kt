package ltd.matrixstudios.alchemist.chat

import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class ChatMessage(
    val id: String,
    val senderUuid: UUID,
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

object ChatModerationService {

    private val recentMessages = ConcurrentHashMap<String, ChatMessage>()

    fun register(senderUuid: UUID, senderName: String, content: String): ChatMessage {
        val id = UUID.randomUUID().toString().substring(0, 8)
        val msg = ChatMessage(id, senderUuid, senderName, content)
        recentMessages[id] = msg
        return msg
    }

    fun get(id: String): ChatMessage? = recentMessages[id]

    fun remove(id: String): ChatMessage? = recentMessages.remove(id)
}