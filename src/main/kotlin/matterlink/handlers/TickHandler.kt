package matterlink.handlers

import kotlinx.coroutines.runBlocking
import matterlink.update.UpdateChecker
import net.minecraft.server.MinecraftServer
import net.fabricmc.fabric.api.event.server.ServerTickCallback
/**
 * Created by nikky on 21/02/18.
 * @author Nikky
 * @version 1.0
 */
object TickHandler : ServerTickCallback {

    var tickCounter = 0
        private set
    private var accumulator = 0
    private const val updateInterval = 12 * 60 * 60 * 20

    override fun tick(t: MinecraftServer) = runBlocking {
        handleTick()
    }

    suspend fun handleTick() {
        tickCounter++
//        if (tickCounter % 100 == 0) {
//            MessageHandlerInst.checkConnection()
//        }

        ServerChatHandler.writeIncomingToChat()

        if (accumulator++ > updateInterval) {
            accumulator -= updateInterval
            UpdateChecker.check()
        }
    }
}