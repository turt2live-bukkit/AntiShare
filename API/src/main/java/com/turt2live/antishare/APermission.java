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

package com.turt2live.antishare;

import com.turt2live.antishare.engine.list.RejectionList;

/**
 * AntiShare Permission Nodes
 *
 * @author turt2live
 */
public final class APermission {

    /**
     * Allows the use of the AntiShare tools
     */
    public static final String TOOLS = "antishare.tools";
    /**
     * Ignores block placement tracking for players with this permission node
     */
    public static final String FREE_PLACE = "antishare.freeplace";
    /**
     * Ignores misc checks for denial when breaking blocks
     */
    public static final String FREE_BREAK = "antishare.freebreak";
    /**
     * Ignores inter-gamemode checks when interacting with blocks/entities
     */
    public static final String FREE_TOUCH = "antishare.freetouch";
    /**
     * If enabled, the player will get notifications of illegal actions performed by players
     */
    public static final String GET_ALERTS = "antishare.alerts";
    /**
     * Allows the ability to reload the plugin
     */
    public static final String PLUGIN_RELOAD = "antishare.reload";
    /**
     * Ignores inter-gamemode checks when attacking
     */
    public static final String FREE_ATTACK = "antishare.freeattack";
    /**
     * Ignores world splits
     */
    public static final String FREE_ROAM = "antishare.freeroam";

    private APermission() {
    }

    /**
     * Gets the permission node for the specified settings. This does not include the
     * final period character.
     *
     * @param allow true for generating an "allow" node, false otherwise
     * @param list  the list to use, cannot be null
     *
     * @return the generated node
     */
    public static String getPermissionNode(boolean allow, RejectionList.ListType list) {
        if (list == null) throw new IllegalArgumentException();

        return "antishare." + (allow ? "allow." : "deny.") + list.name().toLowerCase();
    }

}
