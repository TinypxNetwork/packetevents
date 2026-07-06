package io.github.retrooper.packetevents.forge.mixin;

import com.github.retrooper.packetevents.protocol.PacketSide;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.retrooper.packetevents.forge.util.ForgeInjectionUtil;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = 1500)
public class ClientConnectionMixin {
    @Inject(
            method = "configureSerialization",
            at = @At("TAIL"),
            require = 1
    )
    private static void configureSerialization(
            CallbackInfo ci,
            @Local(ordinal = 0, argsOnly = true) ChannelPipeline pipeline,
            @Local(ordinal = 0, argsOnly = true) PacketFlow flow
    ) {
        PacketSide side = switch (flow) {
            case CLIENTBOUND -> PacketSide.CLIENT;
            case SERVERBOUND -> PacketSide.SERVER;
        };
        ForgeInjectionUtil.injectAtPipelineBuilder(pipeline, side);
    }
}