/*    */ package me.thetealviper.chatbubbles;
/*    */ 
/*    */ import com.gmail.filoghost.holographicdisplays.api.Hologram;
/*    */ import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
/*    */ import com.massivecraft.factions.entity.MPlayer;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
         import java.util.Objects;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.scheduler.BukkitRunnable;
/*    */ public class handleThree
/*    */ {
/*    */   public static void run(final ChatBubbles cb, final String message, final Player p) {
/* 16 */     final boolean sendOriginal = cb.getConfig().getBoolean("ChatBubble_Send_Original_Message");
/* 17 */     final boolean requirePerm = cb.getConfig().getBoolean("ConfigOne_Require_Permissions");
/* 18 */     final String usePerm = cb.getConfig().getString("ConfigOne_Use_Permission");
/* 19 */     (new BukkitRunnable() {
/*    */         public void run() {
/* 22 */           if (requirePerm) {
                    assert usePerm != null;
                    if (!p.hasPermission(usePerm)) return;
                   }
/* 24 */           if (cb.existingHolograms.containsKey(p.getUniqueId()))
/* 25 */             for (Hologram h : cb.existingHolograms.get(p.getUniqueId())) {
/* 26 */               if (!h.isDeleted()) {
/* 27 */                 h.delete();
/*    */               }
/*    */             }  
/* 30 */           MPlayer mPlayer = MPlayer.get(p);
/* 31 */           String faction = mPlayer.getFactionName();
/* 32 */           final Hologram hologram = HologramsAPI.createHologram(cb, p.getLocation().add(0.0D, cb.bubbleOffset, 0.0D));
/* 33 */           List<Hologram> hList = new ArrayList<>();
/* 34 */           hList.add(hologram);
/* 35 */           cb.existingHolograms.put(p.getUniqueId(), hList);
/* 36 */           hologram.getVisibilityManager().setVisibleByDefault(false);
/* 37 */           for (Player oP : Bukkit.getOnlinePlayers()) {
/* 38 */             if ((cb.seeOwnBubble || !oP.getName().equals(p.getName())) && oP.getWorld().getName().equals(p.getWorld().getName()) && oP.getLocation().distance(p.getLocation()) <= cb.distance && MPlayer.get(oP).getFactionName().equals(faction) && oP.canSee(p))
                     {
/* 43 */               hologram.getVisibilityManager().showTo(oP);
                     }
/*    */           } 
/* 45 */           final int lines = cb.formatHologramLines(p, hologram, message);
/* 46 */           if (sendOriginal) {
/* 47 */             p.chat(message);
/*    */           }
/* 49 */           (new BukkitRunnable() {
/* 50 */               int ticksRun = 0;
/*    */               
/*    */               public void run() {
/* 53 */                 this.ticksRun++;
/* 54 */                 if (!hologram.isDeleted())
/* 55 */                   hologram.teleport(p.getLocation().add(0.0D, cb.bubbleOffset + 0.25D * lines, 0.0D)); 
/* 56 */                 if (this.ticksRun > cb.life) {
/* 57 */                   hologram.delete();
/* 58 */                   cancel();
/*    */                 }  }
/* 60 */             }).runTaskTimer(cb, 1L, 1L);
/*    */           
/* 62 */           if (cb.getConfig().getBoolean("ChatBubble_Play_Sound")) {
/* 63 */             String sound = Objects.requireNonNull(cb.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
/* 64 */             float volume = (float)cb.getConfig().getDouble("ChatBubble_Sound_Volume");
/* 65 */             if (!sound.equals(""))
/*    */               try {
/* 67 */                 p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
/* 68 */               } catch (Exception e) {
/* 69 */                 cb.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
/* 70 */                 cb.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
/*    */               }  
/*    */           } 
/*    */         }
/* 74 */       }).runTask(cb);
/*    */   }
/*    */ }


/* Location:              C:\Users\Bradley Methorst\Desktop\chatbubbles.jar!\me\TheTealViper\chatbubbles\handleThree.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */