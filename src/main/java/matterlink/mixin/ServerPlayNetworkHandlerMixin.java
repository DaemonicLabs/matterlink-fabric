package matterlink.mixin;

import matterlink.handlers.ChatEvent;
import matterlink.handlers.ChatProcessor;
import matterlink.handlers.ServerChatHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ChatMessageServerPacket;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/TextComponent;Z)V"), method = "onChatMessage")
    public void onChatMessage(ChatMessageServerPacket chatMessageServerPacket, CallbackInfo ci) {
        ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
        PlayerEntity player = this.player;
        String text = chatMessageServerPacket.getChatMessage();
        text = StringUtils.normalizeSpace(text);

        ChatProcessor.INSTANCE.sendToBridge(player.getEntityName(), text, ChatEvent.PLAIN, player.getUuid());
    }
}
