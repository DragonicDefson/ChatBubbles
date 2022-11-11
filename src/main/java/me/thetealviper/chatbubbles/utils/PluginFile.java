/*    */ package me.thetealviper.chatbubbles.utils;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStreamReader;
/*    */ import java.util.Objects;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.configuration.InvalidConfigurationException;
/*    */ import org.bukkit.configuration.file.YamlConfiguration;
/*    */ import org.bukkit.plugin.java.JavaPlugin;
/*    */ public class PluginFile extends YamlConfiguration {
/*    */   private final File file;
/*    */   private final String defaults;
/*    */   private final JavaPlugin plugin;
/*    */   public PluginFile(JavaPlugin plugin, String fileName) {
/* 25 */     this(plugin, fileName, null);
/*    */   }
/*    */   public PluginFile(JavaPlugin plugin, String fileName, String defaultsName) {
/* 35 */     this.plugin = plugin;
/* 36 */     this.defaults = defaultsName;
/* 37 */     this.file = new File(plugin.getDataFolder(), fileName);
/* 38 */     reload();
/*    */   }
/*    */   public void reload() {
/* 46 */     if (!this.file.exists()) {
/*    */       
/*    */       try {
/* 49 */         if (this.file.getParentFile().mkdirs()) {
/* 50 */           Bukkit.getServer().getConsoleSender().sendMessage("Created plugin directory's " + this.file.getAbsolutePath());
                 }
/*    */         if (this.file.createNewFile()) {
/*    */           Bukkit.getServer().getConsoleSender().sendMessage("Created plugin configuration file " + this.file.getName());
                 }
/*    */       }
/* 52 */       catch (IOException exception) {
/* 53 */         exception.printStackTrace();
/* 54 */         Bukkit.getServer().getConsoleSender().sendMessage("Error while creating file " + this.file.getName());
/*    */       }
/*    */     }
/*    */     try {
/* 60 */       load(this.file);
/*    */       
/* 62 */       if (this.defaults != null) {
/* 63 */         InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(this.plugin.getResource(this.defaults)));
/* 64 */         YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(reader);
/*    */         
/* 66 */         setDefaults(yamlConfiguration);
/* 67 */         options().copyDefaults(true);
/*    */         
/* 69 */         reader.close();
/* 70 */         save();
/*    */       }
/*    */     
/* 73 */     } catch (IOException exception) {
/* 74 */       exception.printStackTrace();
/* 75 */       Bukkit.getServer().getConsoleSender().sendMessage("Error while loading file " + this.file.getName());
/*    */     }
/* 77 */     catch (InvalidConfigurationException exception) {
/* 78 */       exception.printStackTrace();
/* 79 */       Bukkit.getServer().getConsoleSender().sendMessage("Error while loading file " + this.file.getName());
/*    */     } 
/*    */   }
/*    */   public void save() {
/*    */     try {
/* 91 */       options().indent(2);
/* 92 */       save(this.file);
/*    */     }
/* 94 */     catch (IOException exception) {
/* 95 */       exception.printStackTrace();
/* 96 */       Bukkit.getServer().getConsoleSender().sendMessage("Error while saving file " + this.file.getName());
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Bradley Methorst\Desktop\chatbubbles.jar!\me\TheTealViper\chatbubble\\utils\PluginFile.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */