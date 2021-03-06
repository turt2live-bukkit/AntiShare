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

package com.turt2live.antishare.configuration.groups;

import com.turt2live.antishare.object.APlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages groups
 *
 * @author turt2live
 */
// TODO: Unit test
public abstract class GroupManager {

    protected final ConcurrentMap<String, Group> groups = new ConcurrentHashMap<>();
    protected MainGroup mainGroup;

    /**
     * Loads all groups from the storage system
     */
    public abstract void loadAll();

    /**
     * Gets the main group
     *
     * @return the main group
     */
    public MainGroup getMainGroup() {
        return mainGroup;
    }

    /**
     * Gets a group by name
     *
     * @param name the group name
     *
     * @return the group, or null if not found
     */
    public Group getGroup(String name) {
        if (name == null) return null;
        if (name.equals(mainGroup.getName())) return mainGroup;
        return groups.get(name);
    }

    /**
     * Clears all groups from the system, including the main group
     */
    public void clear() {
        groups.clear();
        mainGroup = null;
    }

    /**
     * Gets a listing of all groups a specified group inherits from
     *
     * @param group the group to get the inheritance tree from. Returns an empty list on null input
     *
     * @return the inherited groups, never null but may be empty
     */
    public List<Group> getInheritances(Group group) {
        if (group == null) return new ArrayList<>();

        List<Group> groups = new ArrayList<>();

        for (String name : group.getInheritedGroups()) {
            groups.addAll(getInheritances(getGroup(name)));
        }

        return groups;
    }

    /**
     * Gets all the groups for the specified world
     *
     * @param world           the world to lookup, null returns an empty list
     * @param includeDisabled if true, disabled groups will be included in the result set
     *
     * @return the applicable groups, or an empty list
     */
    public List<Group> getGroupsForWorld(String world, boolean includeDisabled) {
        if (world == null) return new ArrayList<>();

        List<Group> groups = new ArrayList<>();
        for (Group group : this.groups.values()) {
            List<String> worlds = group.getApplicableWorlds();
            if (worlds.contains("all") || worlds.contains(world))
                if (group.isEnabled() || includeDisabled) groups.add(group);
        }

        List<String> worlds = mainGroup.getApplicableWorlds();
        if (worlds.contains("all") || worlds.contains(world))
            if (mainGroup.isEnabled() || includeDisabled) groups.add(mainGroup);

        return groups;
    }

    /**
     * Gets a list of all groups
     *
     * @param includeDisabled if true, disabled groups will be included in the result set
     *
     * @return the applicable groups, or an empty list
     */
    public List<Group> getAllGroups(boolean includeDisabled) {
        List<Group> groups = new ArrayList<>();

        for (Group group : this.groups.values()) {
            if (group.isEnabled() || includeDisabled) groups.add(group);
        }

        if (mainGroup.isEnabled() || includeDisabled) groups.add(mainGroup);

        return groups;
    }

    /**
     * Gets a listing of applicable groups to a player, including inherited groups. This will
     * only include groups for the player's current world.
     *
     * @param player          the player to lookup, cannot be null
     * @param includeDisabled if true, disabled groups will be included in the result set
     *
     * @return the list of groups. May be empty but never null
     */
    public List<Group> getGroupsForPlayer(APlayer player, boolean includeDisabled) {
        List<Group> groups = getAllGroupsForPlayer(player, includeDisabled);
        List<Group> applicable = new ArrayList<>();

        for (Group group : groups) {
            List<String> worlds = group.getApplicableWorlds();
            if (contains(worlds, "all") || contains(worlds, player.getWorld().getName())) {
                applicable.add(group);
            }
        }

        return applicable;
    }

    private boolean contains(List<String> strings, String value) {
        for (String s : strings) {
            if (s.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    /**
     * Gets a listing of applicable groups to a player, including inherited groups
     *
     * @param player          the player to lookup, cannot be null
     * @param includeDisabled if true, disabled groups will be included in the result set
     *
     * @return the list of groups. May be empty but never null
     */
    public List<Group> getAllGroupsForPlayer(APlayer player, boolean includeDisabled) {
        List<Group> groups = new ArrayList<>();

        for (Group group : this.groups.values()) {
            if (player.hasPermission(group.getPermission())) {
                if (group.isEnabled() || includeDisabled) {
                    addIfNotFound(groups, group);
                    List<Group> var = getInheritances(group);
                    addIfNotFound(groups, var.toArray(new Group[var.size()])); // All inherited groups are automatic
                }
            }
        }

        if (player.hasPermission(mainGroup.getPermission()) && (mainGroup.isEnabled() || includeDisabled)) {
            addIfNotFound(groups, mainGroup);
            List<Group> var = getInheritances(mainGroup);
            addIfNotFound(groups, var.toArray(new Group[var.size()])); // All inherited groups are automatic
        }

        if (groups.size() <= 0) {
            addIfNotFound(groups, mainGroup);
            List<Group> var = getInheritances(mainGroup);
            addIfNotFound(groups, var.toArray(new Group[var.size()])); // All inherited groups are automatic
        }

        return groups;
    }

    private void addIfNotFound(List<Group> list, Group... groups) {
        for (Group group : groups) {
            if (!list.contains(group)) list.add(group);
        }
    }
}
