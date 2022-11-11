/*    */ package me.thetealviper.chatbubbles.listeners;
/*    */
/*    */ import io.papermc.paper.event.player.AsyncChatEvent;
         import me.thetealviper.chatbubbles.ChatBubbles;
         import me.thetealviper.chatbubbles.handleThree;
         import org.bukkit.Bukkit;
/*    */ import org.bukkit.GameMode;
/*    */ import org.bukkit.event.EventHandler;
/*    */ import org.bukkit.event.EventPriority;
/*    */ import org.bukkit.event.Listener;
/*    */ 
/*    */ public class ChatListenerLow implements Listener {
/*    */
/*    */   public ChatBubbles plugin;
/*    */   @EventHandler(priority = EventPriority.LOW)
/*    */   public void onChat(AsyncChatEvent e) {
/* 24 */     if (e.isCancelled() || e.getPlayer().getGameMode().name().equals(GameMode.SPECTATOR.name()))
/*    */       return; 
/* 26 */     switch (this.plugin.getConfig().getInt("ChatBubble_Configuration_Mode")) {
/*    */       case 0:
/* 28 */         if (!this.plugin.getConfig().getBoolean("ChatBubble_Send_Original_Message"))
/* 29 */           e.setCancelled(true); 
/* 30 */         this.plugin.handleZero(String.valueOf(e.message()), e.getPlayer());
/*    */         break;
/*    */       case 2:
/* 36 */         if (!this.plugin.getConfig().getBoolean("ChatBubble_Send_Original_Message"))
/* 37 */           e.setCancelled(true); 
/* 38 */         this.plugin.handleTwo(String.valueOf(e.message()), e.getPlayer());
/*    */         break;
/*    */       case 3:
/* 41 */         if (Bukkit.getServer().getPluginManager().getPlugin("Factions") != null) {
/* 42 */           handleThree.run(this.plugin, String.valueOf(e.message()), e.getPlayer()); break;
/*    */         } 
/* 44 */         this.plugin.getServer().getConsoleSender().sendMessage("ChatBubbles is set to configuration mode 3 but Factions can't be found!");
/*    */         break;
/*    */       case 4:
/* 47 */         this.plugin.handleFour(String.valueOf(e.message()), e.getPlayer());
/*    */         break;
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\Bradley Methorst\Desktop\chatbubbles.jar!\me\TheTealViper\chatbubbles\listeners\ChatListenerLow.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */