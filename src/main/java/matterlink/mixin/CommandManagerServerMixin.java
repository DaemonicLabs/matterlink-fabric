package matterlink.mixin;

import com.mojang.brigadier.CommandDispatcher;
import matterlink.commands.TestCommand;
import net.minecraft.server.command.ServerCommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommandManager.class)
@SuppressWarnings("unused")
public class CommandManagerServerMixin {
    @Shadow
    @Final
    public CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(at = @At("RETURN"), method = "<init>(Z)V")
    private void init(CallbackInfo info) {
        TestCommand.INSTANCE.register(dispatcher);
    }
}