package me.thetealviper.chatbubbles.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
public class PluginFile extends YamlConfiguration {
    private final File file;
    private final String defaults;
    private final JavaPlugin plugin;
    public PluginFile(JavaPlugin plugin, String fileName) {
        this(plugin, fileName, null);
    }
    public PluginFile(JavaPlugin plugin, String fileName, String defaultsName) {
        this.plugin = plugin;
        this.defaults = defaultsName;
        this.file = new File(plugin.getDataFolder(), fileName);
        reload();
    }
    public void reload() {
        if (!this.file.exists()) {

            try {
                if (this.file.getParentFile().mkdirs()) {
                    Bukkit.getServer().getConsoleSender().sendMessage("Created plugin directory's " + this.file.getAbsolutePath());
                }
                if (this.file.createNewFile()) {
                    Bukkit.getServer().getConsoleSender().sendMessage("Created plugin configuration file " + this.file.getName());
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                Bukkit.getServer().getConsoleSender().sendMessage("Error while creating file " + this.file.getName());
            }
        }
        try {
            load(this.file);
            if (this.defaults != null) {
                InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(this.plugin.getResource(this.defaults)));
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(reader);

                setDefaults(yamlConfiguration);
                options().copyDefaults(true);

                reader.close();
                save();
            }

        } catch (IOException | InvalidConfigurationException exception) {
            exception.printStackTrace();
            Bukkit.getServer().getConsoleSender().sendMessage("Error while loading file " + this.file.getName());
        }
    }
    public void save() {
        try {
            options().indent(2);
            save(this.file);
        } catch (IOException exception) {
            exception.printStackTrace();
            Bukkit.getServer().getConsoleSender().sendMessage("Error while saving file " + this.file.getName());
        }
    }
}