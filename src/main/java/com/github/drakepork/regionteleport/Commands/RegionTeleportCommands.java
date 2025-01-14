package com.github.drakepork.regionteleport.Commands;


import com.Zrips.CMI.CMI;
import com.github.drakepork.regionteleport.Addons.CMIAddon;
import com.github.drakepork.regionteleport.Addons.EssentialsAddon;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.github.drakepork.regionteleport.RegionTeleport;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RegionTeleportCommands implements CommandExecutor {
	private final RegionTeleport plugin;

	public RegionTeleportCommands(RegionTeleport plugin) {
		this.plugin = plugin;
	}

	public void tellConsole(String message){
		Bukkit.getConsoleSender().sendMessage(message);
	}

	public String ColourMessage(String message){
		message = plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
		return message;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		String noPerm = ColourMessage(langConf.getString("global.no-perm"));
		String prefix = langConf.getString("global.plugin-prefix");

		File spawnloc = new File(plugin.getDataFolder() + File.separator + "spawnlocations.yml");
		YamlConfiguration spawnConf = YamlConfiguration.loadConfiguration(spawnloc);

		if(sender instanceof Player) {
			List<String> cmdList = (List<String>) langConf.getList("global.help");
			String commandHelp = "";
			for(int i = 0; i < cmdList.size(); i++) {
				commandHelp += ColourMessage(cmdList.get(i));

				if(i + 1 < cmdList.size()) {
					commandHelp += "\n";
				}
			}
			Player player = (Player) sender;
			if(args.length < 1) {
				if(player.hasPermission("regionteleport.command.help")) {
					player.sendMessage(commandHelp);
				} else {
					player.sendMessage(noPerm);

				}
				return true;
			}

			switch(args[0].toLowerCase()) {
				case "help":
					if(player.hasPermission("regionteleport.command.help")) {
						player.sendMessage(commandHelp);
					} else {
						player.sendMessage(noPerm);
					}
					break;

				case "setspawn":
					if(player.hasPermission("regionteleport.command.setspawn")) {
						if (args.length < 2) {
							player.sendMessage(ColourMessage(prefix + langConf.getString("spawn.specify-loc-name")));
							return true;
						}

						if (args.length > 2) {
							player.sendMessage(ColourMessage(prefix + langConf.getString("spawn.wrong-usage-setspawn")));
							return true;
						}

						if (spawnConf.contains(args[1])) {
							String spawnExist = langConf.getString("spawn.spawn-already-exists").replaceAll("\\[name\\]", args[1]);
							player.sendMessage(ColourMessage(prefix + spawnExist));
							return true;
						}

						spawnConf.set(args[1] + ".world", player.getLocation().getWorld().getName());
						spawnConf.set(args[1] + ".x", player.getLocation().getX());
						spawnConf.set(args[1] + ".y", player.getLocation().getY());
						spawnConf.set(args[1] + ".z", player.getLocation().getZ());
						spawnConf.set(args[1] + ".yaw", player.getLocation().getYaw());
						spawnConf.set(args[1] + ".pitch", player.getLocation().getPitch());
						try {
							spawnConf.save(spawnloc);
							String setspawnSuccess = langConf.getString("spawn.successful-setspawn").replaceAll("\\[name\\]", args[1]);
							player.sendMessage(ColourMessage(prefix + setspawnSuccess));
						} catch (IOException e) {
							e.printStackTrace();
						}
						return true;
					} else {
						player.sendMessage(noPerm);
					}
					break;
				case "delspawn":
					if(player.hasPermission("regionteleport.command.delspawn")) {
						if (args.length < 2) {
							player.sendMessage(ColourMessage(prefix + langConf.getString("spawn.specify-loc-name")));
							return true;
						}

						if (args.length > 2) {
							player.sendMessage(ColourMessage(prefix + langConf.getString("spawn.wrong-usage-delspawn")));
							return true;
						}

						if(spawnConf.contains(args[1])) {
							spawnConf.set(args[1], null);
							try {
								spawnConf.save(spawnloc);
								String delspawnSuccess = langConf.getString("spawn.successful-delspawn").replaceAll("\\[name\\]", args[1]);
								player.sendMessage(ColourMessage(prefix + delspawnSuccess));
							} catch (IOException e) {
								e.printStackTrace();
								String delspawnFail = langConf.getString("spawn.failed-delspawn").replaceAll("\\[name\\]", args[1]);
								player.sendMessage(ColourMessage(prefix + delspawnFail));
							}
						} else {
							String noSpawn = langConf.getString("spawn.no-such-spawn").replaceAll("\\[name\\]", args[1]);
							player.sendMessage(ColourMessage(prefix + noSpawn));
						}

					} else {
						player.sendMessage(noPerm);
					}
					break;
				case "spawnlist":
					if(player.hasPermission("regionteleport.command.spawnlist")) {
						if (args.length > 1) {
							player.sendMessage(ColourMessage(prefix + langConf.getString("spawn.wrong-usage-spawnlist")));
							return true;
						}

						player.sendMessage(ColourMessage(langConf.getString("spawn.list-header")));
						for (String spawnName : spawnConf.getKeys(false)) {
							String spawn = langConf.getString("spawn.list-spawn").replaceAll("\\[name\\]", spawnName);
							player.sendMessage(ColourMessage(spawn));
						}
					} else {
						player.sendMessage(noPerm);
					}
					break;
				case "reload":
					if(player.hasPermission("regionteleport.command.reload")) {
						plugin.onReload();
						player.sendMessage(ColourMessage(langConf.getString("global.reload")));
					}
					break;
				case "teleport":
				case "tp":
					if(player.hasPermission("regionteleport.command.teleport")) {
						if (args.length < 3) {
							player.sendMessage(ColourMessage(prefix + langConf.getString("teleport.wrong-usage")));
							return true;
						}

						if (args.length > 4) {
							player.sendMessage(ColourMessage(prefix + langConf.getString("teleport.wrong-usage")));
							return true;
						}
						String name = args[2];

						if(args[2].contains(":")) {
							RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
							RegionManager regions = container.get(BukkitAdapter.adapt(player).getWorld());
							String[] teleportLoc = args[2].split(":");
							name = teleportLoc[1];
							if (regions.getRegion(args[1]) != null || args[1].equalsIgnoreCase("__global__")) {
								int teleported = 0;
								World pWorld = player.getWorld();
								switch (teleportLoc[0].toLowerCase()) {
									case "cmi":
										if(plugin.cmiAddon == null) {
											player.sendMessage(ColourMessage(langConf.getString("addon.disabled").replaceAll("\\[name\\]", teleportLoc[0])));
											return true;
										}
										break;
									case "essentials":
									case "ess":
										if(plugin.essAddon == null) {
											player.sendMessage(ColourMessage(langConf.getString("addon.disabled").replaceAll("\\[name\\]", teleportLoc[0])));
											return true;
										}
										break;
									default:
										player.sendMessage(ColourMessage(langConf.getString("teleport.no-such-addon").replaceAll("\\[name\\]", teleportLoc[0])));
										return true;
								}
								for (Player pOnline : Bukkit.getServer().getOnlinePlayers()) {
									if (!pOnline.hasPermission("regionteleport.teleport.bypass")) {
										switch (teleportLoc[0].toLowerCase()) {
											case "cmi":
												if(args[1].equalsIgnoreCase("__global__") && pOnline.getWorld() == pWorld) {
													if (plugin.cmiAddon.warpLoc(teleportLoc) != null) {
														Location nLoc = plugin.cmiAddon.warpLoc(teleportLoc);
														pOnline.teleport(nLoc);
														teleported++;
													} else {
														player.sendMessage(ColourMessage(prefix + langConf.getString("spawn.no-such-cmi").replaceAll("\\[name\\]", name)));
														return true;
													}
												} else {
													if(plugin.cmiAddon.warpLoc(teleportLoc) != null) {
														Location loc = pOnline.getLocation();
														BlockVector3 v = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
														World world = pOnline.getWorld();
														RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
														ApplicableRegionSet set = rm.getApplicableRegions(v);
														for (ProtectedRegion r : set) {
															if (r.getId().equalsIgnoreCase(args[1])) {
																Location nLoc = plugin.cmiAddon.warpLoc(teleportLoc);
																pOnline.teleport(nLoc);
																teleported++;
																break;
															}
														}
													} else {
														player.sendMessage(ColourMessage(prefix + langConf.getString("spawn.no-such-cmi").replaceAll("\\[name\\]", name)));
														return true;
													}
												}
												break;
											case "essentials":
											case "ess":
												if(args[1].equalsIgnoreCase("__global__") && pOnline.getWorld() == pWorld) {
													if (plugin.essAddon.warpLoc(teleportLoc) != null) {
														Location nLoc = plugin.essAddon.warpLoc(teleportLoc);
														pOnline.teleport(nLoc);
														teleported++;
													} else {
														player.sendMessage(ColourMessage(prefix + langConf.getString("spawn.no-such-essentials").replaceAll("\\[name\\]", name)));
														return true;
													}
												} else {
													if(plugin.essAddon.warpLoc(teleportLoc) != null) {
														Location loc = pOnline.getLocation();
														BlockVector3 v = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
														World world = pOnline.getWorld();
														RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
														ApplicableRegionSet set = rm.getApplicableRegions(v);
														for (ProtectedRegion r : set) {
															if (r.getId().equalsIgnoreCase(args[1])) {
																Location nLoc = plugin.essAddon.warpLoc(teleportLoc);
																pOnline.teleport(nLoc);
																teleported++;
																break;
															}
														}
													} else {
														player.sendMessage(ColourMessage(prefix + langConf.getString("spawn.no-such-essentials").replaceAll("\\[name\\]", name)));
														return true;
													}
												}
												break;
										}

									}
								}
								if(args.length == 4) {
									if (!args[3].equalsIgnoreCase(("-s"))) {
										player.sendMessage(ColourMessage(prefix + langConf.getString("teleport.wrong-usage")));
									}
								} else {
									String tpSuccess = langConf.getString("teleport.successful-teleport").replaceAll("\\[name\\]", name);
									tpSuccess = tpSuccess.replaceAll("\\[region\\]", args[1]);
									tpSuccess = tpSuccess.replaceAll("\\[amount\\]", String.valueOf(teleported));
									player.sendMessage(ColourMessage(prefix + tpSuccess));
								}
							} else {
								String noRegion = langConf.getString("teleport.no-such-region").replaceAll("\\[name\\]", name);
								player.sendMessage(ColourMessage(prefix + noRegion));
							}
						} else if(spawnConf.contains(args[2])) {
							RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
							RegionManager regions = container.get(BukkitAdapter.adapt(player).getWorld());
							if (regions.getRegion(args[1]) != null || args[1].equalsIgnoreCase("__global__")) {
								int teleported = 0;
								World pWorld = player.getWorld();
								for (Player pOnline : Bukkit.getServer().getOnlinePlayers()) {
									if(!pOnline.hasPermission("regionteleport.teleport.bypass")) {
										World w = Bukkit.getServer().getWorld(spawnConf.getString(args[2] + ".world"));
										float yaw = (float) spawnConf.getDouble(args[2] + ".yaw");
										float pitch = (float) spawnConf.getDouble(args[2] + ".pitch");
										if (args[1].equalsIgnoreCase("__global__") && pOnline.getWorld() == pWorld) {
											Location location = new Location(w, spawnConf.getDouble(args[2] + ".x"), spawnConf.getDouble(args[2] + ".y"), spawnConf.getDouble(args[2] + ".z"));
											location.setYaw(yaw);
											location.setPitch(pitch);
											pOnline.teleport(location);
											teleported++;
										} else {
											Location location = pOnline.getLocation();
											BlockVector3 v = BlockVector3.at(location.getX(), location.getY(), location.getZ());
											World world = pOnline.getWorld();
											RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
											ApplicableRegionSet set = rm.getApplicableRegions(v);
											for (ProtectedRegion r : set) {
												if (r.getId().equalsIgnoreCase(args[1])) {
													Location nLoc = new Location(w, spawnConf.getDouble(args[2] + ".x"), spawnConf.getDouble(args[2] + ".y"), spawnConf.getDouble(args[2] + ".z"));
													nLoc.setYaw(yaw);
													nLoc.setPitch(pitch);
													pOnline.teleport(nLoc);
													teleported++;
												}
											}
										}
									}
								}
								if(args.length == 4) {
									if (!args[3].equalsIgnoreCase(("-s"))) {
										player.sendMessage(ColourMessage(prefix + langConf.getString("teleport.wrong-usage")));
									}
								} else {
									String tpSuccess = langConf.getString("teleport.successful-teleport").replaceAll("\\[name\\]", name);
									tpSuccess = tpSuccess.replaceAll("\\[region\\]", args[1]);
									tpSuccess = tpSuccess.replaceAll("\\[amount\\]", String.valueOf(teleported));
									player.sendMessage(ColourMessage(prefix + tpSuccess));
								}
							} else {
								String noRegion = langConf.getString("teleport.no-such-region").replaceAll("\\[name\\]", args[1]);
								player.sendMessage(ColourMessage(prefix + noRegion));
							}
						} else {
							String noSpawn = langConf.getString("spawn.no-such-spawn").replaceAll("\\[name\\]", name);
							player.sendMessage(ColourMessage(prefix + noSpawn));
						}

					} else {
						player.sendMessage(noPerm);
					}
					break;
				default:
					player.sendMessage(commandHelp);
					return true;
			}
		} else {
			List<String> cmdList = (List<String>) langConf.getList("console.help");
			String commandHelp = "";
			for(int i = 0; i < cmdList.size(); i++) {
				commandHelp += ColourMessage(cmdList.get(i));

				if(i + 1 < cmdList.size()) {
					commandHelp += "\n";
				}
			}

			if(args.length < 1) {
				tellConsole(commandHelp);
				return true;
			}

			switch(args[0].toLowerCase()) {
				case "help":
					tellConsole(commandHelp);
					break;

				case "setspawn":
					if (args.length < 6) {
						tellConsole(ColourMessage(prefix + langConf.getString("console.wrong-usage-setspawn")));
						return true;
					}

					if (args.length > 6) {
						tellConsole(ColourMessage(prefix + langConf.getString("console.wrong-usage-setspawn")));
						return true;
					}

					if (spawnConf.contains(args[1])) {
						String spawnExist = langConf.getString("spawn.spawn-already-exists").replaceAll("\\[name\\]", args[1]);
						tellConsole(ColourMessage(prefix + spawnExist));
						return true;
					}

					World spawnWorld = Bukkit.getWorld(args[5]);
					if(spawnWorld != null) {
						Location loc = new Location(spawnWorld, Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
						if(loc != null) {
							spawnConf.set(args[1] + ".world", spawnWorld.getName());
							spawnConf.set(args[1] + ".x", loc.getX());
							spawnConf.set(args[1] + ".y", loc.getY());
							spawnConf.set(args[1] + ".z", loc.getZ());
							spawnConf.set(args[1] + ".yaw", loc.getYaw());
							spawnConf.set(args[1] + ".pitch", loc.getPitch());
							try {
								spawnConf.save(spawnloc);
								String setspawnSuccess = langConf.getString("spawn.successful-setspawn").replaceAll("\\[name\\]", args[1]);
								tellConsole(ColourMessage(prefix + setspawnSuccess));
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							tellConsole(ColourMessage(prefix + langConf.getString("console.wrong-usage-setspawn")));
						}
					} else {
						String noSuchWorld = langConf.getString("console.no-such-world").replaceAll("\\[name\\]", args[5]);
						tellConsole(ColourMessage(prefix + noSuchWorld));
					}
					break;
				case "delspawn":
					if (args.length < 2) {
						tellConsole(ColourMessage(prefix + langConf.getString("spawn.specify-loc-name")));
						return true;
					}

					if (args.length > 2) {
						tellConsole(ColourMessage(prefix + langConf.getString("spawn.wrong-usage-delspawn")));
						return true;
					}

					if(spawnConf.contains(args[1])) {
						spawnConf.set(args[1], null);
						try {
							spawnConf.save(spawnloc);
							String delspawnSuccess = langConf.getString("spawn.successful-delspawn").replaceAll("\\[name\\]", args[1]);
							tellConsole(ColourMessage(prefix + delspawnSuccess));
						} catch (IOException e) {
							e.printStackTrace();
							String delspawnFail = langConf.getString("spawn.failed-delspawn").replaceAll("\\[name\\]", args[1]);
							tellConsole(ColourMessage(prefix + delspawnFail));
						}
					} else {
						String noSpawn = langConf.getString("spawn.no-such-spawn").replaceAll("\\[name\\]", args[1]);
						tellConsole(ColourMessage(prefix + noSpawn));
					}
					break;
				case "spawnlist":
					if (args.length > 1) {
						tellConsole(ColourMessage(prefix + langConf.getString("spawn.wrong-usage-spawnlist")));
						return true;
					}

					tellConsole(ColourMessage(langConf.getString("spawn.list-header")));
					for (String spawnName : spawnConf.getKeys(false)) {
						String spawn = langConf.getString("spawn.list-spawn").replaceAll("\\[name\\]", spawnName);
						tellConsole(ColourMessage(spawn));
					}
					break;
				case "teleport":
				case "tp":
						if (args.length < 4) {
							tellConsole(ColourMessage(prefix + langConf.getString("console.wrong-usage-teleport")));
							return true;
						}

						if (args.length > 5) {
							tellConsole(ColourMessage(prefix + langConf.getString("console.wrong-usage-teleport")));
							return true;
						}
					String name = args[2];
					if(args[2].contains(":")) {
						World cWorld = Bukkit.getWorld(args[3]);
						RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
						RegionManager regions = container.get(BukkitAdapter.adapt(cWorld));
						String[] teleportLoc = args[2].split(":");
						name = teleportLoc[1];
						if (regions.getRegion(args[1]) != null || args[1].equalsIgnoreCase("__global__")) {
							int teleported = 0;
							switch (teleportLoc[0].toLowerCase()) {
								case "cmi":
									if(plugin.cmiAddon == null) {
										tellConsole(ColourMessage(langConf.getString("addon.disabled").replaceAll("\\[name\\]", teleportLoc[0])));
										return true;
									}
									break;
								case "essentials":
								case "ess":
									if(plugin.essAddon == null) {
										tellConsole(ColourMessage(langConf.getString("addon.disabled").replaceAll("\\[name\\]", teleportLoc[0])));
										return true;
									}
									break;
								default:
									tellConsole(ColourMessage(langConf.getString("teleport.no-such-addon").replaceAll("\\[name\\]", teleportLoc[0])));
									return true;
							}
							for (Player pOnline : Bukkit.getServer().getOnlinePlayers()) {
								if (!pOnline.hasPermission("regionteleport.teleport.bypass")) {
									switch (teleportLoc[0].toLowerCase()) {
										case "cmi":
											if(args[1].equalsIgnoreCase("__global__") && pOnline.getWorld() == cWorld) {
												if (plugin.cmiAddon.warpLoc(teleportLoc) != null) {
													Location nLoc = plugin.cmiAddon.warpLoc(teleportLoc);
													pOnline.teleport(nLoc);
													teleported++;
												} else {
													tellConsole(ColourMessage(prefix + langConf.getString("spawn.no-such-cmi").replaceAll("\\[name\\]", name)));
													return true;
												}
											} else {
												if(plugin.cmiAddon.warpLoc(teleportLoc) != null) {
													Location loc = pOnline.getLocation();
													BlockVector3 v = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
													World world = pOnline.getWorld();
													RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
													ApplicableRegionSet set = rm.getApplicableRegions(v);
													for (ProtectedRegion r : set) {
														if (r.getId().equalsIgnoreCase(args[1])) {
															Location nLoc = plugin.cmiAddon.warpLoc(teleportLoc);
															pOnline.teleport(nLoc);
															teleported++;
															break;
														}
													}
												} else {
													tellConsole(ColourMessage(prefix + langConf.getString("spawn.no-such-cmi").replaceAll("\\[name\\]", name)));
													return true;
												}
											}
											break;
										case "essentials":
										case "ess":
											if(args[1].equalsIgnoreCase("__global__") && pOnline.getWorld() == cWorld) {
												if (plugin.essAddon.warpLoc(teleportLoc) != null) {
													Location nLoc = plugin.essAddon.warpLoc(teleportLoc);
													pOnline.teleport(nLoc);
													teleported++;
												} else {
													tellConsole(ColourMessage(prefix + langConf.getString("spawn.no-such-essentials").replaceAll("\\[name\\]", name)));
													return true;
												}
											} else {
												if(plugin.essAddon.warpLoc(teleportLoc) != null) {
													Location loc = pOnline.getLocation();
													BlockVector3 v = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
													World world = pOnline.getWorld();
													RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
													ApplicableRegionSet set = rm.getApplicableRegions(v);
													for (ProtectedRegion r : set) {
														if (r.getId().equalsIgnoreCase(args[1])) {
															Location nLoc = plugin.essAddon.warpLoc(teleportLoc);
															pOnline.teleport(nLoc);
															teleported++;
															break;
														}
													}
												} else {
													tellConsole(ColourMessage(prefix + langConf.getString("spawn.no-such-essentials").replaceAll("\\[name\\]", name)));
													return true;
												}
											}
											break;
									}

								}
							}
							if(args.length == 5) {
								if (!args[4].equalsIgnoreCase(("-s"))) {
									tellConsole(ColourMessage(prefix + langConf.getString("teleport.wrong-usage")));
								}
							} else {
								String tpSuccess = langConf.getString("teleport.successful-teleport").replaceAll("\\[name\\]", name);
								tpSuccess = tpSuccess.replaceAll("\\[region\\]", args[1]);
								tpSuccess = tpSuccess.replaceAll("\\[amount\\]", String.valueOf(teleported));
								tellConsole(ColourMessage(prefix + tpSuccess));
							}
						} else {
							String noRegion = langConf.getString("teleport.no-such-region").replaceAll("\\[name\\]", name);
							tellConsole(ColourMessage(prefix + noRegion));
						}
					} else if(spawnConf.contains(args[2])) {
							World cWorld = Bukkit.getWorld(args[3]);
							if(cWorld != null) {
								RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
								RegionManager regions = container.get(BukkitAdapter.adapt(cWorld));
								if (regions.getRegion(args[1]) != null || args[1].equalsIgnoreCase("__global__")) {
									int teleported = 0;
									for (Player pOnline : Bukkit.getServer().getOnlinePlayers()) {
										if(!pOnline.hasPermission("regionteleport.teleport.bypass")) {
											World w = Bukkit.getServer().getWorld(spawnConf.getString(args[2] + ".world"));
											float yaw = (float) spawnConf.getDouble(args[2] + ".yaw");
											float pitch = (float) spawnConf.getDouble(args[2] + ".pitch");
											if (args[1].equalsIgnoreCase("__global__") && pOnline.getWorld() == cWorld) {
												Location location = new Location(w, spawnConf.getDouble(args[2] + ".x"), spawnConf.getDouble(args[2] + ".y"), spawnConf.getDouble(args[2] + ".z"));
												location.setYaw(yaw);
												location.setPitch(pitch);
												pOnline.teleport(location);
												teleported++;
											} else {
												Location location = pOnline.getLocation();
												BlockVector3 v = BlockVector3.at(location.getX(), location.getY(), location.getZ());
												World world = pOnline.getWorld();
												RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
												ApplicableRegionSet set = rm.getApplicableRegions(v);
												for (ProtectedRegion r : set) {
													if (r.getId().equalsIgnoreCase(args[1])) {
														Location nLoc = new Location(w, spawnConf.getDouble(args[2] + ".x"), spawnConf.getDouble(args[2] + ".y"), spawnConf.getDouble(args[2] + ".z"));
														nLoc.setYaw(yaw);
														nLoc.setPitch(pitch);
														pOnline.teleport(nLoc);
														teleported++;
													}
												}
											}
										}
									}
									if(args.length == 5) {
										if (!args[4].equalsIgnoreCase(("-s"))) {
											tellConsole(ColourMessage(prefix + langConf.getString("teleport.wrong-usage")));
										}
									} else {
										String tpSuccess = langConf.getString("teleport.successful-teleport").replaceAll("\\[name\\]", name);
										tpSuccess = tpSuccess.replaceAll("\\[region\\]", args[1]);
										tpSuccess = tpSuccess.replaceAll("\\[amount\\]", String.valueOf(teleported));
										tellConsole(ColourMessage(prefix + tpSuccess));
									}
								} else {
									String noRegion = langConf.getString("teleport.no-such-region").replaceAll("\\[name\\]", args[1]);
									tellConsole(ColourMessage(prefix + noRegion));
								}
							} else {
								String noSuchWorld = langConf.getString("console.no-such-world").replaceAll("\\[name\\]", args[3]);
								tellConsole(ColourMessage(prefix + noSuchWorld));
							}
						} else {
							String noSpawn = langConf.getString("spawn.no-such-spawn").replaceAll("\\[name\\]", name);
							tellConsole(ColourMessage(prefix + noSpawn));
						}

					break;
				case "reload":
					plugin.onReload();
					tellConsole(ColourMessage(langConf.getString("global.reload")));
					break;
				default:
					tellConsole(commandHelp);
					return true;
			}
		}
		return true;
	}
}
