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

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.injector.ChannelInjector;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.netty.NettyManager;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import com.github.retrooper.packetevents.util.PEVersions;
import io.github.retrooper.packetevents.forge.manager.Forge1201PlayerManager;
import io.github.retrooper.packetevents.forge.manager.ForgeProtocolManager;
import io.github.retrooper.packetevents.forge.manager.ForgeServerManager;
import io.github.retrooper.packetevents.forge.manager.InternalForgePacketListener;
import io.github.retrooper.packetevents.impl.netty.NettyManagerImpl;
import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

import java.util.Locale;

public class ForgePacketEventsAPI extends PacketEventsAPI<ModContainer> {

    private final String modId;
    private final PacketEventsSettings settings;
    private final ProtocolManager protocolManager;
    private final ServerManager serverManager;
    private final PlayerManager playerManager;
    private final ChannelInjector injector;
    private final NettyManager nettyManager = new NettyManagerImpl();

    private boolean loaded;
    private boolean initialized;
    private boolean terminated;

    public ForgePacketEventsAPI(String modId) {
        this(modId, new PacketEventsSettings());
    }

    public ForgePacketEventsAPI(String modId, PacketEventsSettings settings) {
        this.modId = modId;
        this.settings = settings;
        this.protocolManager = new ForgeProtocolManager(this);
        this.serverManager = new ForgeServerManager();
        this.playerManager = new Forge1201PlayerManager(this);
        this.injector = new ForgeChannelInjector(this);
    }

    @Override
    public void load() {
        if (this.loaded) {
            return;
        }

        String id = this.modId.toLowerCase(Locale.ROOT);
        PacketEvents.IDENTIFIER = "pe-" + id;
        PacketEvents.ENCODER_NAME = "pe-encoder-" + id;
        PacketEvents.DECODER_NAME = "pe-decoder-" + id;
        PacketEvents.CONNECTION_HANDLER_NAME = "pe-connection-handler-" + id;
        PacketEvents.SERVER_CHANNEL_HANDLER_NAME = "pe-connection-initializer-" + id;

        super.load();
        this.loaded = true;

        this.getLogManager().info("Loaded packetevents v" + PEVersions.RAW);
    }

    @Override
    protected void registerInternalListener() {
        this.getEventManager().registerListener(new InternalForgePacketListener());
        this.getEventManager().registerListener(new InternalForgePacketListener(
                PacketListenerPriority.LOWEST, true));
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public void init() {
        this.load();
        if (this.initialized) {
            return;
        }

        if (this.settings.shouldCheckForUpdates()) {
            this.getUpdateChecker().handleUpdateCheck();
        }

        PacketType.Play.Client.load();
        PacketType.Play.Server.load();

        if (!"true".equalsIgnoreCase(System.getenv("PE_IGNORE_INCOMPATIBILITY"))) {
            checkCompatibility();
        }

        this.initialized = true;
    }

    private void checkCompatibility() {
        ViaVersionUtil.checkIfViaIsPresent();
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public void terminate() {
        if (!this.initialized) {
            return;
        }
        super.terminate();
        this.initialized = false;
        this.terminated = true;
    }

    @Override
    public boolean isTerminated() {
        return this.terminated;
    }

    @Override
    public ModContainer getPlugin() {
        return ModList.get().getModContainerById(this.modId).orElse(null);
    }

    @Override
    public ProtocolManager getProtocolManager() {
        return this.protocolManager;
    }

    @Override
    public ServerManager getServerManager() {
        return this.serverManager;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    @Override
    public ChannelInjector getInjector() {
        return this.injector;
    }

    @Override
    public PacketEventsSettings getSettings() {
        return this.settings;
    }

    @Override
    public NettyManager getNettyManager() {
        return this.nettyManager;
    }
}