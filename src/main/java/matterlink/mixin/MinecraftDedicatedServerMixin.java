package matterlink.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import matterlink.Matterlink;
import matterlink.MessageHandler;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(MinecraftDedicatedServer.class)
@SuppressWarnings("unused")
public class MinecraftDedicatedServerMixin {
    @Inject(
            at = @At(value = "RETURN"),
            method = "<init>"
    )
    private void init(File file_1, ServerPropertiesLoader serverPropertiesLoader_1, DataFixer dataFixer_1, YggdrasilAuthenticationService yggdrasilAuthenticationService_1, MinecraftSessionService minecraftSessionService_1, GameProfileRepository gameProfileRepository_1, UserCache userCache_1, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory_1, String string_1, CallbackInfo ci) {
        MinecraftDedicatedServer minecraftDedicatedServer = (MinecraftDedicatedServer) (Object) this;
        Matterlink.INSTANCE.setServer(minecraftDedicatedServer);
    }

    @Inject(
            at = @At(value = "HEAD"),
            method = "shutdown"
    )
    private void shutdown(CallbackInfo ci) {
        Matterlink.INSTANCE.onShutdown();
    }

    @Inject(
            at = @At(value = "RETURN"),
            method = "setupServer"
    )
    private void setupServer(CallbackInfoReturnable ci) {
        MessageHandler.INSTANCE.afterServerSetup();
    }

}
