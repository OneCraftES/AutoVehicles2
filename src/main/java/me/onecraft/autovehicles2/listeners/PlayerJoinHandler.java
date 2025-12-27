package me.onecraft.autovehicles2.listeners;

import me.onecraft.autovehicles2.PlayerConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!(PlayerConfig.getPlayersFileConfig().isSet("players." + e.getPlayer().getUniqueId()))) {
            PlayerConfig.getPlayersFileConfig().set("players."+e.getPlayer().getUniqueId()+".cart.toggled", true);
            PlayerConfig.getPlayersFileConfig().set("players."+e.getPlayer().getUniqueId()+".boat.toggled", true);
            PlayerConfig.savePlayerConfig();
            PlayerConfig.reloadPlayerConfig();
        }
    }

}
