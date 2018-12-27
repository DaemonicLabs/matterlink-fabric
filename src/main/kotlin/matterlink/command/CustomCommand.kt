package matterlink.command

import matterlink.Matterlink
import matterlink.lazyFormat
import matterlink.logger
import matterlink.stripColorIn

data class CustomCommand(
    val type: CommandType = CommandType.RESPONSE,
    val execute: String? = null,
    val response: String? = null,
    override val permLevel: Double = 0.0,
    override val help: String = "",
    override val timeout: Int = 20,
    val defaultCommand: Boolean? = null,
    val execOp: Boolean? = null,
    val argumentsRegex: Regex? = null
) : IBridgeCommand() {

    override suspend fun execute(alias: String, user: String, env: CommandEnvironment, args: String): Boolean {
        if (argumentsRegex != null) {
            logger.debug("testing '$args' against '${argumentsRegex.pattern}'")
            if (!argumentsRegex.matches(args)) {
                env.respond("$user sent invalid input to command $alias")
                return false
            }
        }

        return when (type) {
            CommandType.EXECUTE -> {
                // uses a new commandsender for each use
                val commandSender = Matterlink.commandSenderFor(user, env, execOp ?: false)
                val cmd = execute?.lazyFormat(getReplacements(user, env, args))?.stripColorIn
                    ?: return false
                commandSender.execute(cmd) || commandSender.reply.isNotEmpty()
            }
            CommandType.RESPONSE -> {
                env.respond(
                    response?.lazyFormat(getReplacements(user, env, args))
                        ?: "", cause = "response to command: $alias"
                )

                true
            }
        }
    }

    /**
     *
     */
    override fun validate(): Boolean {
        val typeCheck = when (type) {
            CommandType.EXECUTE -> execute?.isNotBlank() ?: false
            CommandType.RESPONSE -> response?.isNotBlank() ?: false
        }
        if (!typeCheck) return false

        return true
    }

    companion object {
        val DEFAULT = CustomCommand()

        fun getReplacements(user: String, env: CommandEnvironment, args: String): Map<String, () -> String?> = mapOf(
            "{uptime}" to Matterlink::getUptimeAsString,
            "{user}" to { user },
            "{userid}" to {
                when (env) {
                    is CommandEnvironment.BridgeEnv -> env.userId
                    else -> null
                }
            },
            "{uuid}" to {
                when (env) {
                    is CommandEnvironment.BridgeEnv -> env.uuid.toString()
                    is CommandEnvironment.GameEnv -> env.uuid.toString()
                }
            },
            "{username}" to {
                when (env) {
                    is CommandEnvironment.BridgeEnv -> env.uuid
                    is CommandEnvironment.GameEnv -> env.uuid
                }?.let { Matterlink.uuidToName(it) }
            },
            "{platform}" to {
                when (env) {
                    is CommandEnvironment.BridgeEnv -> env.platform
                    else -> null
                }
            },
            "{args}" to { args },
            "{version}" to { Matterlink.modVersion }
        )
    }
}

enum class CommandType {
    EXECUTE, RESPONSE
}