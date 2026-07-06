/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2024 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents.forge.factory;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.injector.ChannelInjector;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.retrooper.packetevents.forge.handler.PacketDecoder;
import io.github.retrooper.packetevents.forge.handler.PacketEncoder;
import io.netty.channel.Channel;

import static com.github.retrooper.packetevents.PacketEvents.DECODER_NAME;
import static com.github.retrooper.packetevents.PacketEvents.ENCODER_NAME;

public class ForgeChannelInjector implements ChannelInjector {

    private final PacketEventsAPI<?> packetEventsAPI;

    public ForgeChannelInjector(PacketEventsAPI<?> packetEventsAPI) {
        this.packetEventsAPI = packetEventsAPI;
    }

    @Override
    public void inject() {
        // NO-OP: injection is handled via Mixin
    }

    @Override
    public void uninject() {
        // NO-OP: uninjection is handled via Mixin
    }

    @Override
    public boolean isPlayerSet(Object ch) {
        if (ch == null) return false;
        Channel channel = (Channel) ch;
        PacketEncoder encoder = (PacketEncoder) channel.pipeline().get(ENCODER_NAME);
        if (encoder != null && encoder.player != null) return true;

        PacketDecoder decoder = (PacketDecoder) channel.pipeline().get(DECODER_NAME);
        return decoder != null && decoder.player != null;
    }

    @Override
    public void updateUser(Object channel, User user) {
        if (!packetEventsAPI.getProtocolManager().hasChannel(channel)) {
            return;
        }
        Channel ch = (Channel) channel;
        PacketDecoder decoder = (PacketDecoder) ch.pipeline().get(DECODER_NAME);
        if (decoder != null) {
            decoder.user = user;
        }
        PacketEncoder encoder = (PacketEncoder) ch.pipeline().get(ENCODER_NAME);
        if (encoder != null) {
            encoder.user = user;
        }
    }

    @Override
    public void setPlayer(Object channel, Object player) {
        if (!packetEventsAPI.getProtocolManager().hasChannel(channel)) {
            return;
        }
        Channel ch = (Channel) channel;
        PacketDecoder decoder = (PacketDecoder) ch.pipeline().get(DECODER_NAME);
        if (decoder != null) {
            decoder.player = player;
        }
        PacketEncoder encoder = (PacketEncoder) ch.pipeline().get(ENCODER_NAME);
        if (encoder != null) {
            encoder.player = player;
        }
    }

    @Override
    public boolean isProxy() {
        return false;
    }

    @Override
    public PacketSide getPacketSide() {
        return PacketSide.SERVER;
    }
}