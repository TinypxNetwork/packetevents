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
import com.github.retrooper.packetevents.settings.PacketEventsSettings;

public final class ForgePacketEventsBuilder {

    private static PacketEventsAPI<?> API_INSTANCE;

    private ForgePacketEventsBuilder() {
    }

    public static void clearBuildCache() {
        API_INSTANCE = null;
    }

    public static PacketEventsAPI<?> build(String modId) {
        if (API_INSTANCE == null) {
            API_INSTANCE = buildNoCache(modId);
        }
        return API_INSTANCE;
    }

    public static PacketEventsAPI<?> build(String modId, PacketEventsSettings settings) {
        if (API_INSTANCE == null) {
            API_INSTANCE = buildNoCache(modId, settings);
        }
        return API_INSTANCE;
    }

    public static PacketEventsAPI<?> buildNoCache(String modId) {
        return buildNoCache(modId, new PacketEventsSettings());
    }

    public static PacketEventsAPI<?> buildNoCache(String modId, PacketEventsSettings settings) {
        return new ForgePacketEventsAPI(modId, settings);
    }
}