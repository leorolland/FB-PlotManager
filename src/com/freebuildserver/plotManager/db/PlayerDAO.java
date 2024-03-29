package com.freebuildserver.plotManager.db;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

public class PlayerDAO extends BasicDAO<PlayerDTO, String> {

	Plugin plugin;

	public PlayerDAO(Class<PlayerDTO> entityClass, Datastore ds, Plugin plugin) {
		super(entityClass, ds);
		this.plugin = plugin;
	}

	public PlayerDTO updatePlayerSync(Player p) {
		final PlayerDAO that = this;
		PlayerDTO dp = this.findOne("uuid", p.getUniqueId().toString());
		if (dp == null)
			dp = new PlayerDTO();
		dp.setUuid(p.getUniqueId().toString());
		dp.setIp(p.getAddress().getHostString());
		dp.setUsername(p.getName());
		dp.setXp(0);
		dp.setLastLogin(new Date());
		Map<String, String> loc = new HashMap<String, String>();
		loc.put("x", String.valueOf(p.getLocation().getBlockX()));
		loc.put("y", String.valueOf(p.getLocation().getBlockY()));
		loc.put("z", String.valueOf(p.getLocation().getBlockZ()));
		loc.put("world", p.getWorld().getName());
		dp.setLocation(loc);
		this.save(dp);
		return dp;
	}

	public PlayerDTO getPlayerSync(Player p) {
		PlayerDTO dp = this.findOne("uuid", p.getUniqueId().toString());
		if (dp == null)
			dp = updatePlayerSync(p);
		return dp;
	}

	public Optional<PlayerDTO> getPlayerSync(String username) {
		return Optional.ofNullable(this.findOne("username", username));
	}
	
	/**
	 * SAME FUNCTIONS BUT ASYNC
	 */

	public void getPlayerAsync(final Player p, final AsyncCallback<PlayerDTO> callback) {
		final Instant begin = Instant.now();
		final PlayerDAO that = this;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				PlayerDTO pd = getPlayerSync(p);
				callback.callback(pd, Duration.between(begin, Instant.now()));
			}
		});
	}

	public void updatePlayerAsync(final Player p, final AsyncCallback<PlayerDTO> callback) {
		final PlayerDAO that = this;
		final Instant begin = Instant.now();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
            	PlayerDTO pd = updatePlayerSync(p);
        		callback.callback(pd, Duration.between(begin, Instant.now()));
            }
        });
	}
	
	public void getPlayerAsync(final String p, final AsyncCallback<Optional<PlayerDTO>> callback) {
		final Instant begin = Instant.now();
		final PlayerDAO that = this;
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				Optional<PlayerDTO> opt = getPlayerSync(p);
				callback.callback(opt, Duration.between(begin, Instant.now()));
			}
		});
	}

}
