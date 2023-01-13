package me.thetealviper.chatbubbles.listeners;

import me.thetealviper.chatbubbles.ChatBubbles;
import me.thetealviper.chatbubbles.handleThree;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListenerLow implements Listener {

    private final ChatBubbles plugin;

    public ChatListenerLow(ChatBubbles plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled() || e.getPlayer().getGameMode().name().equals(GameMode.SPECTATOR.name())) {
            return;
        }
        switch (plugin.getConfig().getInt("ChatBubble_Configuration_Mode")) {
            case 0 -> {
                if (!plugin.getConfig().getBoolean("ChatBubble_Send_Original_Message")) {
                    e.setCancelled(true);
                }
                plugin.handleZero(e.getMessage(), e.getPlayer());
            }
            case 2 -> {
                if (!plugin.getConfig().getBoolean("ChatBubble_Send_Original_Message")) {
                    e.setCancelled(true);
                }
                plugin.handleTwo(e.getMessage(), e.getPlayer());
            }
            case 3 -> {
                if (Bukkit.getServer().getPluginManager().getPlugin("Factions") != null) {
                    handleThree.run(plugin, e.getMessage(), e.getPlayer());
                    break;
                }
                plugin.getServer().getConsoleSender().sendMessage("ChatBubbles is set to configuration mode 3 but Factions can't be found!");
            }
            case 4 -> plugin.handleFour(e.getMessage(), e.getPlayer());
        }
    }
}