package org.sevensrequiem.globalBans;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

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
        this.getCommand("gban").setExecutor(new CommandGban());
        this.getCommand("gunban").setExecutor(new CommandGunban());
        this.getCommand("gbanlist").setExecutor(new CommandGbanlist());
        this.getCommand("gbanreload").setExecutor(new CommandGbanreload());
        this.getServer().getPluginManager().registerEvents(new Bans(this), this);

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
            URL serverUrl = new URL(url);
            // check external IP
            String ip = getExternalIP("https://api.ipify.org?format=string");

            URL requestUrl = new URL(serverUrl.toString() + "/api/minecraft/server?ip=" + ip + "&port=" + Bukkit.getServer().getPort());
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
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
            URL serverUrl = new URL(url);
            URL requestUrl = new URL(serverUrl.toString() + "/api/minecraft/ping");
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
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

    private void banWatcher() {
        // Implement the banWatcher method
    }

    public static class CommandGban implements CommandExecutor {

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    player.sendMessage("You must specify a player to ban");
                    return false;
                }
                if (args.length == 1) {
                    player.sendMessage("You must specify a reason for the ban");
                    return false;
                }
                Bans bans = new Bans(instance);
                bans.GlobalBan(player.getName(), args[0], args[1], args.length > 2 ? args[2] : "No reason provided");

            }
            return false;
        }
    }

    public static class CommandGunban implements CommandExecutor {

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    player.sendMessage("You must specify a player to unban");
                    return false;
                }
                if (args.length == 1) {
                    try {
                        String url = Main.getInstance().configFile.getString("url");
                        URL serverUrl = new URL(url);
                        URL requestUrl = new URL(serverUrl.toString() + "/api/minecraft/unban?uuid=" + player.getUniqueId());
                        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                        connection.setRequestMethod("POST");
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode == 200) {
                            player.sendMessage("Successfully unbanned " + args[0]);
                        } else {
                            player.sendMessage("Failed to unban " + args[0]);
                        }
                    } catch (Exception e) {
                        Main.getInstance().getLogger().warning("Failed to unban " + args[0] + ": " + e.getMessage());
                    }
                }
            }
            return false;
        }
    }

    public static class CommandGbanlist implements CommandExecutor {

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    try {
                        String url = Main.getInstance().configFile.getString("url");
                        URL serverUrl = new URL(url);
                        URL requestUrl = new URL(serverUrl.toString() + "/api/minecraft/banlist");
                        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();
                        int responseCode = connection.getResponseCode();
                        if (responseCode == 200) {
                            player.sendMessage("Successfully retrieved the ban list");
                        } else {
                            player.sendMessage("Failed to retrieve the ban list");
                        }
                    } catch (Exception e) {
                        Main.getInstance().getLogger().warning("Failed to retrieve the ban list: " + e.getMessage());
                    }
                }
            }
            return false;
        }
    }

    public static class CommandGbanreload implements CommandExecutor {

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    try {
                        Main.getInstance().configFile.load(new File(Main.getInstance().getDataFolder(), "config.yml"));
                        player.sendMessage("Successfully reloaded the config");
                    } catch (Exception e) {
                        Main.getInstance().getLogger().warning("Failed to reload the config: " + e.getMessage());
                    }
                }
            }
            return false;
        }
    }
}