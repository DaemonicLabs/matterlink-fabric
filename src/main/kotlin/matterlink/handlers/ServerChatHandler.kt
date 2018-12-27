package matterlink.handlers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import matterlink.Matterlink
import matterlink.MessageHandler
import matterlink.api.ApiMessage
import matterlink.command.BridgeCommandRegistry
import matterlink.config.cfg
import matterlink.format
import matterlink.logger
import java.util.UUID

object ServerChatHandler {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    val rcvChannel = MessageHandler.broadcast.openSubscription()

    /**
     * This method must be called every server tick with no arguments.
     */
    suspend fun writeIncomingToChat() {
        val nextMessage = rcvChannel.poll() ?: return

        val filter = cfg.incoming.filter

        val sourceGateway = nextMessage.gateway

        if(sourceGateway != cfg.connect.gateway) {
            logger.debug("message from mismatching gateway: '$sourceGateway' dropped")
            return
        }

        if (nextMessage.event.isEmpty()) {
            if (filter.commands) {
                // try handle command
                if (BridgeCommandRegistry.handleCommand(nextMessage)) return
            }
        }

        val matchesEvent = when (nextMessage.event) {
            "" -> filter.plain
            ApiMessage.JOIN_LEAVE -> filter.join_leave
            ApiMessage.USER_ACTION -> filter.action
            else -> {
                logger.error("unknown event type '${nextMessage.event}' on incoming message")
                return
            }
        }

        if(!matchesEvent) {
            logger.debug("dropped message '${nextMessage.text}' from user: '${nextMessage.username}', event not enabled")
            logger.debug("event: ${nextMessage.event}")
            logger.debug("filter: $filter")
            return
        }

        val message = when (nextMessage.event) {
            "" -> {
                nextMessage.format(cfg.incoming.chat)
            }
            ApiMessage.USER_ACTION -> nextMessage.format(cfg.incoming.action)
            ApiMessage.JOIN_LEAVE -> nextMessage.format(cfg.incoming.joinPart)
            else -> {
                val user = nextMessage.username
                val text = nextMessage.text
                val json = nextMessage.encode()
                logger.debug("Threw out message with unhandled event: ${nextMessage.event}")
                logger.debug(" Message contents:")
                logger.debug(" User: $user")
                logger.debug(" Text: $text")
                logger.debug(" JSON: $json")
                return
            }
        }

        Matterlink.wrappedSendToPlayers(message)
    }
}
