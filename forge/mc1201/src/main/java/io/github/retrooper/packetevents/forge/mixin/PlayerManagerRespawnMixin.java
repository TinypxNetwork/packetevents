package io.github.retrooper.packetevents.forge.mixin;

import com.github.retrooper.packetevents.PacketEvents;
import io.netty.channel.Channel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerManagerRespawnMixin {
    @Inject(
            method = "respawn",
            at = @At("RETURN"),
            require = 1
    )
    private void postRespawn(CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer player = cir.getReturnValue();
        Channel channel = player.connection.connection.channel;
        PacketEvents.getAPI().getInjector().setPlayer(channel, player);
    }
}