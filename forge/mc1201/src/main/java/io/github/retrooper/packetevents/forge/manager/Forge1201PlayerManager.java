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

package io.github.retrooper.packetevents.forge.manager;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.github.retrooper.packetevents.impl.netty.manager.player.PlayerManagerAbstract;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class Forge1201PlayerManager extends PlayerManagerAbstract {

    private final PacketEventsAPI<?> packetEventsAPI;

    public Forge1201PlayerManager(PacketEventsAPI<?> packetEventsAPI) {
        this.packetEventsAPI = packetEventsAPI;
    }

    @Override
    public int getPing(@NotNull Object player) {
        return ((ServerPlayer) player).connection.latency;
    }

    @Override
    public Object getChannel(@NotNull Object player) {
        return ((ServerPlayer) player).connection.connection.channel;
    }

    @Override
    public User getUser(@NotNull Object player) {
        Object channel = getChannel(player);
        User user = packetEventsAPI.getProtocolManager().getUser(channel);
        if (user == null) {
            throw new IllegalStateException("Failed to get user for " + player);
        }
        return user;
    }

    @Override
    public ConnectionState getConnectionState(@NotNull Object player) throws IllegalStateException {
        return getUser(player).getConnectionState();
    }

    public void disconnectPlayer(@NotNull Object serverPlayer, @NotNull String message) {
        ((ServerPlayer) serverPlayer).connection.disconnect(Component.literal(message));
    }

    public void kickOnException(@NotNull Object serverPlayer, @NotNull String message) {
        ServerPlayer player = (ServerPlayer) serverPlayer;
        MinecraftServer server = player.server;
        server.execute(() -> disconnectPlayer(serverPlayer, message));
    }

    @Override
    public void sendPacket(@NotNull Object player, @NotNull Object byteBuf) {
        packetEventsAPI.getProtocolManager().sendPacket(getChannel(player), byteBuf);
    }

    @Override
    public void sendPacket(@NotNull Object player, @NotNull PacketWrapper<?> wrapper) {
        packetEventsAPI.getProtocolManager().sendPacket(getChannel(player), wrapper);
    }

    @Override
    public void sendPacketSilently(@NotNull Object player, @NotNull Object byteBuf) {
        packetEventsAPI.getProtocolManager().sendPacketSilently(getChannel(player), byteBuf);
    }

    @Override
    public void sendPacketSilently(@NotNull Object player, @NotNull PacketWrapper<?> wrapper) {
        packetEventsAPI.getProtocolManager().sendPacketSilently(getChannel(player), wrapper);
    }

    @Override
    public void writePacket(@NotNull Object player, @NotNull Object byteBuf) {
        packetEventsAPI.getProtocolManager().writePacket(getChannel(player), byteBuf);
    }

    @Override
    public void writePacket(@NotNull Object player, @NotNull PacketWrapper<?> wrapper) {
        packetEventsAPI.getProtocolManager().writePacket(getChannel(player), wrapper);
    }

    @Override
    public void writePacketSilently(@NotNull Object player, @NotNull Object byteBuf) {
        packetEventsAPI.getProtocolManager().writePacketSilently(getChannel(player), byteBuf);
    }

    @Override
    public void writePacketSilently(@NotNull Object player, @NotNull PacketWrapper<?> wrapper) {
        packetEventsAPI.getProtocolManager().writePacketSilently(getChannel(player), wrapper);
    }

    @Override
    public void receivePacket(Object player, Object byteBuf) {
        packetEventsAPI.getProtocolManager().receivePacket(getChannel(player), byteBuf);
    }

    @Override
    public void receivePacket(Object player, PacketWrapper<?> wrapper) {
        packetEventsAPI.getProtocolManager().receivePacket(getChannel(player), wrapper);
    }

    @Override
    public void receivePacketSilently(Object player, Object byteBuf) {
        packetEventsAPI.getProtocolManager().receivePacketSilently(getChannel(player), byteBuf);
    }

    @Override
    public void receivePacketSilently(Object player, PacketWrapper<?> wrapper) {
        packetEventsAPI.getProtocolManager().receivePacketSilently(getChannel(player), wrapper);
    }
}