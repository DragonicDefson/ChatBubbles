/*    */ package me.thetealviper.chatbubbles.utils;
/*    */ 
/*    */ import com.gmail.filoghost.holographicdisplays.api.Hologram;
/*    */ import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
         import java.util.Objects;
/*    */ import me.thetealviper.chatbubbles.ChatBubbles;
/*    */ import net.citizensnpcs.api.ai.speech.SpeechContext;
/*    */ import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent;
/*    */ import net.citizensnpcs.api.npc.NPC;
/*    */ import net.citizensnpcs.api.trait.Trait;
/*    */ import org.bukkit.Bukkit;
/*    */ import org.bukkit.entity.LivingEntity;
/*    */ import org.bukkit.entity.Player;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.EventPriority;
/*    */ import org.bukkit.plugin.java.JavaPlugin;
/*    */ import org.bukkit.scheduler.BukkitRunnable;
/*    */
/*    */ public class ChatBubbleTrait extends Trait {
/* 25 */   ChatBubbles plugin;
/*    */   
/*    */   public ChatBubbleTrait() {
/* 28 */     super("chatbubble");
/* 29 */     this.plugin = JavaPlugin.getPlugin(ChatBubbles.class);
/*    */   }
/*    */   
/*    */   @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
/*    */   public void onNPCSpeech(NPCSpeechEvent event) {
/* 34 */     if (this.npc != event.getNPC())
/* 35 */       return;  if (event.getNPC() != null && event.getNPC().isSpawned()) {
/* 36 */       NPC talker = event.getNPC();
/* 37 */       if (talker.getEntity() instanceof LivingEntity) {
/*    */         
/* 39 */         SpeechContext sp = event.getContext();
/* 40 */         String msg = sp.getMessage();
/* 41 */         LivingEntity p = (LivingEntity)talker.getEntity();
/* 42 */         createBubble(p, msg);
/* 43 */         if (this.plugin.chatBubbleOverridesNPCChat) {
/* 44 */           event.setCancelled(true);
/*    */         }
/*    */       } 
/*    */     } 
/*    */   }
/*    */   
/*    */   public void onAttach() {
/* 51 */     this.plugin.getServer().getLogger().info(this.npc.getName() + " has been assigned trait ChatBubble!");
/*    */   }
/*    */   public void createBubble(final LivingEntity p, String msg) {
/* 55 */     if (this.plugin.existingHolograms.containsKey(p.getUniqueId()))
/* 56 */       for (Hologram h : this.plugin.existingHolograms.get(p.getUniqueId())) {
/* 57 */         if (!h.isDeleted()) {
/* 58 */           h.delete();
/*    */         }
/*    */       }  
/* 61 */     final Hologram hologram = HologramsAPI.createHologram(this.plugin, p.getLocation().add(0.0D, this.plugin.bubbleOffset, 0.0D));
/* 62 */     List<Hologram> hList = new ArrayList<>();
/* 63 */     hList.add(hologram);
/* 64 */     this.plugin.existingHolograms.put(p.getUniqueId(), hList);
/* 65 */     hologram.getVisibilityManager().setVisibleByDefault(false);
/* 66 */     for (Player oP : Bukkit.getOnlinePlayers()) {
/* 67 */
             if (oP.getWorld().getName().equals(p.getWorld().getName())) {
                 oP.getLocation().distance(p.getLocation());
                 hologram.getVisibilityManager().showTo(oP);
/*    */     } 
/* 71 */     final int lines = this.plugin.formatHologramLines(p, hologram, msg);
/*    */     
/* 73 */     (new BukkitRunnable() {
/* 74 */         int ticksRun = 0;
/*    */         
/*    */         public void run() {
/* 77 */           this.ticksRun++;
/* 78 */           if (!hologram.isDeleted())
/* 79 */             hologram.teleport(p.getLocation().add(0.0D, ChatBubbleTrait.this.plugin.bubbleOffset + 0.25D * lines, 0.0D)); 
/* 80 */           if (this.ticksRun > ChatBubbleTrait.this.plugin.life) {
/* 81 */             hologram.delete();
/* 82 */             cancel();
/*    */           }  }
/* 84 */       }).runTaskTimer(this.plugin, 1L, 1L);
/*    */     
/* 86 */     if (this.plugin.getConfig().getBoolean("ChatBubble_Play_Sound")) {
/* 87 */       String sound = Objects.requireNonNull(this.plugin.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
/* 88 */       float volume = (float)this.plugin.getConfig().getDouble("ChatBubble_Sound_Volume");
/* 89 */       if (!sound.equals(""))
/*    */         try {
/* 91 */           p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
/* 92 */         } catch (Exception e) {
/* 93 */           Bukkit.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
/* 94 */           Bukkit.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
/*    */         }  
/*    */     } 
/*    */   }
/*    */ }
       }


/* Location:              C:\Users\Bradley Methorst\Desktop\chatbubbles.jar!\me\TheTealViper\chatbubble\\utils\ChatBubbleTrait.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */