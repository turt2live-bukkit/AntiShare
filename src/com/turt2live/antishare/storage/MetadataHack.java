package com.turt2live.antishare.storage;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.feildmaster.lib.configuration.EnhancedConfiguration;
import com.turt2live.antishare.AntiShare;

public class MetadataHack {

	/* TODO: Hope that deltahat can fix Metadata before the RB
	 * 
	 * Note to self:
	 * 	In the IRC deltahat was talking about metadata bugs, at
	 *  one point the key check was brought up, he said he will
	 *  try to fix it in time for the RB. If it is NOT in the RB,
	 *  AntiShare should be released with this wonderful POS, if 
	 *  it is able to get into the RB, fix this thing ASAP to get
	 *  AS out with the RB, as per schedule.
	 */

	private AntiShare plugin;
	private HashMap<Object, Meta> metadata = new HashMap<Object, Meta>();

	public MetadataHack(AntiShare plugin){
		this.plugin = plugin;
		File file = new File(plugin.getDataFolder(), "metadata.yml");
		EnhancedConfiguration metadata = new EnhancedConfiguration(file, plugin);
		if(!file.exists()){
			try{
				file.createNewFile();
			}catch(Exception e){} // Not REALLY needed
		}
		metadata.load();
		Set<String> keys = metadata.getKeys(false);
		for(String key : keys){ // Blocks only! (for now?)
			// Format: [x];[y];[z];[world]
			String parts[] = key.split(";");
			Location location = new Location(Bukkit.getWorld(parts[3]), Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
			Block block = location.getBlock();
			Meta meta = new Meta();
			if(metadata.get(key + ".ASCreative") != null){
				meta.add("ASCreative", metadata.getBoolean(key + ".ASCreative"));
			}
			if(metadata.get(key + ".ASSurvival") != null){
				meta.add("ASSurvival", metadata.getBoolean(key + ".ASSurvival"));
			}
			if(metadata.get(key + ".invmirror") != null){
				meta.add("invmirror", metadata.getString(key + ".invmirror"));
			}
			this.metadata.put(block, meta);
		}
	}

	public void save(){
		File file = new File(plugin.getDataFolder(), "metadata.yml");
		if(file.exists()){
			file.delete();
			try{
				file.createNewFile();
			}catch(Exception e){} // Why not make it more hackish?
		}
		EnhancedConfiguration metadata = new EnhancedConfiguration(file, plugin);
		metadata.save();
		metadata.load();
		Set<Object> keys = this.metadata.keySet();
		for(Object obj : keys){
			if(obj instanceof Block){ // Should be
				Block block = (Block) obj;
				String path = block.getX() + ";" + block.getY() + ";" + block.getZ() + ";" + block.getWorld().getName();
				Meta meta = this.metadata.get(obj);
				if(meta.get("ASCreative") != null){
					metadata.set(path + ".ASCreative", meta.get("ASCreative"));
				}
				if(meta.get("ASSurvival") != null){
					metadata.set(path + ".ASSurvival", meta.get("ASSurvival"));
				}
				if(meta.get("invmirror") != null){
					metadata.set(path + ".invmirror", meta.get("invmirror"));
				}
				metadata.save();
			}
		}
		this.metadata.clear();
	}

	public void set(Block block, String key, Object value){
		if(metadata.containsKey(block)){
			Meta meta = metadata.get(block);
			meta.add(key, value);
		}else{
			Meta meta = new Meta();
			meta.add(key, value);
			metadata.put(block, meta);
		}
	}

	public void set(Block block, Meta meta){
		metadata.put(block, meta);
	}

	public Meta get(Block block){
		if(metadata.containsKey(block)){
			return metadata.get(block);
		}
		return null;
	}

	public Object get(Block block, String key){
		if(metadata.containsKey(block)){
			return metadata.get(block).get(key);
		}
		return null;
	}

	public void remove(Block block, String key){
		if(metadata.containsKey(block)){
			metadata.get(block).unset(key);
		}
	}
}
