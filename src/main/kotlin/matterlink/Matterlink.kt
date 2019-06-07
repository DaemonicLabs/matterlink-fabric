package matterlink

import kotlinx.coroutines.runBlocking
import matterlink.MatterlinkConstants.BUILD_NUMBER
import matterlink.MatterlinkConstants.JENKINS_BUILD_NUMBER
import matterlink.MatterlinkConstants.FABRIC_API_VERSION
import matterlink.MatterlinkConstants.MC_VERSION
import matterlink.MatterlinkConstants.VERSION
import matterlink.command.IBridgeCommand
import matterlink.command.IMinecraftCommandSender
import matterlink.config.BaseConfig
import matterlink.config.cfg
import matterlink.handlers.TickHandler
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.server.ServerTickCallback
import net.minecraft.network.chat.ChatMessageType
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import java.io.File
import java.util.UUID

object Matterlink : DedicatedServerModInitializer, MatterlinkBase() {
    lateinit var server: MinecraftDedicatedServer

    override fun commandSenderFor(
        user: String,
        env: IBridgeCommand.CommandEnvironment,
        op: Boolean
    ): IMinecraftCommandSender {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun wrappedSendToPlayers(msg: String) {
        logger.info(msg)
        server.playerManager.playerList.forEach {
            it.sendChatMessage(TextComponent(msg), ChatMessageType.CHAT)
        }
    }

    override fun wrappedSendToPlayer(username: String, msg: String) {
        server.playerManager.getPlayer(username)?.sendChatMessage(TextComponent(msg), ChatMessageType.CHAT)
    }

    override fun wrappedSendToPlayer(uuid: UUID, msg: String) {
        server.playerManager.getPlayer(uuid)?.sendChatMessage(TextComponent(msg), ChatMessageType.CHAT)
    }

    override fun isOnline(username: String): Boolean {
        return server.playerManager.getPlayer(username) != null
    }

    override fun nameToUUID(username: String): UUID? {
        return server.playerManager.getPlayer(username)?.uuid
    }

    override fun uuidToName(uuid: UUID): String? {
        return server.playerManager.getPlayer(uuid)?.entityName
    }

    override fun onInitializeServer() = runBlocking<Unit> {
        logger.info("Matterlink server starting")

        ServerTickCallback.EVENT.register(TickHandler)

        // TODO: register commands

        // TODO: start bridge

        val rootFolder = File(".").absoluteFile
        val configFolder = rootFolder.resolve("config")
        configFolder.mkdirs()

        logger.info("Building bridge!")

        cfg = BaseConfig(configFolder).load()

        logger.debug("Registering bridge commands")
        registerBridgeCommands()

        start()
    }

    const val mcVersion: String = MC_VERSION
    const val modVersion: String = VERSION
    val buildNumber = BUILD_NUMBER
    const val jenkinsBuildNumber = JENKINS_BUILD_NUMBER
    const val fabricVersion = FABRIC_API_VERSION
}