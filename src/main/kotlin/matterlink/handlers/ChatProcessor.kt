package matterlink.handlers

import kotlinx.coroutines.runBlocking
import matterlink.command.BridgeCommandRegistry
import matterlink.logger
import java.util.UUID

object ChatProcessor {
    @JvmName("sendToBridge")
    fun sendToBridgeJava(
        user: String,
        msg: String,
        event: ChatEvent,
        uuid: UUID? = null
    ): Boolean = runBlocking {
        sendToBridge(
            user = user,
            msg = msg,
            event = event,
            uuid = uuid
        )
    }

    /**
     * @return cancel message flag
     */
    suspend fun sendToBridge(
        user: String,
        msg: String,
//        x: Int = 0, y: Int = 0, z: Int = 0,
//        dimension: Int? = null,
        event: ChatEvent,
        uuid: UUID? = null
    ): Boolean {
        //TODO: pass message to Locations
//        logger.info("position: $x $y $z dimension: $dimension")
        val message = msg.trim()
        if (uuid != null && BridgeCommandRegistry.handleCommand(message, user, uuid)) return true
        when {
            message.isNotBlank() -> LocationHandler.sendToLocations(
                user = user,
                msg = message,
//                x = x, y = y, z = z, dimension = dimension,
                event = event,
                cause = "Message from $user",
                uuid = uuid
            )


            else -> logger.warn("WARN: dropped blank message by '$user'")
        }
        return false
    }
}
