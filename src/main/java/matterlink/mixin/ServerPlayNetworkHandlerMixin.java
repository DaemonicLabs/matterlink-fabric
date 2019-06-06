package matterlink.mixin;

import matterlink.handlers.ChatEvent;
import matterlink.handlers.ChatProcessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onChatMessage(Lnet/minecraft/server/network/packet/ChatMessageC2SPacket;)V"), method = "onChatMessage")
    public void onChatMessage(ChatMessageC2SPacket chatMessageC2SPacket, CallbackInfo ci) {
        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
        PlayerEntity player = this.player;
        String text = chatMessageC2SPacket.getChatMessage();
        text = StringUtils.normalizeSpace(text);

        ChatProcessor.INSTANCE.sendToBridge(player.getEntityName(), text, ChatEvent.PLAIN, player.getUuid());
    }
}
