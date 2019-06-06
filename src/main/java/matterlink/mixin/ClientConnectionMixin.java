package matterlink.mixin;

import matterlink.handlers.JoinLeaveHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
@SuppressWarnings("unused")
abstract class ClientConnectionMixin {
    @Inject(at = @At("HEAD"), method = "handleDisconnection")
    public void onHandleDisconnection(CallbackInfo ci) {
        ClientConnection connection = (ClientConnection) (Object) this;
        Component reason = connection.getDisconnectReason();
        String message = "unknown reason";
        if(reason != null) {
            message = reason.getFormattedText();
        }
        JoinLeaveHandler.INSTANCE.handleLeave("unknown", message);
        System.out.println();
        System.out.println("mixin: player left " + connection.getAddress() + "reason: " + reason);
    }
}
