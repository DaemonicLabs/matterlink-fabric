package matterlink.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import matterlink.logger
import net.minecraft.command.arguments.MessageArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.TranslatableText

object TestCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            (CommandManager
                .literal("fabricSay")
                .requires { source ->
                    source.hasPermissionLevel(2)
                } as LiteralArgumentBuilder)
                .then(CommandManager
                    .argument("message", MessageArgumentType.message())
                    .executes { context ->
                        val var1 = MessageArgumentType.getMessage(context, "message")
                        (context.source as ServerCommandSource).minecraftServer.playerManager.sendToAll(
                            TranslatableText(
                                "chat.type.announcement",
                                (context.source as ServerCommandSource).displayName,
                                var1
                            )
                        )
                        1
                    })
        )
        logger.info("registered fabricSay")
    }
}