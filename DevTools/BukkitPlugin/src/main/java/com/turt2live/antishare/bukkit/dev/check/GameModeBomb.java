/*******************************************************************************
 * Copyright (C) 2014 Travis Ralston (turt2live)
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
 ******************************************************************************/

package com.turt2live.antishare.bukkit.dev.check;

import com.turt2live.antishare.bukkit.dev.AntiShare;
import com.turt2live.antishare.bukkit.dev.CheckBase;
import com.turt2live.antishare.engine.Engine;
import com.turt2live.antishare.object.ASLocation;
import com.turt2live.antishare.object.attribute.ObjectType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class GameModeBomb extends CheckBase {

    private ASLocation start;
    private ObjectType[] values = new ObjectType[] {ObjectType.CREATIVE, ObjectType.SURVIVAL, ObjectType.ADVENTURE, ObjectType.SPECTATOR};

    public GameModeBomb(AntiShare plugin, ASLocation start) {
        super(plugin);
        this.start = start;
    }

    @Override
    public void begin() {
        Bukkit.broadcastMessage(ChatColor.GREEN + "Starting: " + start);
        String world = "world";
        int creative = 0, survival = 0, adventure = 0, spectator = 0;
        int bombRadius = 50;
        for (int x = -bombRadius; x < bombRadius; x++) {
            for (int z = -bombRadius; z < bombRadius; z++) {
                for (int y = 0; y < 256; y++) {
                    ASLocation offset = new ASLocation(start.X + x, y, start.Z + z);
                    ObjectType type = values[AntiShare.RANDOM.nextInt(values.length)];
                    Engine.getInstance().getEngine(world).getBlockManager().setBlockType(offset, type);

                    switch (type) {
                        case CREATIVE:
                            creative++;
                            break;
                        case SURVIVAL:
                            survival++;
                            break;
                        case ADVENTURE:
                            adventure++;
                            break;
                        case SPECTATOR:
                            spectator++;
                            break;
                    }
                }
            }
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "Done (" + bombRadius + "r): " + creative + "c " + survival + "s " + adventure + "a " + spectator + "sp = " + (creative + survival + adventure + spectator));
    }

}
