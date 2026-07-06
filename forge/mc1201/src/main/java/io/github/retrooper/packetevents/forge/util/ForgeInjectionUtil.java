package io.github.retrooper.packetevents.forge.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.UserConnectEvent;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.FakeChannelUtil;
import com.github.retrooper.packetevents.util.PacketEventsImplHelper;
import io.github.retrooper.packetevents.forge.handler.PacketDecoder;
import io.github.retrooper.packetevents.forge.handler.PacketEncoder;
import io.github.retrooper.packetevents.forge.manager.Forge1201PlayerManager;
import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

import java.util.List;

public class ForgeInjectionUtil {
    private static final String VIA_DECODER_NAME = "via-decoder";
    private static final String VIA_ENCODER_NAME = "via-encoder";

    public static void injectAtPipelineBuilder(ChannelPipeline pipeline, PacketSide pipelineSide) {
        Channel channel = pipeline.channel();

        User existing = PacketEvents.getAPI().getProtocolManager().getUser(channel);
        if (existing != null) {
            return;
        }

        PacketEvents.getAPI().getLogManager().debug("Game connected!");
        User user = new User(channel, ConnectionState.HANDSHAKING,
                null, new UserProfile(null, null));

        PacketEvents.getAPI().getProtocolManager().setUser(channel, user);

        UserConnectEvent connectEvent = new UserConnectEvent(user);
        PacketEvents.getAPI().getEventManager().callEvent(connectEvent);
        if (connectEvent.isCancelled()) {
            channel.unsafe().closeForcibly();
            return;
        }

        String decoderName = channel.pipeline().names().contains("inbound_config") ? "inbound_config" : "decoder";
        channel.pipeline().addBefore(decoderName, PacketEvents.DECODER_NAME, new PacketDecoder(pipelineSide, user, false));
        String encoderName = channel.pipeline().names().contains("outbound_config") ? "outbound_config" : "encoder";
        channel.pipeline().addBefore(encoderName, PacketEvents.ENCODER_NAME, new PacketEncoder(pipelineSide, user, false));
        if (PacketEvents.getAPI().getSettings().isPreViaInjection() && ViaVersionUtil.isAvailable(user)) {
            channel.pipeline().addBefore(VIA_DECODER_NAME, "pre-" + PacketEvents.DECODER_NAME, new PacketDecoder(pipelineSide, user, true));
            channel.pipeline().addBefore(VIA_ENCODER_NAME, "pre-" + PacketEvents.ENCODER_NAME, new PacketEncoder(pipelineSide, user, true));
        }
        channel.closeFuture().addListener((ChannelFutureListener) future ->
                PacketEventsImplHelper.handleDisconnection(user.getChannel(), user.getUUID()));
    }

    public static void removeIfExists(ChannelPipeline pipeline, String handlerName) {
        if (pipeline.get(handlerName) != null) {
            pipeline.remove(handlerName);
        }
    }

    public static void reorderHandlers(ChannelPipeline pipeline, PacketSide side) {
        if (PacketEvents.getAPI().getSettings().isDebugEnabled()) {
            PacketEvents.getAPI().getLogManager().debug("Pipeline before reorder: " + pipeline.names());
        }

        String preDecoderName = "pre-" + PacketEvents.DECODER_NAME;
        String peDecoderName = PacketEvents.DECODER_NAME;
        String preEncoderName = "pre-" + PacketEvents.ENCODER_NAME;
        String peEncoderName = PacketEvents.ENCODER_NAME;

        reorderDecoderPipeline(pipeline, preDecoderName, peDecoderName);
        reorderEncoderPipeline(pipeline, preEncoderName, peEncoderName);

        if (PacketEvents.getAPI().getSettings().isDebugEnabled()) {
            PacketEvents.getAPI().getLogManager().debug("Pipeline after reorder: " + pipeline.names());
        }
    }

    private static void reorderDecoderPipeline(ChannelPipeline pipeline,
                                               String preDecoderName, String peDecoderName) {
        List<String> names = pipeline.names();

        String decompressName = names.contains("decompress") ? "decompress" : null;
        String vanillaDecoderName = names.contains("inbound_config") ? "inbound_config" : "decoder";
        boolean hasVia = names.contains(VIA_DECODER_NAME);

        boolean viaInValidPosition = !hasVia || decompressName == null ||
                names.indexOf(VIA_DECODER_NAME) > names.indexOf(decompressName);

        ChannelHandler preDecoder = pipeline.get(preDecoderName);
        ChannelHandler peDecoder = pipeline.get(peDecoderName);

        boolean peNeedsReorder = false;
        if (peDecoder != null) {
            int peIdx = names.indexOf(peDecoderName);
            if (decompressName != null && !isAfter(names, peDecoderName, decompressName)) {
                peNeedsReorder = true;
            }
            if (hasVia && !isAfter(names, peDecoderName, VIA_DECODER_NAME)) {
                peNeedsReorder = true;
            }
            if (!isBefore(names, peDecoderName, vanillaDecoderName)) {
                peNeedsReorder = true;
            }
        }

        boolean preNeedsReorder = false;
        if (preDecoder != null) {
            if (decompressName != null && !isAfter(names, preDecoderName, decompressName)) {
                preNeedsReorder = true;
            }
            if (hasVia && viaInValidPosition && !isBefore(names, preDecoderName, VIA_DECODER_NAME)) {
                preNeedsReorder = true;
            }
        }

        if (!peNeedsReorder && !preNeedsReorder) {
            return;
        }

        if (preDecoder != null) pipeline.remove(preDecoderName);
        if (peDecoder != null) pipeline.remove(peDecoderName);

        names = pipeline.names();
        hasVia = names.contains(VIA_DECODER_NAME);
        decompressName = names.contains("decompress") ? "decompress" : null;
        vanillaDecoderName = names.contains("inbound_config") ? "inbound_config" : "decoder";

        viaInValidPosition = !hasVia || decompressName == null ||
                names.indexOf(VIA_DECODER_NAME) > names.indexOf(decompressName);

        if (peDecoder != null) {
            String addAfter = findLatestHandler(names, decompressName,
                    hasVia ? VIA_DECODER_NAME : null);

            if (addAfter != null) {
                pipeline.addAfter(addAfter, peDecoderName, peDecoder);
            } else {
                pipeline.addBefore(vanillaDecoderName, peDecoderName, peDecoder);
            }
        }

        names = pipeline.names();

        if (preDecoder != null) {
            if (hasVia && viaInValidPosition && names.contains(VIA_DECODER_NAME)) {
                pipeline.addBefore(VIA_DECODER_NAME, preDecoderName, preDecoder);
            } else if (decompressName != null && names.contains(decompressName)) {
                pipeline.addAfter(decompressName, preDecoderName, preDecoder);
            }
        }
    }

    private static void reorderEncoderPipeline(ChannelPipeline pipeline,
                                               String preEncoderName, String peEncoderName) {
        List<String> names = pipeline.names();

        String compressName = names.contains("compress") ? "compress" : null;
        String vanillaEncoderName = names.contains("outbound_config") ? "outbound_config" : "encoder";
        boolean hasVia = names.contains(VIA_ENCODER_NAME);

        boolean viaInValidPosition = !hasVia || compressName == null ||
                names.indexOf(VIA_ENCODER_NAME) > names.indexOf(compressName);

        ChannelHandler preEncoder = pipeline.get(preEncoderName);
        ChannelHandler peEncoder = pipeline.get(peEncoderName);

        boolean peNeedsReorder = false;
        if (peEncoder != null) {
            if (compressName != null && !isAfter(names, peEncoderName, compressName)) {
                peNeedsReorder = true;
            }
            if (hasVia && !isAfter(names, peEncoderName, VIA_ENCODER_NAME)) {
                peNeedsReorder = true;
            }
            if (!isBefore(names, peEncoderName, vanillaEncoderName)) {
                peNeedsReorder = true;
            }
        }

        boolean preNeedsReorder = false;
        if (preEncoder != null) {
            if (compressName != null && !isAfter(names, preEncoderName, compressName)) {
                preNeedsReorder = true;
            }
            if (hasVia && viaInValidPosition && !isBefore(names, preEncoderName, VIA_ENCODER_NAME)) {
                preNeedsReorder = true;
            }
        }

        if (!peNeedsReorder && !preNeedsReorder) {
            return;
        }

        if (preEncoder != null) pipeline.remove(preEncoderName);
        if (peEncoder != null) pipeline.remove(peEncoderName);

        names = pipeline.names();
        hasVia = names.contains(VIA_ENCODER_NAME);
        compressName = names.contains("compress") ? "compress" : null;
        vanillaEncoderName = names.contains("outbound_config") ? "outbound_config" : "encoder";

        viaInValidPosition = !hasVia || compressName == null ||
                names.indexOf(VIA_ENCODER_NAME) > names.indexOf(compressName);

        if (peEncoder != null) {
            String addAfter = findLatestHandler(names, compressName,
                    hasVia ? VIA_ENCODER_NAME : null);

            if (addAfter != null) {
                pipeline.addAfter(addAfter, peEncoderName, peEncoder);
            } else {
                pipeline.addBefore(vanillaEncoderName, peEncoderName, peEncoder);
            }
        }

        names = pipeline.names();

        if (preEncoder != null) {
            if (hasVia && viaInValidPosition && names.contains(VIA_ENCODER_NAME)) {
                pipeline.addBefore(VIA_ENCODER_NAME, preEncoderName, preEncoder);
            } else if (compressName != null && names.contains(compressName)) {
                pipeline.addAfter(compressName, preEncoderName, preEncoder);
            }
        }
    }

    private static boolean isAfter(List<String> names, String handler, String target) {
        int handlerIdx = names.indexOf(handler);
        int targetIdx = names.indexOf(target);
        if (handlerIdx == -1 || targetIdx == -1) return true;
        return handlerIdx > targetIdx;
    }

    private static boolean isBefore(List<String> names, String handler, String target) {
        int handlerIdx = names.indexOf(handler);
        int targetIdx = names.indexOf(target);
        if (handlerIdx == -1 || targetIdx == -1) return true;
        return handlerIdx < targetIdx;
    }

    private static String findLatestHandler(List<String> names, String... handlers) {
        String latest = null;
        int latestIdx = -1;

        for (String handler : handlers) {
            if (handler != null) {
                int idx = names.indexOf(handler);
                if (idx > latestIdx) {
                    latestIdx = idx;
                    latest = handler;
                }
            }
        }

        return latest;
    }

    public static void fireUserLoginEvent(Object player) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user == null) {
            Object channelObj = PacketEvents.getAPI().getPlayerManager().getChannel(player);

            if (!FakeChannelUtil.isFakeChannel(channelObj) &&
                    (!PacketEvents.getAPI().isTerminated() || PacketEvents.getAPI().getSettings().isKickIfTerminated())) {
                ((Forge1201PlayerManager) PacketEvents.getAPI().getPlayerManager()).disconnectPlayer(player, "PacketEvents failed to inject into a channel.");
            }
            return;
        }

        PacketEvents.getAPI().getEventManager().callEvent(new UserLoginEvent(user, player));
    }
}