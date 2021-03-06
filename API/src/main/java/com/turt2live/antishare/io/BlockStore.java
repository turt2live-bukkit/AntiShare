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

package com.turt2live.antishare.io;

import com.turt2live.antishare.object.ASLocation;
import com.turt2live.antishare.object.attribute.ObjectType;

import java.util.Map;

/**
 * Represents storage for block information
 *
 * @author turt2live
 */
public interface BlockStore {

    /**
     * Gets the type of a block type
     *
     * @param x the x location
     * @param y the y location
     * @param z the z location
     *
     * @return the block type
     */
    public ObjectType getType(int x, int y, int z);

    /**
     * Sets the type of a block
     *
     * @param x    the x location
     * @param y    the y location
     * @param z    the z location
     * @param type the new block type. Null is assumed to be {@link com.turt2live.antishare.object.attribute.ObjectType#UNKNOWN}
     */
    public void setType(int x, int y, int z, ObjectType type);

    /**
     * Gets the block type for a specified location
     *
     * @param location the location, cannot be null
     *
     * @return the block type
     */
    public ObjectType getType(ASLocation location);

    /**
     * Sets the type of a block
     *
     * @param location the location, cannot be null
     * @param type     the new block type. Null is assumed to be {@link com.turt2live.antishare.object.attribute.ObjectType#UNKNOWN}
     */
    public void setType(ASLocation location, ObjectType type);

    /**
     * Gets all the location/block type combinations known as a copied map
     *
     * @return the map of values
     */
    public Map<ASLocation, ObjectType> getAll();

    /**
     * Saves the store
     */
    public void save();

    /**
     * Loads the store
     */
    public void load();

    /**
     * Clears the store
     */
    public void clear();

    /**
     * Gets the time (in milliseconds) this block store was last accessed
     *
     * @return the last time this store was accessed
     */
    public long getLastAccess();

}
