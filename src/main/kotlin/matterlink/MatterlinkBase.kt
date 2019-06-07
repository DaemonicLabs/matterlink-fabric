package matterlink

import kotlinx.coroutines.runBlocking
import matterlink.api.ApiMessage
import matterlink.command.BridgeCommandRegistry
import matterlink.command.IBridgeCommand
import matterlink.command.IMinecraftCommandSender
import matterlink.config.cfg
import matterlink.update.UpdateChecker
import java.util.UUID


abstract class MatterlinkBase {
    abstract fun commandSenderFor(
        user: String,
        env: IBridgeCommand.CommandEnvironment,
        op: Boolean
    ): IMinecraftCommandSender

    abstract fun wrappedSendToPlayers(msg: String)

    abstract fun wrappedSendToPlayer(username: String, msg: String)
    abstract fun wrappedSendToPlayer(uuid: UUID, msg: String)
    abstract fun isOnline(username: String): Boolean
    abstract fun nameToUUID(username: String): UUID?
    abstract fun uuidToName(uuid: UUID): String?

    /**
     * in milliseconds
     */
    var serverStartTime: Long = System.currentTimeMillis()

    fun getUptimeAsString(): String {
        val total = (System.currentTimeMillis() - serverStartTime) / 1000
        val s = total % 60
        val m = (total / 60) % 60
        val h = (total / 3600) % 24
        val d = total / 86400

        fun timeFormat(unit: Long, name: String) = when {
            unit > 1L -> "$unit ${name}s "
            unit == 1L -> "$unit $name "
            else -> ""
        }

        var result = ""
        result += timeFormat(d, "Day")
        result += timeFormat(h, "Hour")
        result += timeFormat(m, "Minute")
        result += timeFormat(s, "Second")
        return result
    }

    fun registerBridgeCommands() {
        BridgeCommandRegistry.reloadCommands()
    }

    suspend fun start() {
        serverStartTime = System.currentTimeMillis()

        if (cfg.connect.autoConnect)
            MessageHandler.start(cfg.outgoing.announceConnectMessage, true)
        UpdateChecker.check()
    }

    fun onShutdown() = runBlocking {
        stop()
    }

    suspend fun stop() {
        MessageHandler.stop(cfg.outgoing.announceDisconnectMessage)
    }
}