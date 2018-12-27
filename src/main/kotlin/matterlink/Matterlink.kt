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
import net.fabricmc.fabric.events.TickEvent
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import net.minecraft.sortme.ChatMessageType
import net.minecraft.text.StringTextComponent
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
        server.configurationManager.playerList.forEach {
            it.sendChatMessage(StringTextComponent(msg), ChatMessageType.CHAT)
        }
    }

    override fun wrappedSendToPlayer(username: String, msg: String) {
        server.configurationManager.getPlayer(username)?.sendChatMessage(StringTextComponent(msg), ChatMessageType.CHAT)
    }

    override fun wrappedSendToPlayer(uuid: UUID, msg: String) {
        server.configurationManager.getPlayer(uuid)?.sendChatMessage(StringTextComponent(msg), ChatMessageType.CHAT)
    }

    override fun isOnline(username: String): Boolean {
        return server.configurationManager.getPlayer(username) != null
    }

    override fun nameToUUID(username: String): UUID? {
        return server.configurationManager.getPlayer(username)?.uuid
    }

    override fun uuidToName(uuid: UUID): String? {
        return server.configurationManager.getPlayer(uuid)?.entityName
    }

    override fun onInitializeServer() = runBlocking<Unit> {
        logger.info("Matterlink server starting")

        TickEvent.SERVER.register(TickHandler)

        // TODO: register commands

        // TODO: start bridge

        val rootFolder = File(".").absoluteFile
        val configFolder = rootFolder.resolve("config")
        configFolder.mkdirs()

        logger.info("Building bridge!")

        cfg = BaseConfig(configFolder).load()

        logger.debug("Registering bridge commands")
        registerBridgeCommands()

        runBlocking {
            start()
        }
    }

    const val mcVersion: String = MC_VERSION
    const val modVersion: String = VERSION
    val buildNumber = BUILD_NUMBER
    const val jenkinsBuildNumber = JENKINS_BUILD_NUMBER
    const val fabricVersion = FABRIC_API_VERSION
}