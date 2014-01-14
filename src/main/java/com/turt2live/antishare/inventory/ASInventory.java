package com.turt2live.antishare.inventory;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.turt2live.antishare.AntiShare;
import com.turt2live.antishare.DebugLogger;
import com.turt2live.antishare.io.GameModeIdentity;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AntiShare Inventory
 *
 * @author turt2live
 */
public class ASInventory implements Cloneable {

    /**
     * Inventory type
     *
     * @author turt2live
     */
    public static enum InventoryType {
        PLAYER("players"),
        REGION("regions"),
        TEMPORARY("temporary"),
        ENDER("ender");

        private String relativeFolderName;

        private InventoryType(String relativeFolderName) {
            this.relativeFolderName = relativeFolderName;
        }

        /**
         * Gets the relative folder name
         *
         * @return the folder
         */
        public String getRelativeFolderName() {
            return relativeFolderName;
        }
    }

    private static File DATA_FOLDER = null;
    public static final int SIZE = (9 * 4) + 4;
    public static final int SIZE_HIGH_9 = (9 * 4) + 9;
    public static final ItemStack AIR = new ItemStack(Material.AIR);
    public static final String VERSION = "2";
    public static final ASInventory EMPTY = null;
    public static final long CACHE_EXPIRE = 60000;

    public GameMode gamemode;
    public final String owner;
    public String world;
    public final InventoryType type;
    final Map<Integer, ItemStack> items = new HashMap<Integer, ItemStack>();
    private boolean isCache = false;
    private long cacheTime = 0;

    ASInventory(GameMode gamemode, String owner, String world, InventoryType type) {
        this.gamemode = gamemode;
        this.owner = owner;
        this.world = world;
        this.type = type;
    }

    public void setCache(boolean cache) {
        this.isCache = cache;
        if (cache) cacheTime = System.currentTimeMillis();
    }

    public boolean isCache() {
        return isCache;
    }

    public long getCacheTime() {
        return System.currentTimeMillis() - cacheTime;
    }

    /**
     * Gets the contents of this Inventory. No null items will be found, only AIR. <br>
     * Shortcut for {@link #getContents(boolean, boolean)} as (true, true)
     *
     * @return the contents as an array
     */
    public ItemStack[] getContents() {
        return getContents(true, true);
    }

    /**
     * Gets the contents of this Inventory. No null items will be found, only AIR.
     *
     * @param core  set as true to include "core" slots
     * @param armor set as true to include "armor" slots
     * @return the contents as an array
     */
    public ItemStack[] getContents(boolean core, boolean armor) {
        // TODO: Remove from production
        DebugLogger.log(System.currentTimeMillis() + " (get) getting contents " + core + " " + armor + " " + owner + " " + type + " " + gamemode);
        ItemStack[] array = new ItemStack[SIZE];
        if (core && armor) {
            for (Integer slot : items.keySet()) {
                array[slot] = items.get(slot);
            }
        } else if (core && !armor) {
            int size = type == InventoryType.ENDER ? 27 : SIZE - 4;
            array = new ItemStack[size];
            for (Integer slot : items.keySet()) {
                if (slot >= size) {
                    continue;
                }
                array[slot] = items.get(slot);
            }
        } else if (!core && armor) {
            array = new ItemStack[4];
            for (Integer slot : items.keySet()) {
                if (slot < SIZE - 4) {
                    continue;
                }
                array[slot - (SIZE - 4)] = items.get(slot);
            }
        }
        return array;
    }

    /**
     * Sets the contents of this inventory. This can be any size. (Extra items are ignored)
     *
     * @param items the items
     */
    public void setContents(ItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            set(i, items[i]);
        }
    }

    /**
     * Sets a slot in the inventory
     *
     * @param slot the slot (any number, outside of 36+4 is ignored)
     * @param item the item
     */
    public void set(int slot, ItemStack item) {
        items.put(slot, item);
    }

    /**
     * Clones an inventory into this inventory
     *
     * @param inventory the inventory to clone
     */
    public void clone(Inventory inventory) {
        items.clear();
        // TODO: Remove from production
        DebugLogger.log(System.currentTimeMillis() + " (clone2) cloning contents " + owner + " " + type + " " + gamemode);
        if (inventory instanceof PlayerInventory) {
            PlayerInventory playerInv = (PlayerInventory) inventory;
            ItemStack[] armor = playerInv.getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                set(36 + i, armor[i]);
            }
        }
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            set(i, contents[i]);
        }
    }

    /**
     * Clones an inventory into this inventory
     *
     * @param inventory the inventory to clone
     */
    public void clone(ASInventory inventory) {
        items.clear();
        // TODO: Remove from production
        DebugLogger.log(System.currentTimeMillis() + " (clone) cloning contents " + owner + " " + type + " " + gamemode);
        if (inventory instanceof PlayerInventory) {
            PlayerInventory playerInv = (PlayerInventory) inventory;
            ItemStack[] armor = playerInv.getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                set(36 + i, armor[i]);
            }
        }
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            set(i, contents[i]);
        }
    }

    /**
     * Fills the entire inventory with the same item
     *
     * @param item the item
     */
    public void fill(ItemStack item) {
        for (int i = 0; i < SIZE; i++) {
            set(i, item.clone());
        }
    }

    /**
     * Sets the inventory to a specified inventory. This can be a player inventory or another inventory. 36 slots minimum.
     *
     * @param inventory the inventory to set to
     */
    public void setTo(Inventory inventory) {
        inventory.clear();
        if (inventory instanceof PlayerInventory) {
            ((PlayerInventory) inventory).setArmorContents(null);
        }
        ItemStack[] armor = getContents(false, true);
        ItemStack[] contents = getContents(true, false);
        // TODO: Remove from production
        DebugLogger.log(System.currentTimeMillis() + " (set) setting contents " + owner + " " + type + " " + gamemode);
        inventory.setContents(contents);
        if (inventory instanceof PlayerInventory) {
            ((PlayerInventory) inventory).setArmorContents(armor);
        }
    }

    /**
     * Determines if this inventory is empty or not.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        for (int slot : items.keySet()) {
            ItemStack item = items.get(slot);
            if (!(item == null || item.getType() == Material.AIR)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Saves the inventory
     */
    /* TODO: Eventual plans:
     * - Save to 'regions' (Multiple players per file). 1 person ~= 150kb. ~7 people per file
	 */
    public void save() {
        checkDataFolder();
        if (type == InventoryType.PLAYER || type == InventoryType.ENDER) {
            if (!GameModeIdentity.hasChangedGameMode(owner)) {
                // TODO: Remove from production
                DebugLogger.log(System.currentTimeMillis() + " (save) NO GM CHANGE " + owner + " " + type + " " + gamemode);
                return; // Don't save if they haven't changed Game Mode yet
            }
        }
        File file = new File(DATA_FOLDER, type.getRelativeFolderName() + File.separator + owner + ".json");
        JsonConfiguration yaml = new JsonConfiguration();
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            // TODO: Remove from production
            DebugLogger.log(System.currentTimeMillis() + " (save) loading file " + owner + " " + type + " " + gamemode);
            yaml.load(file);
            // TODO: Remove from production
            DebugLogger.log(System.currentTimeMillis() + " (save) setting file " + owner + " " + type + " " + gamemode);
            yaml.set(owner + "." + world + "." + gamemode.name(), getContents());
            yaml.set("version", VERSION);
            // TODO: Remove from production
            DebugLogger.log(System.currentTimeMillis() + " (save) saving file " + owner + " " + type + " " + gamemode);
            yaml.save(file);
            // TODO: Remove from production
            DebugLogger.log(System.currentTimeMillis() + " (save )done " + owner + " " + type + " " + gamemode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads an inventory. This will always return an inventory, however it may be empty.
     *
     * @param player   the player to load (player name)
     * @param gamemode the game mode to load
     * @param type     the inventory type to load
     * @param world    the world name to load
     * @return the loaded inventory. Will never be null.
     */
    public static ASInventory load(String player, GameMode gamemode, InventoryType type, String world) {
        // TODO: Remove from production
        DebugLogger.log(System.currentTimeMillis() + " (load) checking data folder " + player + " " + type + " " + gamemode);
        checkDataFolder();
        // TODO: Remove from production
        DebugLogger.log(System.currentTimeMillis() + " (load) blank creation " + player + " " + type + " " + gamemode);
        File file = new File(DATA_FOLDER, type.getRelativeFolderName() + File.separator + player + ".json");
        ASInventory inventory = new ASInventory(gamemode, player, world, type);
        // TODO: Remove from production
        DebugLogger.log(System.currentTimeMillis() + " (load) loading json " + player + " " + type + " " + gamemode);
        JsonConfiguration json = new JsonConfiguration();
        try {
            if (!file.exists()) {
                // TODO: Remove from production
                DebugLogger.log(System.currentTimeMillis() + " (load) NO FILE EXIST " + player + " " + type + " " + gamemode);
                return inventory; // Empty inventory
            }
            json.load(file);
            String version = json.getString("version");
            if (version == null) {
                // TODO: Remove from production
                DebugLogger.log(System.currentTimeMillis() + " (load) no version - null " + player + " " + type + " " + gamemode);
                return inventory; // Empty inventory
            } else if (version.equalsIgnoreCase("2")) {
                // TODO: Remove from production
                DebugLogger.log(System.currentTimeMillis() + " (load) version2 " + player + " " + type + " " + gamemode);
                Object something = json.get(player + "." + world + "." + gamemode.name());
                if (something instanceof List) {
                    List<?> objects = (List<?>) something;
                    for (int i = 0; i < objects.size(); i++) {
                        Object entry = objects.get(i);
                        if (entry instanceof ItemStack) {
                            // TODO: Remove from production
                            DebugLogger.log(System.currentTimeMillis() + " (load) set " + i + " = " + entry + " || " + player + " " + type + " " + gamemode);
                            inventory.set(i, (ItemStack) entry);
                        }
                    }
                }
            } else {

                // TODO: Remove from production
                DebugLogger.log(System.currentTimeMillis() + " (load) unknown version " + version + " " + player + " " + type + " " + gamemode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return inventory;
    }

    /**
     * Gets a list of all inventories for a player of a specified type
     *
     * @param playerName the player name
     * @param type       the type
     * @return a list of inventories, never null
     */
    public static List<ASInventory> getAll(String playerName, InventoryType type) {
        List<ASInventory> invs = new ArrayList<ASInventory>();
        ASInventory i = null;
        for (World world : AntiShare.p.getServer().getWorlds()) {
            for (GameMode gamemode : GameMode.values()) {
                // TODO: Remove from production
                DebugLogger.log(System.currentTimeMillis() + " (load all) loading " + playerName + " " + type + " " + gamemode);
                i = load(playerName, gamemode, type, world.getName());
                invs.add(i); // Never null
            }
        }
        return invs;
    }

    /**
     * Creates an empty inventory
     *
     * @param playerName the player name
     * @param worldName  the world name
     * @param gamemode   the gamemode
     * @param type       the inventory type
     * @return an empty inventory
     */
    public static ASInventory createEmptyInventory(String playerName, String worldName, GameMode gamemode, InventoryType type) {
        return new ASInventory(gamemode, playerName, worldName, type);
    }

    /**
     * Gets the data folder for AntiShare inventories
     *
     * @return the data folder
     */
    public static File getDataFolder() {
        checkDataFolder();
        return DATA_FOLDER;
    }

    private static void checkDataFolder() {
        File archiveFolder = new File(AntiShare.p.getDataFolder(), "archive" + File.separator + "inventories");
        if (DATA_FOLDER == null) {
            DATA_FOLDER = new File(AntiShare.p.getDataFolder(), "data" + File.separator + "inventories");
        }
        if (!DATA_FOLDER.exists()) {
            DATA_FOLDER.mkdirs();
        }
        if (!archiveFolder.exists()) {
            archiveFolder.mkdirs();
        }
        for (InventoryType type : InventoryType.values()) {
            File f = new File(DATA_FOLDER, type.getRelativeFolderName());
            if (!f.exists()) {
                f.mkdirs();
            }
            f = new File(archiveFolder, type.getRelativeFolderName());
            if (!f.exists()) {
                f.mkdirs();
            }
        }
    }

    @Override
    public String toString() {
        return "ASInventory [gamemode=" + gamemode + ", owner=" + owner + ", world=" + world + ", type=" + type + ", items=" + items + "]";
    }

    @Override
    public ASInventory clone() {
        ASInventory inventory = new ASInventory(gamemode, owner, world, type);
        inventory.setContents(getContents());
        return inventory;
    }

}
