package io.github.retrooper.packetevents.forge.mixin;

import com.github.retrooper.packetevents.PacketEvents;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.retrooper.packetevents.forge.util.ForgeInjectionUtil;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {

    @Inject(
            method = "placeNewPlayer",
            at = @At("HEAD")
    )
    private void onPlayerConnect(
            CallbackInfo ci,
            @Local(ordinal = 0, argsOnly = true) Connection connection,
            @Local(ordinal = 0, argsOnly = true) ServerPlayer player
    ) {
        PacketEvents.getAPI().getInjector().setPlayer(connection.channel, player);
    }

    @Inject(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onPlayerLogin(
            CallbackInfo ci,
            @Local(ordinal = 0, argsOnly = true) ServerPlayer player
    ) {
        ForgeInjectionUtil.fireUserLoginEvent(player);
    }
}