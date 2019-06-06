package matterlink.mixin;

import matterlink.handlers.JoinLeaveHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
@SuppressWarnings("unused")
public class PlayerManagerMixin {
    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    void onPlayerConnect(ClientConnection clientConnection, ServerPlayerEntity entityPlayerServer, CallbackInfo ci) {
        System.out.println("mixin: player joined " + entityPlayerServer.getEntityName());
        JoinLeaveHandler.INSTANCE.handleJoin(entityPlayerServer.getEntityName());
    }
//    @Inject(at = @At("RETURN"), method = "broadcastChatMessage(Lnet/minecraft/text/TextComponent;Z)V")
//    void broadcastChatMessage(TextComponent textComponent, boolean b, CallbackInfo ci) {
//        if(b) return;
//        System.out.println("broadcast: " + textComponent.toString());
//    }



}
