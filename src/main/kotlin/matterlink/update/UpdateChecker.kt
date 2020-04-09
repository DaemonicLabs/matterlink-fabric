package matterlink.update

import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import com.github.kittinunf.result.Result
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import matterlink.Matterlink
import matterlink.MessageHandler
import matterlink.api.ApiMessage
import matterlink.config.cfg
import matterlink.handlers.ChatEvent
import matterlink.handlers.LocationHandler
import matterlink.jenkins.JenkinsServer
import matterlink.jsonNonstrict
import matterlink.logger

object UpdateChecker : CoroutineScope {
    override val coroutineContext = Job() + CoroutineName("UpdateChecker")

    suspend fun check() {
        if (cfg.update.enable) {
            run()
        }
    }

    private suspend fun run() {
        if (Matterlink.jenkinsBuildNumber > 0) {
            val server = JenkinsServer("https://ci.elytradev.com")
            val job = server.getJob("elytra/MatterLink/master", "MatterLink/${Matterlink.modVersion}")
                ?: run {
                    logger.error("failed obtaining job: elytra/MatterLink/master")
                    return
                }
            //TODO: add job name to constants at build time
            val build = job.lastSuccessfulBuild ?: run {
                logger.error("no successful build found")
                return
            }
            with(build) {
                when {
                    number > Matterlink.buildNumber -> {
                        logger.warn("Mod out of date! New build $number available at $url")
                        val difference = number - Matterlink.buildNumber
                        LocationHandler.sendToLocations(
                            msg = "MatterLink out of date! You are $difference builds behind! Please download new version from $url",
//                            x = 0, y = 0, z = 0, dimension = null,
                            event = ChatEvent.STATUS,
                            cause = "MatterLink update notice"
                        )
                    }
                    number < Matterlink.buildNumber -> logger.error("lastSuccessfulBuild: $number is older than installed build: ${Matterlink.buildNumber}")
                    else -> logger.info("you are up to date")
                }
            }
            return
        }
        if (Matterlink.modVersion.contains("-dev")) {
            logger.debug("Not checking updates on developer build")
            return
        }

        logger.info("Checking for new versions...")
        val (request, response, result) = with(Matterlink) {
            val useragent =
                "MatterLink/$modVersion Fabric/$mcVersion-$fabricVersion (https://github.com/DaemonicLabs/matterlink-fabric)"
            logger.debug("setting User-Agent: '$useragent'")

            "https://curse.nikky.moe/api/addon/287323/files".httpGet()
                .header("User-Agent" to useragent)
                .responseObject(kotlinxDeserializerOf(
                    loader = CurseFile.serializer().list,
                    json = jsonNonstrict
                ))
        }

        val apiUpdateList = when (result) {
            is Result.Success -> {
                result.value
            }
            is Result.Failure -> {
                logger.error("Could not check for updates!")
                logger.error("cUrl: ${request.cUrlString()}")
                logger.error("request: $request")
                logger.error("response: $response")
                logger.error("response: ${result.error}")
                return
            }
        }
            .filter { it.fileStatus == "SemiNormal" && it.gameVersion.contains(Matterlink.mcVersion) }

        val modVersionChunks = Matterlink.modVersion
            .substringBefore("-dev")
            .substringBefore("-build")
            .split('.')
            .map {
                it.toInt()
            }

        val possibleUpdates = mutableListOf<CurseFile>()
        apiUpdateList.forEach { curseFile ->
            logger.debug(curseFile.toString())
            val version = curseFile.fileName.substringAfterLast("-").split('.').map { it.toInt() }
            var bigger = false
            version.forEachIndexed { index, chunk ->
                if (!bigger) {
                    val currentChunk = modVersionChunks.getOrNull(index) ?: 0
                    logger.debug("$chunk > $currentChunk")
                    if (chunk < currentChunk)
                        return@forEach

                    bigger = chunk > currentChunk
                }
            }
            if (bigger) {
                possibleUpdates += curseFile
            }
        }
        if (possibleUpdates.isEmpty()) return
        val latest = possibleUpdates[0]

        possibleUpdates.sortByDescending { it.fileName.substringAfter(" ") }
        val count = possibleUpdates.count()
        val version = if (count == 1) "version" else "versions"

        logger.info("Matterlink out of date! You are $count $version behind")
        possibleUpdates.forEach {
            logger.info("version: ${it.fileName} download: ${it.downloadURL}")
        }

        logger.warn("Mod out of date! New $version available at ${latest.downloadURL}")
        MessageHandler.transmit(
            ApiMessage(
                text = "MatterLink out of date! You are $count $version behind! Please download new version from ${latest.downloadURL}"
            )
        )
    }
}