package com.comze_instancelabs.founddiamonds;

import java.awt.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * 
 * @author instancelabs
 *
 */

public class Main extends JavaPlugin implements Listener{
	
	
	HashMap<Player, int[]> count = new HashMap<Player, int[]>();
	HashMap<Player, String> joindate = new HashMap<Player, String>(); // player -> join date
	HashMap<Player, Integer> diaores = new HashMap<Player, Integer>();
	HashMap<Player, Integer> emeraldores = new HashMap<Player, Integer>();
	HashMap<Player, Integer> goldores = new HashMap<Player, Integer>();
	HashMap<Player, Integer> spawner = new HashMap<Player, Integer>();
	
	HashMap<Player, Integer> lastdiaores = new HashMap<Player, Integer>();
	HashMap<Player, Integer> lastemeraldores = new HashMap<Player, Integer>();
	HashMap<Player, Integer> lastgoldores = new HashMap<Player, Integer>();
	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		
		
		/**
		 *  More than 30 diamonds in 5 minutes -> possible xraying
		 *  More than 20 emeralds in 5 minutes -> possible xraying
		 *  More than 40 gold ores in 5 minutes -> possible xraying
		 */
		
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run(){
				for(Player p : Bukkit.getOnlinePlayers()){
					if(joindate.containsKey(p)){
						if(((diaores.get(p) - lastdiaores.get(p)) > 30) || ((emeraldores.get(p) - lastemeraldores.get(p)) > 20) || ((goldores.get(p) - lastgoldores.get(p)) > 40)){
							for(Player p_ : Bukkit.getOnlinePlayers()){
								if(p_.isOp()){
									p_.sendMessage("§3[FoundDiamonds] §4Possible xraying: " + p.getName());
								}
							}
						}
					}
					lastdiaores.put(p, diaores.get(p));
					lastemeraldores.put(p, emeraldores.get(p));
					lastgoldores.put(p, goldores.get(p));
				}
			}
		}, 6000, 6000); // <- 5 minutes
	}
	

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("fdstats") && args.length > 0){
			if(args.length > 0){
				sender.sendMessage("§3Statistics for " +  args[0] +  "'s session:");
				Player p = Bukkit.getPlayer(args[0]);
				sender.sendMessage("§4Join Date: " + joindate.get(p));
				sender.sendMessage("§bDiamond Ores: " + diaores.get(p));
				sender.sendMessage("§2Emerald Ores: " + emeraldores.get(p));
				sender.sendMessage("§eGold Ores: " + goldores.get(p));
				sender.sendMessage("§8Spawner: " + spawner.get(p));
			}else{
				sender.sendMessage("§4You need to provide a player!");
			}
			
			return true;
		}
	
		return false;
	}
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		DateFormat dateFormat = new SimpleDateFormat("dd.MM HH:mm");
		Date date = new Date();
		Player p = event.getPlayer();
		joindate.put(p, dateFormat.format(date));
		diaores.put(p, 0);
		emeraldores.put(p, 0);
		goldores.put(p, 0);
		spawner.put(p, 0);
		
		lastdiaores.put(p, 0);
		lastemeraldores.put(p, 0);
		lastgoldores.put(p, 0);
	}
	
	
	
	// HERE: THE MOST NASTY BUG EVER
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		if(event.getBlock().getType() == Material.DIAMOND_ORE){
			
			diaores.put(event.getPlayer(), diaores.get(event.getPlayer()) + 1);
			
			if(count.containsKey(event.getPlayer())){
				int[] countp = count.get(event.getPlayer());
				countp[0] += 1;
				count.put(event.getPlayer(), countp);
			}else{
				int[] countp = new int[]{0, 0, 0};
				countp[0] = 1;
				countp[1] = 0;
				countp[2] = 0;
				count.put(event.getPlayer(), countp);
			}
			
			if(count.get(event.getPlayer())[0] > 4){
				for(Player p : Bukkit.getServer().getOnlinePlayers()){
					// p.hasPermission("founddiamonds.notification") 
					if(p.hasPermission("founddiamonds.notification")){
						p.sendMessage("§b" + event.getPlayer().getName() + " broke " + Integer.toString(count.get(event.getPlayer())[0]) + " diamond ore.");
					}
				}
				int[] countp = count.get(event.getPlayer());
				countp[0] = 0;
				count.put(event.getPlayer(), countp);
			}
		}else if(event.getBlock().getType() == Material.EMERALD_ORE){
			
			emeraldores.put(event.getPlayer(), emeraldores.get(event.getPlayer()) + 1);
			
			if(count.containsKey(event.getPlayer())){
				int[] countp = count.get(event.getPlayer());
				countp[1] += 1;
				count.put(event.getPlayer(), countp);
			}else{
				int[] countp = new int[]{0, 0, 0};
				countp[0] = 0;
				countp[1] = 1;
				countp[2] = 0;
				count.put(event.getPlayer(), countp);
			}
			
			if(count.get(event.getPlayer())[1] > 4){
				for(Player p : Bukkit.getServer().getOnlinePlayers()){
					if(p.hasPermission("founddiamonds.notification")){
						p.sendMessage("§2" + event.getPlayer().getName() + " broke " + Integer.toString(count.get(event.getPlayer())[1]) + " emerald ore.");
					}
				}
				int[] countp = count.get(event.getPlayer());
				countp[1] = 0;
				count.put(event.getPlayer(), countp);
			}
		}else if(event.getBlock().getType() == Material.GOLD_ORE){
			
			goldores.put(event.getPlayer(), goldores.get(event.getPlayer()) + 1);
			
			if(count.containsKey(event.getPlayer())){
				int[] countp = count.get(event.getPlayer());
				countp[2] += 1;
				count.put(event.getPlayer(), countp);
			}else{
				int[] countp = new int[]{0, 0, 0};
				countp[0] = 0;
				countp[1] = 0;
				countp[2] = 1;
				count.put(event.getPlayer(), countp);
			}
			
			if(count.get(event.getPlayer())[2] > 4){
				for(Player p : Bukkit.getServer().getOnlinePlayers()){
					if(p.hasPermission("founddiamonds.notification")){
						p.sendMessage("§e" + event.getPlayer().getName() + " broke " + Integer.toString(count.get(event.getPlayer())[2]) + " gold ore.");
					}
				}
				int[] countp = count.get(event.getPlayer());
				countp[2] = 0;
				count.put(event.getPlayer(), countp);
			}
		}else if(event.getBlock().getType() == Material.MOB_SPAWNER){
			
			spawner.put(event.getPlayer(), spawner.get(event.getPlayer()) + 1);
			
			for(Player p : Bukkit.getServer().getOnlinePlayers()){
				if(p.hasPermission("founddiamonds.notification")){
					p.sendMessage("§8" + event.getPlayer().getName() + " broke a spawner.");
				}
			}
		}
	}
}
