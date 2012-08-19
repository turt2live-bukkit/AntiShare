/*******************************************************************************
 * Copyright (c) 2012 turt2live (Travis Ralston).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 * turt2live (Travis Ralston) - initial API and implementation
 ******************************************************************************/
package com.turt2live.antishare;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import com.turt2live.antishare.AntiShare.LogType;
import com.turt2live.antishare.api.DebugFile;
import com.turt2live.antishare.convert.Convert320bInternal;
import com.turt2live.antishare.convert.Convert320bInternal.ConvertType;
import com.turt2live.antishare.permissions.PermissionNodes;
import com.turt2live.antishare.regions.ASRegion;
import com.turt2live.antishare.regions.RegionKey;

/**
 * Command Handler
 * 
 * @author turt2live
 */
public class CommandHandler implements CommandExecutor {

	@SuppressWarnings ("deprecation")
	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args){
		AntiShare plugin = AntiShare.getInstance();
		long timerId = plugin.getTimer().start(getClass(), "CMD: " + command.getName() + ", args: " + args.toString());
		if(command.getName().equalsIgnoreCase("AntiShare")){
			if(args.length > 0){
				if(args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")){
					if(plugin.getPermissions().has(sender, PermissionNodes.RELOAD)){
						ASUtils.sendToPlayer(sender, "Reloading...", true);
						plugin.reload();
						ASUtils.sendToPlayer(sender, ChatColor.GREEN + "AntiShare Reloaded.", true);
					}else{
						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!", true);
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("mirror")){
					// Sanity Check
					if(!(sender instanceof Player)){
						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You are not a player, and therefore cannot view inventories. Sorry!", true);
					}else{
						if(plugin.getPermissions().has(sender, PermissionNodes.MIRROR)){
							if(args.length < 2){
								ASUtils.sendToPlayer(sender, ChatColor.RED + "No player name provided! Try /as mirror <player>", true);
							}else{
								// Setup
								String playername = args[1];
								Player player = Bukkit.getPlayer(playername);

								// Sanity Check
								if(player == null){
									ASUtils.sendToPlayer(sender, ChatColor.RED + "Player '" + playername + "' could not be found, sorry!", true);
								}else{
									ASUtils.sendToPlayer(sender, ChatColor.GREEN + "Welcome to " + player.getName() + "'s inventory.", true);
									ASUtils.sendToPlayer(sender, "You are able to edit their inventory as you please.", true);
									((Player) sender).openInventory(player.getInventory()); // Creates the "live editing" window
								}
							}
						}else{
							ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!", true);
						}
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("debug")){
					// Sanity Check
					if(!(sender instanceof ConsoleCommandSender)){
						ASUtils.sendToPlayer(sender, ChatColor.RED + "Sorry! This command needs to be run from the console.", true);
					}else{
						// Generate debug dump file
						try{
							final File file = new File(plugin.getDataFolder(), "debug.txt");
							BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
							out.write("[v" + plugin.getDescription().getVersion() + "] AntiShare Debug Information:\r\n");
							out.write("CraftBukkit: " + Bukkit.getBukkitVersion() + "\r\n");
							out.write("Version: " + Bukkit.getVersion() + "\r\n");
							out.write("Online Mode: " + Bukkit.getOnlineMode() + "\r\n");
							out.write("----------------------------\r\n");
							out.write("Plugins: \r\n");
							for(Plugin p : Bukkit.getPluginManager().getPlugins()){
								out.write("[" + (p.isEnabled() ? "E" : "D") + "] " + p.getDescription().getFullName() + "\r\n");
							}
							out.write("----------------------------\r\n");
							out.write("Configuration Keys: \r\n");
							for(String key : plugin.getConfig().getKeys(true)){
								String value = plugin.getConfig().getString(key, "");
								if(value.startsWith("MemorySection")){
									value = "";
								}
								if(key.startsWith("settings.sql")){
									value = "CENSORED FOR SECURITY";
								}
								out.write(key + ": " + value + "\r\n");
							}
							out.write("----------------------------\r\n");
							out.write("Message Keys: \r\n");
							plugin.getMessages().print(out);
							out.write("----------------------------\r\n");
							out.write("Notification Keys: \r\n");
							plugin.getAlerts().print(out);
							out.write("----------------------------\r\n");
							out.write("World Configurations: \r\n");
							plugin.getListener().print(out);
							out.write("----------------------------\r\n");
							out.write("Region Configurations: \r\n");
							for(ASRegion region : plugin.getRegionManager().getAllRegions()){
								region.getConfig().print(out);
							}
							out.write("----------------------------\r\n");
							out.write("Rewards/Fines: \r\n");
							plugin.getMoneyManager().print(out);
							out.close();
							out.write("----------------------------\r\n");
							out.write("TIMERS: \r\n");
							plugin.getTimer().debug(out);
							out.close();
							plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
								@Override
								public void run(){
									DebugFile debug = new DebugFile(file);
									debug.save(sender);
									debug.alert();
								}
							});
							ASUtils.sendToPlayer(sender, "Debug file is saved at: " + file.getAbsolutePath(), true);
						}catch(Exception e){
							AntiShare.getInstance().getMessenger().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE, LogType.ERROR);
							AntiShare.getInstance().getMessenger().log("Please see " + ErrorLog.print(e) + " for the full error.", Level.SEVERE, LogType.ERROR);
						}
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("region")){
					if(plugin.getPermissions().has(sender, PermissionNodes.REGION_CREATE)){
						// Sanity Check
						if(sender instanceof Player){
							if(args.length < 3){
								ASUtils.sendToPlayer(sender, ChatColor.RED + "Not enough arguments! " + ChatColor.WHITE + "Try /as region <gamemode> <name>", true);
							}else{
								plugin.getRegionFactory().addRegion((Player) sender, args[1], args[2]);
							}
						}else{
							ASUtils.sendToPlayer(sender, ChatColor.RED + "You must be a player to create regions.", true);
						}
					}else{
						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!", true);
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("rmregion") || args[0].equalsIgnoreCase("removeregion")){
					if(plugin.getPermissions().has(sender, PermissionNodes.REGION_DELETE)){
						// Sanity check
						if(sender instanceof Player){
							// Remove region
							if(args.length == 1){
								Location location = ((Player) sender).getLocation();
								plugin.getRegionFactory().removeRegionByLocation(sender, location);
							}else{
								plugin.getRegionFactory().removeRegionByName(sender, args[1]);
							}
						}else{
							// Remove region
							if(args.length > 1){
								plugin.getRegionFactory().removeRegionByName(sender, args[1]);
							}else{
								ASUtils.sendToPlayer(sender, ChatColor.RED + "You must supply a region name when removing regions from the console. " + ChatColor.WHITE + "Try: /as rmregion <name>", true);
							}
						}
					}else{
						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!", true);
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("editregion")){
					if(plugin.getPermissions().has(sender, PermissionNodes.REGION_EDIT)){
						// Check validity of key
						boolean valid = false;
						if(args.length >= 3){
							if(RegionKey.isKey(args[2])){
								if(!RegionKey.requiresValue(RegionKey.getKey(args[2]))){
									valid = true; // we have at least 3 values in args[] and the key does not need a value
								}
							}
						}
						if(args.length >= 4){
							valid = true;
						}
						if(!valid){
							// Show help
							if(args.length >= 2){
								if(args[1].equalsIgnoreCase("help")){
									ASUtils.sendToPlayer(sender, ChatColor.GOLD + "/as editregion <name> <key> <value>", false);
									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "name " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "<any name>", false);
									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "ShowEnterMessage " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "true/false", false);
									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "ShowExitMessage " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "true/false", false);
									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "EnterMessage " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "<enter message>", false);
									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "ExitMessage " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "<exit message>", false);
									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "inventory " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "'none'/'set'", false);
									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "gamemode " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "survival/creative", false);
									ASUtils.sendToPlayer(sender, ChatColor.AQUA + "Key: " + ChatColor.WHITE + "area " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + "No Value", false);
									ASUtils.sendToPlayer(sender, ChatColor.YELLOW + "'Show____Message'" + ChatColor.WHITE + " - True to show the message", false);
									ASUtils.sendToPlayer(sender, ChatColor.YELLOW + "'____Message'" + ChatColor.WHITE + " - Use {name} to input the region name.", false);
									ASUtils.sendToPlayer(sender, ChatColor.YELLOW + "'inventory'" + ChatColor.WHITE + " - Sets the region's inventory. 'none' to not have a default inventory, 'set' to mirror yours", false);
									ASUtils.sendToPlayer(sender, ChatColor.YELLOW + "'area'" + ChatColor.WHITE + " - Sets the area based on your WorldEdit selection", false);
								}else{
									ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "Incorrect syntax, try: /as editregion <name> <key> <value>", true);
									ASUtils.sendToPlayer(sender, ChatColor.RED + "For keys and values type /as editregion help", true);
								}
							}else{
								ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "Incorrect syntax, try: /as editregion <name> <key> <value>", true);
								ASUtils.sendToPlayer(sender, ChatColor.RED + "For keys and values type /as editregion help", true);
							}
						}else{
							// Setup
							String name = args[1];
							String key = args[2];
							String value = args.length > 3 ? args[3] : "";

							// Merge message
							if(args.length > 4){
								for(int i = 4; i < args.length; i++){ // Starts at args[4]
									value = value + args[i] + " ";
								}
								value = value.substring(0, value.length() - 1);
							}

							// Check region
							if(plugin.getRegionManager().getRegion(name) == null){
								ASUtils.sendToPlayer(sender, ChatColor.RED + "That region does not exist!", true);
							}else{
								// Update region if needed
								if(RegionKey.isKey(key)){
									plugin.getRegionFactory().editRegion(plugin.getRegionManager().getRegion(name), RegionKey.getKey(key), value, sender);
								}else{
									ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "That is not a valid region key", true);
									ASUtils.sendToPlayer(sender, ChatColor.RED + "For keys and values type /as editregion help", true);
								}
							}
						}
					}else{
						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!", true);
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("listregions")){
					if(plugin.getPermissions().has(sender, PermissionNodes.REGION_LIST)){
						// Sanity check on page number
						int page = 1;
						if(args.length >= 2){
							try{
								page = Integer.parseInt(args[1]);
							}catch(Exception e){
								ASUtils.sendToPlayer(sender, ChatColor.RED + "'" + args[1] + "' is not a number!", true);
								plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
								return true;
							}
						}

						// Setup
						page = Math.abs(page);
						int resultsPerPage = 6; // Put as a variable for ease of changing
						List<ASRegion> regions = plugin.getRegionManager().getAllRegions();

						// Math
						double maxPages = Math.ceil(regions.size() / resultsPerPage);
						if(maxPages < 1){
							maxPages = 1;
						}
						if(maxPages < page){
							ASUtils.sendToPlayer(sender, ChatColor.RED + "Page " + page + " does not exist! The last page is " + maxPages, true);
							plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
							return true;
						}

						// Generate pages
						String pagenation = ChatColor.DARK_GREEN + "=======[ " + ChatColor.GREEN + "AntiShare Regions " + ChatColor.DARK_GREEN + "|" + ChatColor.GREEN + " Page " + page + "/" + maxPages + ChatColor.DARK_GREEN + " ]=======";
						ASUtils.sendToPlayer(sender, pagenation, false);
						for(int i = ((page - 1) * resultsPerPage); i < (resultsPerPage < regions.size() ? (resultsPerPage * page) : regions.size()); i++){
							ASUtils.sendToPlayer(sender, ChatColor.DARK_AQUA + "#" + (i + 1) + " " + ChatColor.GOLD + regions.get(i).getName()
									+ ChatColor.YELLOW + " Creator: " + ChatColor.AQUA + regions.get(i).getWhoSet()
									+ ChatColor.YELLOW + " World: " + ChatColor.AQUA + regions.get(i).getWorld().getName(), false);
						}
						ASUtils.sendToPlayer(sender, pagenation, false);
					}else{
						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!", true);
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("tool")){
					if(plugin.getPermissions().has(sender, PermissionNodes.TOOL_GET)){
						// Sanity check
						if(!(sender instanceof Player)){
							ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You must be a player to use the tool!", true);
						}else{
							// Setup
							Player player = (Player) sender;
							PlayerInventory inventory = player.getInventory();

							// Check inventory
							if(inventory.firstEmpty() != -1 && inventory.firstEmpty() <= inventory.getSize()){
								if(inventory.contains(AntiShare.ANTISHARE_TOOL)){
									ASUtils.sendToPlayer(sender, ChatColor.RED + "You already have the tool! (" + AntiShare.ANTISHARE_TOOL.name().toLowerCase().replace("_", " ") + ")", true);
								}else{
									// Add the tool
									inventory.addItem(new ItemStack(AntiShare.ANTISHARE_TOOL));
									player.updateInventory();
									ASUtils.sendToPlayer(sender, ChatColor.GREEN + "You now have the tool! (" + AntiShare.ANTISHARE_TOOL.name().toLowerCase().replace("_", " ") + ")", true);
								}
							}else{
								ASUtils.sendToPlayer(sender, ChatColor.RED + "You must have at least 1 free spot in your inventory!", true);
							}
						}
					}else{
						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!", true);
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("convert")){
					if(plugin.getPermissions().has(sender, PermissionNodes.CONVERT)){
						if(args.length < 3){
							ASUtils.sendToPlayer(sender, ChatColor.RED + "Incorrect syntax, Try /as convert <from> <to>", true);
						}else{
							ConvertType from = ConvertType.getType(args[1]);
							ConvertType to = ConvertType.getType(args[2]);
							if(from == ConvertType.INVALID || to == ConvertType.INVALID || to == from){
								ASUtils.sendToPlayer(sender, ChatColor.RED + "Incorrect syntax, Try /as convert <from> <to>", true);
							}else{
								// Starts SQL if is not already
								if(plugin.startSQL()){
									// Setup sql
									if(to == ConvertType.SQL){
										plugin.getConfig().set("enabled-features.sql", true);
									}else{
										plugin.getConfig().set("enabled-features.sql", false);
									}

									// Connected
									if(Convert320bInternal.convert(from, to)){
										ASUtils.sendToPlayer(sender, ChatColor.GREEN + "Converted!", true);
									}else{
										ASUtils.sendToPlayer(sender, ChatColor.RED + "Something went wwrong while converting. Please check the server log for errors", true);
									}
								}else{
									ASUtils.sendToPlayer(sender, ChatColor.RED + "Something went wrong while connecting to the SQL server. Please check your settings.", true);
								}
							}
						}
					}else{
						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!", true);
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("money")){
					if(args.length < 2){
						ASUtils.sendToPlayer(sender, ChatColor.RED + "Syntax Error, try /as money on/off/status", true);
					}else{
						if(args[1].equalsIgnoreCase("status") || args[1].equalsIgnoreCase("state")){
							String state = !plugin.getMoneyManager().isSilent(sender.getName()) ? ChatColor.GREEN + "getting" : ChatColor.RED + "not getting";
							state = state + ChatColor.WHITE;
							ASUtils.sendToPlayer(sender, "You are " + state + " fine/reward messages", true);
							plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
							return true;
						}
						if(ASUtils.getBoolean(args[1]) == null){
							ASUtils.sendToPlayer(sender, ChatColor.RED + "Syntax Error, try /as money on/off/status", true);
							plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
							return true;
						}
						boolean silent = !ASUtils.getBoolean(args[1]);
						if(silent){
							plugin.getMoneyManager().addToSilentList(sender.getName());
						}else{
							plugin.getMoneyManager().removeFromSilentList(sender.getName());
						}
						String message = "You are now " + (silent ? ChatColor.RED + "not getting" : ChatColor.GREEN + "getting") + ChatColor.WHITE + " fine/reward messages";
						ASUtils.sendToPlayer(sender, message, true);
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("simplenotice") || args[0].equalsIgnoreCase("sn")){
					if(sender instanceof Player){
						Player player = (Player) sender;
						if(player.getListeningPluginChannels().contains("SimpleNotice")){
							if(plugin.isSimpleNoticeEnabled(player.getName())){
								plugin.disableSimpleNotice(player.getName());
								ASUtils.sendToPlayer(player, ChatColor.RED + "SimpleNotice is now NOT being used to send you messages", false);
							}else{
								plugin.enableSimpleNotice(player.getName());
								ASUtils.sendToPlayer(player, ChatColor.GREEN + "SimpleNotice is now being used to send you messages", false);
							}
						}else{
							ASUtils.sendToPlayer(sender, ChatColor.RED + "You do not have SimpleNotice", false);
						}
					}else{
						ASUtils.sendToPlayer(sender, ChatColor.RED + "You are not a player and do not have SimpleNotice", true);
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else if(args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("gamemode") || args[0].equalsIgnoreCase("gm")){
					if(plugin.getPermissions().has(sender, PermissionNodes.CHECK)){
						GameMode gm = null;
						if(args.length > 1){
							gm = ASUtils.getGameMode(args[1]);
							if(gm == null){
								ASUtils.sendToPlayer(sender, ChatColor.RED + "Unknown Game Mode!", true);
								plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
								return true;
							}
						}
						if(gm == null){
							for(GameMode gamemode : GameMode.values()){
								if(ASUtils.findGameModePlayers(gamemode).size() > 0){
									ASUtils.sendToPlayer(sender, ChatColor.GOLD + gamemode.name() + ": " + ChatColor.YELLOW + ASUtils.commas(ASUtils.findGameModePlayers(gamemode)), false);
								}else{
									ASUtils.sendToPlayer(sender, ChatColor.GOLD + gamemode.name() + ": " + ChatColor.YELLOW + "no one", false);
								}
							}
						}else{
							ASUtils.sendToPlayer(sender, ChatColor.GOLD + gm.name() + ": " + ChatColor.YELLOW + ASUtils.commas(ASUtils.findGameModePlayers(gm)), false);
						}
					}else{
						ASUtils.sendToPlayer(sender, ChatColor.DARK_RED + "You do not have permission!", true);
					}
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return true;
				}else{
					// This is for all extra commands, like /as help.
					// This is also for all "non-commands", like /as sakjdha
					plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
					return false; //Shows usage in plugin.yml
				}
			}
		}
		plugin.getTimer().stop(getClass(), "CMD: " + command.getName() + ", args: " + args.toString(), timerId);
		return false; //Shows usage in plugin.yml
	}

}
