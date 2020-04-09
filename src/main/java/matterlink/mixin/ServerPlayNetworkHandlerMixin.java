package matterlink.mixin;

import matterlink.handlers.ChatEvent;
import matterlink.handlers.ChatProcessor;
import matterlink.handlers.JoinLeaveHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements ServerPlayPacketListener {
    @Shadow public ServerPlayerEntity player;

    @Inject(
            at = @At(
                    value = "INVOKE",
//                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
                    target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Z)V",
                    shift = At.Shift.AFTER
            ),
            method = "onChatMessage"
    )
    public void onChatMessage(ChatMessageC2SPacket chatMessageC2SPacket, CallbackInfo ci) {
//        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
//        PlayerEntity player = this.player;
        String text = chatMessageC2SPacket.getChatMessage();
        text = StringUtils.normalizeSpace(text);

        ChatProcessor.INSTANCE.sendToBridge(player.getEntityName(), text, ChatEvent.PLAIN, player.getUuid());
    }

    @Inject(
            at = @At(
                    value = "HEAD"
            ),
            method = "onDisconnected"
    )
    public void onDisconnected(Text text, CallbackInfo ci) {
        String playername = player.getName().getString();
        String message = text.asFormattedString();
        JoinLeaveHandler.INSTANCE.handleLeave(playername, message);
        System.out.println(String.format("mixin disconnected: %s lost connection: %s", playername, message));
    }
}
