package matterlink.handlers

import matterlink.logger
import matterlink.MessageHandler
import matterlink.api.ApiMessage
import matterlink.config.cfg
import matterlink.stripColorOut
import java.util.UUID

object LocationHandler {

    // TODO: rename and move to MessageHandler/Inst
    suspend fun sendToLocations(
        user: String = cfg.outgoing.systemUser,
        msg: String,
        x: Int = -1, y: Int = -1, z: Int = -1,
        dimension: Int? = null,
        event: ChatEvent,
        systemuser: Boolean = false,
        uuid: UUID? = null,
        cause: String
    ): Boolean {
        val filter = cfg.outgoing.filter

        val matchesEvent = when (event) {
            ChatEvent.PLAIN -> filter.plain
            ChatEvent.ACTION -> filter.action
            ChatEvent.DEATH -> filter.death
            ChatEvent.JOIN -> filter.join
            ChatEvent.LEAVE -> filter.leave
            ChatEvent.ADVANCEMENT -> filter.advancement
            ChatEvent.BROADCAST -> filter.broadcast
            ChatEvent.STATUS -> filter.status
        }

        if(!matchesEvent) {
            logger.debug("dropped message '$msg' from user: '$user', event not enabled")
            logger.debug("event: $event")
            logger.debug("filter: $filter")
            return false
        }

        val eventStr = when (event) {
            ChatEvent.PLAIN -> ""
            ChatEvent.ACTION -> ApiMessage.USER_ACTION
            ChatEvent.DEATH -> ""
            ChatEvent.JOIN -> ApiMessage.JOIN_LEAVE
            ChatEvent.LEAVE -> ApiMessage.JOIN_LEAVE
            ChatEvent.ADVANCEMENT -> ""
            ChatEvent.BROADCAST -> ""
            ChatEvent.STATUS -> ""
        }

        val username = when {
            systemuser -> cfg.outgoing.systemUser
            else -> user
        }

        val avatar = when {
            systemuser ->
                cfg.outgoing.avatar.systemUserAvatar
            cfg.outgoing.avatar.enable && uuid != null ->
                cfg.outgoing.avatar.urlTemplate.replace("{uuid}", uuid.toString())
            else ->
                null
        }
        when {
            msg.isNotBlank() -> MessageHandler.transmit(
                ApiMessage(
                    username = username.stripColorOut,
                    text = msg.stripColorOut,
                    event = eventStr,
                    gateway = cfg.connect.gateway
                ).apply {
                    avatar?.let {
                        this.avatar = it
                    }
                },
                cause = cause
            )
            else -> logger.warn("WARN: dropped blank message by '$user'")
        }
        logger.debug("sent message, cause: $cause")
        return true
    }
}