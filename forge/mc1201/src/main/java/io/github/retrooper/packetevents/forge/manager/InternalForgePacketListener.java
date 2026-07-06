package io.github.retrooper.packetevents.forge.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.LogManager;
import com.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;

public class InternalForgePacketListener extends com.github.retrooper.packetevents.manager.InternalPacketListener {

    public InternalForgePacketListener() {
        super();
    }

    public InternalForgePacketListener(PacketListenerPriority priority) {
        super(priority);
    }

    public InternalForgePacketListener(PacketListenerPriority priority, boolean preVia) {
        super(priority, preVia);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Handshaking.Client.HANDSHAKE) {
            if (isPreVia()) return;
            User user = event.getUser();
            WrapperHandshakingClientHandshake packet = new WrapperHandshakingClientHandshake(event);
            ClientVersion clientVersion = packet.getClientVersion();
            ConnectionState state = packet.getNextConnectionState();

            String feature;
            if (ViaVersionUtil.isAvailable(user)) {
                clientVersion = ClientVersion.getById(ViaVersionUtil.getProtocolVersion(user));
                feature = "ViaVersion";
            } else {
                feature = null;
            }

            LogManager logger = PacketEvents.getAPI().getLogManager();
            if (logger.isDebug()) {
                logger.debug("Processed handshake for " + event.getAddress() + ": "
                        + state.name() + " / " + packet.getClientVersion().getReleaseName()
                        + (feature != null ? " (using " + feature + ")" : ""));
            }

            user.setClientVersion(clientVersion);
            user.setConnectionState(state);
        } else {
            super.onPacketReceive(event);
        }
    }
}