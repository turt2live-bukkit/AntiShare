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

package com.turt2live.antishare.bukkit.abstraction.v1_5_R1;

import com.turt2live.antishare.bukkit.abstraction.event.AntiShareEatEvent;
import com.turt2live.antishare.object.ABlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.DoubleChestInventory;

import java.util.List;

public class Minecraft extends com.turt2live.antishare.bukkit.abstraction.v1_4_R1.Minecraft {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        AntiShareEatEvent asevent = new AntiShareEatEvent(event.getPlayer(), event.getItem());
        Bukkit.getServer().getPluginManager().callEvent(asevent);
        if (asevent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @Override
    public List<Material> getContainerTypes() {
        List<Material> list = super.getContainerTypes();
        list.add(Material.TRAPPED_CHEST);
        list.add(Material.DROPPER);
        list.add(Material.HOPPER);
        return list;
    }

    @Override
    public List<Material> getBrokenOnTop() {
        List<Material> list = super.getBrokenOnTop();
        list.add(Material.GOLD_PLATE);
        list.add(Material.IRON_PLATE);
        list.add(Material.REDSTONE_COMPARATOR_OFF);
        list.add(Material.REDSTONE_COMPARATOR_ON);
        list.add(Material.ACTIVATOR_RAIL);
        return list;
    }

    @Override
    public List<Material> getPistonBreak() {
        List<Material> materials = super.getPistonBreak();
        materials.add(Material.GOLD_PLATE);
        materials.add(Material.IRON_PLATE);
        materials.add(Material.REDSTONE_COMPARATOR_OFF);
        materials.add(Material.REDSTONE_COMPARATOR_ON);
        return materials;
    }

    @Override
    public ABlock.ChestType getChestType(Block block) {
        ABlock.ChestType type = super.getChestType(block);

        if (type == ABlock.ChestType.NONE) {
            if (block.getType() == Material.TRAPPED_CHEST) {
                BlockState state = block.getState();
                if (state instanceof Chest && ((Chest) state).getInventory() instanceof DoubleChestInventory) {
                    return ABlock.ChestType.DOUBLE_TRAPPED;
                }
                return ABlock.ChestType.TRAPPED;
            }
        }

        return type;
    }
}
