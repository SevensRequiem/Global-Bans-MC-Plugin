package org.sevensrequiem.globalBans;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginLogger;


import java.net.URL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;


public final class Bans implements Listener {
    private String url;
    private FileConfiguration configFile;
    private Plugin plugin;
    private static Main instance;
    private String uuid;


    public Bans(Main instance) {
        this.instance = instance;
        this.plugin = instance;
        PluginLogger logger = new PluginLogger(instance);

        this.configFile = instance.getConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // check if player is banned
        // if player is banned, kick them
        // if player is not banned, do nothing

    }


    public void RetrieveSelfBanList() {
        try {
            url = configFile.getString("url");
            uuid = configFile.getString("uuid");
            URL url = new URL(this.url);

            url = new URL(url.toString() + "/api/minecraft/selfbanlist?uuid=" + uuid);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // parse response and store in a list in sqlite
            }
        } catch (Exception e) {
            instance.getLogger().warning("Failed to retrieve self ban list: " + e.getMessage());

        }
    }

    public void RetrieveGlobalBanList() {
        try {
            url = configFile.getString("url");
            URL url = new URL(this.url);
            url = new URL(url.toString() + "/api/minecraft/banlist");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // parse response and store in a list in sqlite
            }
        } catch (Exception e) {
            instance.getLogger().warning("Failed to retrieve global ban list: " + e.getMessage());
        }
    }

    public void GlobalBan(String admin, String player, String expires, String reason) {
        try {
            uuid = configFile.getString("uuid");
            url = configFile.getString("url");
            URL url = new URL(this.url);
            url = new URL(url.toString() + "/api/minecraft/ban?admin=" + admin + "&reason=" + reason + "&expires=" + expires + "&player=" + player + "&server=" + uuid);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                instance.getLogger().info("Successfully banned player: " + player);
            }
        } catch (Exception e) {
            instance.getLogger().warning("Failed to ban player: " + e.getMessage());
        }
    }
}