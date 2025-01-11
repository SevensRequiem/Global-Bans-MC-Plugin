package org.sevensrequiem.globalBans;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.PluginLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.io.File;


import java.net.HttpURLConnection;
import java.io.IOException;

import org.json.*;


public final class Main extends JavaPlugin {

    private String url;
    private FileConfiguration configFile;
    private Plugin plugin;
    private static Main instance;
    private JSONObject uuid;


    @Override
    public void onEnable() {
        instance = this;
        plugin = this;
        PluginLogger logger = new PluginLogger(this);
        logger.info("GlobalBans is starting up...");
        createConfig();
        generateServerID();
        ping();
        banWatcher();
        logger.info("GlobalBans has started up successfully");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void createConfig() {
        File customFile = new File(getDataFolder(), "config.yml");
        if (!customFile.exists()) {
            customFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }



        configFile = new YamlConfiguration();
        try {
            configFile.load(customFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static Main getInstance() {
        return instance;
    }

private void generateServerID() {
    try {
        url = configFile.getString("url");
        URL url = new URL(this.url);
        // check external IP
        String ip = getExternalIP("https://api.ipify.org?format=string");

        url = new URL(url.toString() + "/api/minecraft/server?ip=" + ip + "&port=" + Bukkit.getServer().getPort());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            String responseString = content.toString();
            if (responseString != null) {
                    getLogger().info("Successfully generated a server ID: " + responseString);
                    configFile.set("serverID", responseString);
                    configFile.save(new File(getDataFolder(), "config.yml"));
                } else {
                    getLogger().warning("Failed to generate a server ID");
            }

        } else {
            getLogger().warning("Failed to generate a server ID");
        }
    } catch (Exception e) {
        getLogger().warning("Failed to generate a server ID: " + e.getMessage());
    }
}

    public void ping() {
        try {
            url = configFile.getString("url");
            URL url = new URL(this.url);
            url = new URL(url.toString() + "/api/minecraft/ping");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                getLogger().info("Successfully pinged the GlobalBans server");
            } else {
                getLogger().warning("Failed to ping the GlobalBans server");
            }
        } catch (Exception e) {
            getLogger().warning("Failed to ping the GlobalBans server");
        }
    }


private String getExternalIP(String urlString) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    int responseCode = connection.getResponseCode();
    if (responseCode == 200) {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }
    return null;
}
}