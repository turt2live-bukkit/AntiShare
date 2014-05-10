/*******************************************************************************
 * Copyright (C) 2014 Travis Ralston (turt2live)
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.turt2live.antishare.bukkit.abstraction.v1_7_R3;

import com.google.common.base.Charsets;
import com.turt2live.hurtle.uuid.UUIDServiceProvider;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class Minecraft extends com.turt2live.antishare.bukkit.abstraction.v1_7_R2.Minecraft {

    @Override
    public UUID getUUID(OfflinePlayer player) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        UUID uuid = player.getUniqueId();
        UUID badUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
        UUID badName = UUID.nameUUIDFromBytes("InvalidUsername".getBytes(Charsets.UTF_8));
        if (uuid != null && (uuid.equals(badUUID) || uuid.equals(badName))) return uuid;

        checkCache();
        if (BY_NAME.containsKey(player.getName())) {
            return BY_NAME.get(player.getName()).uuid;
        }
        uuid = UUIDServiceProvider.getUUID(player.getName());
        if (uuid != null) {
            BY_NAME.put(player.getName(), new UUIDStore(uuid, System.currentTimeMillis()));
        }
        return uuid;
    }
}