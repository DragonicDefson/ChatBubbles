package me.thetealviper.chatbubbles;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class placeholderShit {
    public static String formatString(Player p, String s) {
        return PlaceholderAPI.setPlaceholders(p, s);
    }
}