package matterlink.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import matterlink.Matterlink;
import net.minecraft.class_3807;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(MinecraftDedicatedServer.class)
@SuppressWarnings("unused")
public class MinecraftDedicatedServerMixin {
    @Inject(
            method = "<init>",
            at = @At(value = "RETURN")
    )
    private void init(File file, class_3807 class_3807, DataFixer dataFixer, YggdrasilAuthenticationService yggdrasilAuthenticationService, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache userCache, CallbackInfo ci) {
        MinecraftDedicatedServer minecraftDedicatedServer = (MinecraftDedicatedServer) (Object) this;
        Matterlink.INSTANCE.setServer(minecraftDedicatedServer);
    }
}
