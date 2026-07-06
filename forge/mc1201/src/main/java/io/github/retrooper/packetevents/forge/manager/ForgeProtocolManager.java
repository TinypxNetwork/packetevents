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
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ProtocolVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.retrooper.packetevents.impl.netty.manager.protocol.ProtocolManagerAbstract;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ForgeProtocolManager extends ProtocolManagerAbstract {

    private final Map<UUID, Object> channels = new ConcurrentHashMap<>();
    private final Map<Object, User> users = new ConcurrentHashMap<>();
    private final PacketEventsAPI<?> packetEventsAPI;

    public ForgeProtocolManager(PacketEventsAPI<?> packetEventsAPI) {
        this.packetEventsAPI = packetEventsAPI;
    }

    @Override
    public ProtocolVersion getPlatformVersion() {
        return ProtocolVersion.UNKNOWN;
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public Collection<Object> getChannels() {
        return channels.values();
    }

    @Override
    public User getUser(Object channel) {
        Object pipeline = ChannelHelper.getPipeline(channel);
        return users.get(pipeline);
    }

    @Override
    public User removeUser(Object channel) {
        Object pipeline = ChannelHelper.getPipeline(channel);
        return users.remove(pipeline);
    }

    @Override
    public void setUser(Object channel, User user) {
        synchronized (channel) {
            Object pipeline = ChannelHelper.getPipeline(channel);
            users.put(pipeline, user);
        }
        packetEventsAPI.getInjector().updateUser(channel, user);
    }

    @Override
    public Object getChannel(UUID uuid) {
        return channels.get(uuid);
    }

    @Override
    public void setChannel(UUID uuid, Object channel) {
        channels.put(uuid, channel);
    }

    @Override
    public boolean hasChannel(Object channel) {
        return channels.containsValue(channel);
    }
}