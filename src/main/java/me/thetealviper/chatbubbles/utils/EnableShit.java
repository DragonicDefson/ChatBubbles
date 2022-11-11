/*    */ package me.thetealviper.chatbubbles.utils;
/*    */ 
/*    */ import java.io.BufferedReader;
/*    */ import java.io.File;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStreamReader;
/*    */ import java.net.HttpURLConnection;
/*    */ import org.apache.commons.io.FileUtils;
/*    */ import java.net.URL;
         import java.nio.charset.StandardCharsets;
         import java.util.Objects;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.configuration.file.YamlConfiguration;
/*    */ import org.bukkit.event.Listener;
/*    */ import org.bukkit.plugin.java.JavaPlugin;
/*    */ public class EnableShit
/*    */ {
/*    */   public static void handleOnEnable(JavaPlugin plugin, Listener pluginL, String spigotID) {
/* 19 */     plugin.saveDefaultConfig();
/* 20 */     checkUpdates(plugin, spigotID);
/* 21 */     Bukkit.getPluginManager().registerEvents(pluginL, plugin);
/* 22 */     Bukkit.getServer().getConsoleSender().sendMessage(plugin.getDescription().getName() + " from TheTealViper powered ON!");
/*    */   }
/*    */   public static void checkUpdates(JavaPlugin plugin, String spigotID) {
/* 26 */     if (!spigotID.equals("-1"))
/* 27 */       updatePlugin(plugin, spigotID); 
/* 28 */     updateConfig(plugin);
/*    */   }
/*    */   public static void updatePlugin(JavaPlugin plugin, String spigotID) {
/* 31 */     String installed = plugin.getDescription().getVersion();
/* 32 */     String[] installed_Arr = installed.split("[.]");
/* 33 */     String posted = getSpigotVersion(spigotID);
/* 34 */     if (posted == null)
/*    */       return; 
/* 36 */     String[] posted_Arr = posted.split("[.]");
/* 37 */     for (int i = 0; i < posted_Arr.length; i++) {
/* 38 */       if (installed_Arr.length <= i || Integer.parseInt(installed_Arr[i]) < Integer.parseInt(posted_Arr[i])) {
/* 39 */         Bukkit.getServer().getConsoleSender().sendMessage(String.valueOf(plugin.getDescription().getName()) + " has an update ready [" + installed + " -> " + posted + "]!");
/*    */         break;
/*    */       } 
/*    */     } 
/*    */   }
/*    */   public static void updateConfig(JavaPlugin plugin) {
/* 45 */     YamlConfiguration compareTo = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml"))));
/* 46 */     boolean update = !plugin.getConfig().contains("VERSION");
             String oldVersion = plugin.getConfig().getString("VERSION");
             assert oldVersion != null;
             String[] oldVersion_Arr = oldVersion.split("[.]");
/* 51 */     String newVersion = compareTo.getString("VERSION");
             assert newVersion != null;
             String[] newVersion_Arr = newVersion.split("[.]");
/* 53 */     for (int i = 0; i < newVersion_Arr.length; i++) {
/* 54 */       if (oldVersion_Arr.length <= i || Integer.parseInt(oldVersion_Arr[i]) < Integer.parseInt(newVersion_Arr[i])) {
/* 55 */         update = true;
/*    */         break;
/*    */       } 
/*    */     } 
/* 59 */     if (update) {
/* 60 */       File file = new File("plugins/" + plugin.getDescription().getName() + "/config.yml");
/*    */       try {
/* 62 */         FileUtils.copyFile(file, new File("plugins/" + plugin.getDescription().getName() + "/configBACKUP_" + oldVersion + ".yml"));
/* 63 */       } catch (IOException e) {
/* 64 */         e.printStackTrace();
/*    */       } 
/* 66 */       if (file.exists()) {
                if (file.delete()) {
                    Bukkit.getServer().getConsoleSender().sendMessage("config.yml has been deleted [" + oldVersion + "]");
                }
               }
/* 68 */       plugin.saveDefaultConfig();
/* 69 */       Bukkit.getServer().getConsoleSender().sendMessage(plugin.getDescription().getName() + " config.yml has been updated [" + oldVersion + " -> " + newVersion + "] and a backup created of old configuration!");
/*    */     } 
/*    */   }
/*    */   private static String getSpigotVersion(String spigotID) {
/*    */     try {
/* 74 */       HttpURLConnection con = (HttpURLConnection)(new URL("http://www.spigotmc.org/api/general.php")).openConnection();
/* 75 */       con.setDoOutput(true);
/* 76 */       con.setRequestMethod("POST");
/* 77 */       con.getOutputStream().write((
/* 78 */           "key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=" + spigotID).getBytes(StandardCharsets.UTF_8));
/*    */       return (new BufferedReader(new InputStreamReader(con.getInputStream()))).readLine();
/* 81 */     } catch (Exception exception) {
/* 84 */       return null;
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Bradley Methorst\Desktop\chatbubbles.jar!\me\TheTealViper\chatbubble\\utils\EnableShit.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */