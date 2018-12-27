package matterlink.mixin;

import matterlink.handlers.DeathHandler;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
@SuppressWarnings("unused")
abstract class ServerPlayerEntityMixin {
//    @Inject(at = @At("HEAD"), method = "trySleep")
//    public void onTrySleep(BlockPos blockPos, CallbackInfoReturnable<PlayerEntity.SleepResult> cir) {
//        PlayerEntity player = (PlayerEntity) (Object) this;
//        String name = player.getEntityName();
//        System.out.println("player: " + name + " tries to sleep at " + blockPos);
//    }

    // TODO: register custom ENTITY_KILLED_PLAYER criterium

    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        String name = player.getEntityName();
        // TODO: get real deathmessage
        String deathMessage = damageSource.getDeathMessage(player).getFormattedText();
        DeathHandler.INSTANCE.handleDeath(player.getEntityName(), deathMessage, damageSource.name);
    }
}
