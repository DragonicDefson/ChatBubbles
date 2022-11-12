package me.thetealviper.chatbubbles.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpServer;
import me.thetealviper.chatbubbles.ChatBubbles;
import org.apache.commons.io.FileUtils;

import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class EnableShit {

    public static void handleOnEnable(JavaPlugin plugin, Listener pluginL, String spigotID) {
        plugin.saveDefaultConfig();
        checkUpdates(plugin, spigotID);
        Bukkit.getPluginManager().registerEvents(pluginL, plugin);
        Bukkit.getServer().getConsoleSender().sendMessage(plugin.getDescription().getName() + " from TheTealViper powered ON!");
    }
    public static void checkUpdates(JavaPlugin plugin, String spigotID) {
        if (!spigotID.equals("-1")) {
            updatePlugin(plugin, spigotID);
        }
        updateConfig(plugin);
    }
    public static void updatePlugin(JavaPlugin plugin, String spigotID) {
        String installed = plugin.getDescription().getVersion();
        String[] installed_Arr = installed.split("[.]");
        String posted = plugin.getDescription().getVersion();
        if (posted == null) {
            return;
        }
        String[] posted_Arr = posted.split("[.]");
        for (int i = 0; i < posted_Arr.length; i++) {
            if (installed_Arr.length <= i || Integer.parseInt(installed_Arr[i]) < Integer.parseInt(posted_Arr[i])) {
                Bukkit.getServer().getConsoleSender().sendMessage(String.valueOf(plugin.getDescription().getName()) + " has an update ready [" + installed + " -> " + posted + "]!");
                break;
            }
        }
    }
    public static void updateConfig(JavaPlugin plugin) {
        YamlConfiguration compareTo = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml"))));
        boolean update = !plugin.getConfig().contains("VERSION");
        String oldVersion = plugin.getConfig().getString("VERSION");
        assert oldVersion != null;
        String[] oldVersion_Arr = oldVersion.split("[.]");
        String newVersion = compareTo.getString("VERSION");
        assert newVersion != null;
        String[] newVersion_Arr = newVersion.split("[.]");
        for (int i = 0; i < newVersion_Arr.length; i++) {
            if (oldVersion_Arr.length <= i || Integer.parseInt(oldVersion_Arr[i]) < Integer.parseInt(newVersion_Arr[i])) {
                update = true;
                break;
            }
        }
        if (update) {
            File file = new File("plugins/" + plugin.getDescription().getName() + "/config.yml");
            try {
                FileUtils.copyFile(file, new File("plugins/" + plugin.getDescription().getName() + "/configBACKUP_" + oldVersion + ".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (file.exists()) {
                if (file.delete()) {
                    Bukkit.getServer().getConsoleSender().sendMessage("config.yml has been deleted [" + oldVersion + "]");
                }
            }
            plugin.saveDefaultConfig();
            Bukkit.getServer().getConsoleSender().sendMessage(plugin.getDescription().getName() + " config.yml has been updated [" + oldVersion + " -> " + newVersion + "] and a backup created of old configuration!");
        }
    }
}
