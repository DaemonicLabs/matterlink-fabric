package matterlink.handlers

import kotlinx.coroutines.runBlocking
import matterlink.antiping
import matterlink.config.cfg
import matterlink.mapFormat
import matterlink.stripColorOut

object JoinLeaveHandler {
    @JvmName("handleJoin")
    fun handleJoinJava(player: String) = runBlocking {
        handleJoin(player)
    }

    suspend fun handleJoin(
        player: String
    ) {
        if (cfg.outgoing.filter.join) {
            val msg = cfg.outgoing.joinPart.joinServer.mapFormat(
                mapOf(
                    "{username}" to player.stripColorOut,
                    "{username:antiping}" to player.stripColorOut.antiping
                )
            )
            LocationHandler.sendToLocations(
                msg = msg,
                event = ChatEvent.JOIN,
                systemuser = true,
                cause = "$player joined"
            )
        }
    }

    @JvmName("handleLeave")
    fun handleLeaveJava(player: String, reason: String) = runBlocking {
        handleLeave(player, reason)
    }

    suspend fun handleLeave(
        player: String,
        reason: String
    ) {
        if (cfg.outgoing.filter.leave) {
            val msg = cfg.outgoing.joinPart.partServer.mapFormat(
                mapOf(
                    "{username}" to player.stripColorOut,
                    "{username:antiping}" to player.stripColorOut.antiping,
                    "{reason}" to reason
                )
            )
            LocationHandler.sendToLocations(
                msg = msg,
                event = ChatEvent.JOIN,
                systemuser = true,
                cause = "$player left"
            )
        }
    }
}