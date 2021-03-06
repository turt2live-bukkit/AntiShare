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

package com.turt2live.antishare.utils;

import com.turt2live.antishare.io.BlockManager;
import com.turt2live.antishare.object.ASLocation;
import com.turt2live.antishare.object.attribute.ObjectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a transaction to occur in batch on a BlockManager.
 * This is intended for use where multiple blocks are to be updated
 * and may overwrite each other under some conditions. One example
 * for this class' purpose may be a piston-related event where the
 * blocks in the piston lineup could 'leapfrog' the block type.
 * <p/>
 * This is intentionally write-only instead of read/write/remove.
 *
 * @author turt2live
 */
public class BlockTypeTransaction {

    private Map<ASLocation, ObjectType> types = new HashMap<>();

    /**
     * Adds a location to this transaction
     *
     * @param location the location to process, cannot be null
     * @param type     the block type being set. Null equates to UNKNOWN
     */
    public void add(ASLocation location, ObjectType type) {
        if (location == null) throw new IllegalArgumentException();
        types.put(location, type == null ? ObjectType.UNKNOWN : type);
    }

    /**
     * Safely adds a location to the transaction. This will perform a lookup
     * on the internal cache to determine the existing stored type. If the
     * type is NOT of type UNKNOWN (or not found), the entry will NOT be
     * overwritten. Instead, only entries which do not exist or are UNKNOWN
     * will be written to the transaction.
     *
     * @param location the location to process, cannot be null
     * @param type     the block type being set. Null equates to UNKNOWN
     */
    public void safeAdd(ASLocation location, ObjectType type) {
        if (location == null) throw new IllegalArgumentException();

        ObjectType existing = types.containsKey(location) ? types.get(location) : ObjectType.UNKNOWN;

        if (existing == ObjectType.UNKNOWN) {
            add(location, type);
        }
    }

    /**
     * Commits the set to the block manager, clearing the internal cache
     * with it. This will take all added information and write them to the
     * block manager defined. Once completed, this will clear the internal
     * cache of information, therefore resetting the transaction's state.
     *
     * @param manager the manager to write to, cannot be null
     */
    public void commit(BlockManager manager) {
        if (manager == null) throw new IllegalArgumentException();

        for (Map.Entry<ASLocation, ObjectType> entry : types.entrySet()) {
            manager.setBlockType(entry.getKey(), entry.getValue());
        }

        types.clear();
    }

}
