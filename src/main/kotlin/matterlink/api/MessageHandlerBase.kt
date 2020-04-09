package matterlink.api

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowViaChannel
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import matterlink.handlers.ServerChatHandler
import matterlink.jsonNonstrict
import matterlink.logger
import java.io.Reader
import java.net.ConnectException
import kotlin.coroutines.CoroutineContext

/**
 * Created by nikky on 07/05/18.
 *
 * @author Nikky
 * @version 1.0
 */
open class MessageHandlerBase {
    private var enabled = false

    private var connectErrors = 0
    private var reconnectCooldown = 0L
    private var sendErrors = 0

    private val keepOpenManager = FuelManager().apply {
        timeoutInMillisecond = 0
        timeoutReadInMillisecond = 0
    }

    var config: Config = Config()

    suspend fun stop(message: String? = null) {
        if (message != null && config.announceDisconnect) {
            sendStatusUpdate(message)
        }
        enabled = false
        rcvJob?.cancel()
        rcvJob = null
    }

    private var rcvJob: Job? = null

    @OptIn(FlowPreview::class)
    suspend fun start(message: String?, clear: Boolean) = coroutineScope {
        logger.debug("starting connection")
        if (clear) {
            clear()
        }

        enabled = true

//        rcvJob = messageReceiver()
        rcvJob = launch {
            messageFlow().collect { msg: ApiMessage ->
                ServerChatHandler.writeMessageToChat(msg)
            }
        }

        logger.info("started rcvJob")

        if (message != null && config.announceConnect) {
            logger.info("sending status update")
            sendStatusUpdate(message)
        }
    }

    private fun clear() {
        val url = "${config.url}/api/messages"
        val (request, response, result) = url.httpGet()
            .apply {
                if (config.token.isNotEmpty()) {
                    headers["Authorization"] = "Bearer ${config.token}"
                }
            }
            .responseString()

        when (result) {
            is Result.Success -> {
                val messages: List<ApiMessage> = jsonNonstrict.parse(ApiMessage.serializer().list, result.value)
                messages.forEach { msg ->
                    logger.trace("skipping $msg")
                }
                logger.debug("skipped ${messages.count()} messages")
            }
            is Result.Failure -> {
                logger.error("failed to clear messages")
                logger.error("url: $url")
                logger.error("cUrl: ${request.cUrlString()}")
                logger.error("response: $response")
                logger.error(result.error.exception.localizedMessage)
                result.error.exception.printStackTrace()
            }
        }
    }

    suspend fun sendStatusUpdate(message: String) {
        transmit(ApiMessage(text = message, gateway = config.gateway))
    }

    open suspend fun transmit(msg: ApiMessage) {
        if (msg.username.isEmpty())
            msg.username = config.systemUser
        if (msg.gateway.isEmpty()) {
            logger.error("missing gateway on message: $msg")
            msg.gateway = config.gateway
        }
        logger.info("Transmitting: $msg")
        msg.let {
            try {
                logger.debug("sending $it")
                val url = "${config.url}/api/message"
                val (request, response, result) = url.httpPost()
                    .apply {
                        if (config.token.isNotEmpty()) {
                            headers["Authorization"] = "Bearer ${config.token}"
                        }
                    }
                    .jsonBody(jsonNonstrict.stringify(ApiMessage.serializer(), it))
                    .responseString()
                when (result) {
                    is Result.Success -> {
                        logger.debug("sent $it")
                        sendErrors = 0
                    }
                    is Result.Failure -> {
                        sendErrors++
                        logger.error("failed to deliver: $it")
                        logger.error("url: $url")
                        logger.error("cUrl: ${request.cUrlString()}")
                        logger.error("response: $response")
                        logger.error(result.error.exception.localizedMessage)
                        result.error.exception.printStackTrace()
//                    close()
                        throw result.error.exception
                    }
                }
            } catch (connectError: ConnectException) {
                connectError.printStackTrace()
                sendErrors++
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun messageFlow() = channelFlow<ApiMessage> { //channel ->
        launch(context = Dispatchers.IO + CoroutineName("msgReceiver")) {
            loop@ while (isActive) {
                logger.info("opening connection")
                val url = "${config.url}/api/stream"
                val (request, response, result) = keepOpenManager.request(Method.GET, url)
                    .apply {
                        if (config.token.isNotEmpty()) {
                            headers["Authorization"] = "Bearer ${config.token}"
                        }
                    }
                    .responseObject(object : ResponseDeserializable<Unit> {
                        override fun deserialize(reader: Reader) {
                            //                        runBlocking(Dispatchers.IO + CoroutineName("msgDecoder")) {
                            logger.info("connected successfully")
                            connectErrors = 0
                            reconnectCooldown = 0

                            reader.useLines { lines ->
                                lines.forEach { line ->
                                    val msg = jsonNonstrict.parse(ApiMessage.serializer(), line)
                                    logger.debug("received: $msg")
                                    if (msg.event != "api_connect") {
                                        runBlocking {
                                            channel.send(msg)
                                        }
                                        // messageBroadcastInput.send(msg)
                                    }
                                }
                            }
                        }
                        // }
                    })

                when (result) {
                    is Result.Success -> {
                        logger.info("connection closed")
                    }
                    is Result.Failure -> {
                        connectErrors++
                        reconnectCooldown = connectErrors * 1000L
                        logger.error("connectErrors: $connectErrors")
                        logger.error("connection error")
                        logger.error("curl: ${request.cUrlString()}")
                        logger.error(result.error.localizedMessage)
                        result.error.exception.printStackTrace()
                        if (connectErrors >= 10) {
                            logger.error("Caught too many errors, closing bridge")
                            stop("Interrupting connection to matterbridge API due to accumulated connection errors")
                            break@loop
                        }
                    }
                }
                delay(reconnectCooldown) // reconnect delay in ms
            }
        }
    }
}



