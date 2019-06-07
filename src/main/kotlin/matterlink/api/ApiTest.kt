package matterlink.api

import kotlinx.coroutines.runBlocking
import matterlink.MessageHandler
import matterlink.config.BaseConfig
import matterlink.config.cfg

fun main() = runBlocking {
    cfg = BaseConfig.MatterLinkConfig(connect = BaseConfig.ConnectOptions(gateway = "matterlink"))
    MessageHandler.config = Config(
        url = "http://nikky.moe:4242",
        token = "secret",
        gateway = "matterlink",
        systemUser = "Test"
    )
    MessageHandler.start("Test started, connecting to matterbridge API", false)
//    MessageHandler.transmit(ApiMessage(text = "test"))

//    MessageHandler.transmit(
//        ApiMessage(
//            "username",
//            "test message",
//            "matterlink"
//        )
//    )
    Unit
}