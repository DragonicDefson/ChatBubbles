package me.thetealviper.chatbubbles;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.thetealviper.chatbubbles.utils.ChatBubbleTrait;
import me.thetealviper.chatbubbles.listeners.*;
import me.thetealviper.chatbubbles.utils.EnableShit;
import me.thetealviper.chatbubbles.utils.PluginFile;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class ChatBubbles extends JavaPlugin implements CommandExecutor, Listener {
    public int life = -1;
    public int distance = -1;
    public int length = -1;
    public String prefix = "";
    public String suffix = "";
    public boolean seeOwnBubble = false;
    public boolean chatBubbleOverridesNPCChat;
    public double bubbleOffset = 2.5D;
    public EventPriority chatPriority = EventPriority.NORMAL;
    public PluginFile togglePF;
    private ChatBubbles plugin;
    public Map < UUID, List < Hologram >> existingHolograms = new HashMap <>();
    public void onEnable() {
        this.plugin = this;
        setChatPriority(this.chatPriority);
        EnableShit.handleOnEnable(plugin, this, "49387");
        this.life = getConfig().getInt("ChatBubble_Life");
        this.distance = getConfig().getInt("ChatBubble_Viewing_Distance");
        this.length = getConfig().getInt("ChatBubble_Maximum_Line_Length");
        this.prefix = getConfig().getString("ChatBubble_Message_Prefix");
        this.seeOwnBubble = getConfig().getBoolean("ChatBubble_See_Own_Bubbles");
        this.bubbleOffset = getConfig().getDouble("ChatBubble_Height_Offset");
        boolean useTrait = getConfig().getBoolean("Use_ChatBubble_Trait_Citizens");
        this.chatBubbleOverridesNPCChat = getConfig().getBoolean("ChatBubble_Overrides_NPC_Chat");
        this.chatPriority = EventPriority.valueOf(Objects.requireNonNull(getConfig().getString("ChatBubble_Chat_Priority")).toUpperCase());
        this.togglePF = new PluginFile(plugin, "toggleData");
        if (this.prefix == null) { this.prefix = ""; }
        this.suffix = getConfig().getString("ChatBubble_Message_Suffix");
        if (this.suffix == null) { this.suffix = ""; }
        if (Objects.requireNonNull(getServer().getPluginManager().getPlugin("Citizens")).isEnabled() && useTrait) {
            Bukkit.getServer().getConsoleSender().sendMessage("Citizens found and trait chatbubble enabled");
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ChatBubbleTrait.class).withName("chatbubble"));
        }
    }
    public void onDisable() {
        getLogger().info(makeColors("ChatBubbles from TheTealViper shutting down."));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            if (label.equalsIgnoreCase("cb") || label.equalsIgnoreCase("chatbubble")) {
                if(args.length == 0){
                    return false;
                } else {
                    StringBuilder message = new StringBuilder();
                    for (int i = 0; i < args.length; i++) {
                        if (i == args.length - 1) {
                            message.append(args[i]);
                        } else {
                            message.append(args[i]).append(" ");
                        }
                    }
                    handleOne(message.toString(), player);
                }
            }

            if (label.equalsIgnoreCase("cbreload") || label.equalsIgnoreCase("chatbubblereload")  && player.hasPermission("chatbubbles.reload")) {
                reloadConfig();
                player.sendMessage("Reloaded Successfully");
            }

            if (label.equalsIgnoreCase("cbtoggle") || label.equalsIgnoreCase("chatbubbletoggle") && player.hasPermission("chatbubbles.toggle")) {
                boolean currentState = togglePF.getBoolean(player.getUniqueId().toString());
                if (currentState) {
                    player.sendMessage("ChatBubbles toggled off!");
                } else {
                    player.sendMessage("ChatBubbles toggled on!");
                }
                togglePF.set(player.getUniqueId().toString(), !currentState);
                togglePF.save();
            }
        }
        return false;
    }

    private void setChatPriority(EventPriority priority) {
        switch (priority) {
            case LOWEST -> getServer().getPluginManager().registerEvents(new ChatListenerLowest(this), this);
            case LOW -> getServer().getPluginManager().registerEvents(new ChatListenerLow(this), this);
            case NORMAL -> getServer().getPluginManager().registerEvents(new ChatListenerNormal(this), this);
            case HIGH -> getServer().getPluginManager().registerEvents(new ChatListenerHigh(this), this);
            case HIGHEST -> getServer().getPluginManager().registerEvents(new ChatListenerHighest(this), this);
            case MONITOR -> getServer().getPluginManager().registerEvents(new ChatListenerMonitor(this), this);
        }
        getServer().getPluginManager().registerEvents(new ChatListenerNormal(this), this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!this.togglePF.contains(p.getUniqueId().toString())) {
            this.togglePF.set(p.getUniqueId().toString(), Boolean.TRUE);
            this.togglePF.save();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        this.existingHolograms.remove(e.getPlayer().getUniqueId());
    }
    public void handleZero(final String message, final Player p) {
        (new BukkitRunnable() {
            public void run() {
                if (ChatBubbles.this.getConfig().getBoolean("ChatBubble_Play_Sound")) {
                    String sound = Objects.requireNonNull(ChatBubbles.this.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
                    float volume = (float) ChatBubbles.this.getConfig().getDouble("ChatBubble_Sound_Volume");
                    if (!sound.equals("")) {
                        try {
                            p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
                        } catch (Exception e) {
                            ChatBubbles.this.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
                            ChatBubbles.this.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
                        }
                    }
                }
                if (!ChatBubbles.this.togglePF.getBoolean(p.getUniqueId().toString())) {
                    return;
                }
                if (ChatBubbles.this.existingHolograms.containsKey(p.getUniqueId())) {
                    for (Hologram h : ChatBubbles.this.existingHolograms.get(p.getUniqueId())) {
                        if (!h.isDeleted()) {
                            h.delete();
                        }
                    }
                }
                final Hologram hologram = HologramsAPI.createHologram(ChatBubbles.this.plugin, p.getLocation().add(0.0D, ChatBubbles.this.bubbleOffset, 0.0D));
                List<Hologram> hList = new ArrayList<>();
                hList.add(hologram);
                ChatBubbles.this.existingHolograms.put(p.getUniqueId(), hList);
                hologram.getVisibilityManager().setVisibleByDefault(false);
                for (Player oP : Bukkit.getOnlinePlayers()) {
                    if ((ChatBubbles.this.seeOwnBubble || !oP.getName().equals(p.getName())) && oP.getWorld().getName().equals(p.getWorld().getName()) && oP.getLocation().distance(p.getLocation()) <= ChatBubbles.this.distance) {
                        hologram.getVisibilityManager().showTo(oP);
                    }
                }
                final int lines = ChatBubbles.this.formatHologramLines(p, hologram, message);
                (new BukkitRunnable() {
                    int ticksRun = 0;
                    public void run() {
                        this.ticksRun++;
                        if (!hologram.isDeleted())
                            hologram.teleport(p.getLocation().add(0.0D,  ChatBubbles.this.bubbleOffset + 0.25D * lines, 0.0D));
                        if (this.ticksRun > ChatBubbles.this.life) {
                            hologram.delete();
                            cancel();
                        }
                    }
                }).runTaskTimer(ChatBubbles.this.plugin, 1L, 1L);
            }
        }).runTask(this);
    }

    public void handleOne(final String message, final Player p) {
        final boolean sendOriginal = getConfig().getBoolean("ChatBubble_Send_Original_Message");
        final boolean requirePerm = getConfig().getBoolean("ConfigOne_Require_Permissions");
        final String usePerm = getConfig().getString("ConfigOne_Use_Permission");
        final String seePerm = getConfig().getString("ConfigOne_See_Permission");
        (new BukkitRunnable() {
            public void run() {
                if (requirePerm) {
                    assert usePerm != null;
                    if (!p.hasPermission(usePerm)) {
                        p.sendMessage("You don't have the permission to use this command.");
                        return;
                    }
                }
                if (ChatBubbles.this.getConfig().getBoolean("ChatBubble_Play_Sound")) {
                    String sound = Objects.requireNonNull(ChatBubbles.this.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
                    float volume = (float) ChatBubbles.this.getConfig().getDouble("ChatBubble_Sound_Volume");
                    if (!sound.equals("")) {
                        try {
                            p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
                        } catch (Exception e) {
                            ChatBubbles.this.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
                            ChatBubbles.this.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
                        }
                    }
                }
                if (!ChatBubbles.this.togglePF.getBoolean(p.getUniqueId().toString())) {
                    return;
                }
                if (ChatBubbles.this.existingHolograms.containsKey(p.getUniqueId())) {
                    for (Hologram h : ChatBubbles.this.existingHolograms.get(p.getUniqueId())) {
                        if (!h.isDeleted()) {
                            h.delete();
                        }
                    }
                }
                final Hologram hologram = HologramsAPI.createHologram(ChatBubbles.this.plugin, p.getLocation().add(0.0D, ChatBubbles.this.bubbleOffset, 0.0D));
                List<Hologram> hList = new ArrayList<>();
                hList.add(hologram);
                ChatBubbles.this.existingHolograms.put(p.getUniqueId(), hList);
                hologram.getVisibilityManager().setVisibleByDefault(false);
                for (Player oP : Bukkit.getOnlinePlayers()) {
                    if ((ChatBubbles.this.seeOwnBubble || !oP.getName().equals(p.getName())) && oP.getWorld().getName().equals(p.getWorld().getName())) {
                        oP.getLocation().distance(p.getLocation());
                    }
                    if (oP.hasPermission(Objects.requireNonNull(seePerm)) && oP.canSee(p)) {
                        hologram.getVisibilityManager().showTo(oP);
                    }
                }
                final int lines = ChatBubbles.this.formatHologramLines(p, hologram, message);
                if (sendOriginal) {
                    p.chat(message);
                }
                (new BukkitRunnable() {
                    int ticksRun = 0;
                    public void run() {
                        this.ticksRun++;
                        if (!hologram.isDeleted())
                            hologram.teleport(p.getLocation().add(0.0D, ((ChatBubbles.this.bubbleOffset + 0.25D * lines)),  0.0D));
                        if (this.ticksRun > (ChatBubbles.this.life)) {
                            hologram.delete();
                            cancel();
                        }
                    }
                }).runTaskTimer(ChatBubbles.this.plugin, 1L, 1L);
            }
        }).runTask(this);
    }
    public void handleTwo(final String message, final Player p) {
        final boolean sendOriginal = getConfig().getBoolean("ChatBubble_Send_Original_Message");
        (new BukkitRunnable() {
            public void run() {
                if (ChatBubbles.this.getConfig().getBoolean("ChatBubble_Play_Sound")) {
                    String sound = Objects.requireNonNull(ChatBubbles.this.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
                    float volume = (float) ChatBubbles.this.getConfig().getDouble("ChatBubble_Sound_Volume");
                    if (!sound.equals("")) {
                        try {
                            p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
                        } catch (Exception e) {
                            ChatBubbles.this.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
                            ChatBubbles.this.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
                        }
                    }
                }
                if (!ChatBubbles.this.togglePF.getBoolean(p.getUniqueId().toString())) {
                    return;
                }
                if (ChatBubbles.this.existingHolograms.containsKey(p.getUniqueId())) {
                    for (Hologram h : ChatBubbles.this.existingHolograms.get(p.getUniqueId())) {
                        if (!h.isDeleted()) {
                            h.delete();
                        }
                    }
                }
                String permGroup = null;
                for (String testPerm : ChatBubbles.this.getConfig().getStringList("ConfigTwo_Permision_Groups")) {
                    if (p.hasPermission(testPerm)) {
                        permGroup = testPerm;
                        break;
                    }
                }
                assert permGroup != null;
                if (permGroup.equals("")) {
                    return;
                }
                final Hologram hologram = HologramsAPI.createHologram(ChatBubbles.this.plugin, p.getLocation().add(0.0D, ChatBubbles.this.bubbleOffset, 0.0D));
                List<Hologram> hList = new ArrayList<>();
                hList.add(hologram);
                ChatBubbles.this.existingHolograms.put(p.getUniqueId(), hList);
                hologram.getVisibilityManager().setVisibleByDefault(false);
                for (Player oP : Bukkit.getOnlinePlayers()) {
                    if ((ChatBubbles.this.seeOwnBubble || !oP.getName().equals(p.getName())) && oP.getWorld().getName().equals(p.getWorld().getName()) && oP.getLocation().distance(p.getLocation()) <= ChatBubbles.this.distance && oP.hasPermission(permGroup) && oP.canSee(p)) {
                        hologram.getVisibilityManager().showTo(oP);
                    }
                }
                final int lines = ChatBubbles.this.formatHologramLines(p, hologram, message);
                if (sendOriginal) {
                    p.chat(message);
                }
                (new BukkitRunnable() {
                    int ticksRun = 0;

                    public void run() {
                        this.ticksRun++;
                        if (!hologram.isDeleted()) {
                            hologram.teleport(p.getLocation().add(0.0D, ChatBubbles.this.bubbleOffset + 0.25D * lines, 0.0D));
                            if (this.ticksRun > ChatBubbles.this.life) {
                                hologram.delete();
                                cancel();
                            }
                        }
                    }
                }).runTaskTimer(ChatBubbles.this.plugin, 1L, 1L);
            }
        }).runTask(this);
    }
    public void handleFour(final String message, final Player p) {
        final boolean requirePerm = getConfig().getBoolean("ConfigZero_Require_Permissions");
        final String usePerm = getConfig().getString("ConfigZero_Use_Permission");
        (new BukkitRunnable() {
            public void run() {
                if (requirePerm) {
                    assert usePerm != null;
                    if (!p.hasPermission(usePerm)) {
                        p.sendMessage("You don't have the permission to use this command.");
                        return;
                    }
                    if (!ChatBubbles.this.togglePF.getBoolean(p.getUniqueId().toString())) {
                        return;
                    }
                    if (ChatBubbles.this.getConfig().getBoolean("ChatBubble_Play_Sound")) {
                        String sound = Objects.requireNonNull(ChatBubbles.this.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
                        float volume = (float) ChatBubbles.this.getConfig().getDouble("ChatBubble_Sound_Volume");
                        if (!sound.equals("")) {
                            try {
                                p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
                            } catch (Exception e) {
                                ChatBubbles.this.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
                                ChatBubbles.this.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
                            }
                        }
                    }
                }
            }
        }).runTask(this);
    }
    public int formatHologramLines(Player p, Hologram hologram, String message) {
        List <String> lineList = new ArrayList <> ();
        for (String formatLine: getConfig().getStringList("ChatBubble_Message_Format")) {
            if (formatLine.contains("%player_name%")) {
                formatLine = formatLine.replace("%player_name%", p.getName());
            }
            boolean addedToLine = false;
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                formatLine = placeholderShit.formatString(p, formatLine);
            }
            if (formatLine.contains("%chatbubble_message%")) {
                addedToLine = true;
                formatLine = formatLine.replace("%chatbubble_message%", message);
                byte b1;
                int j;
                String[] arrayOfString1;
                for (j = (arrayOfString1 = formatLine.split(" ")).length, b1 = 0; b1 < j;) {
                    String s = arrayOfString1[b1];
                    if (s.length() > this.length) {
                        String insert = "-\n";
                        int period = this.length - 1;
                        StringBuilder builder = new StringBuilder(
                            s.length() + insert.length() * s.length() / this.length + 1);
                        int index = 0;
                        String prefix = "";
                        while (index < s.length()) {
                            builder.append(prefix);
                            prefix = insert;

                            builder.append(s, index, Math.min(index + period, s.length()));
                            index += period;
                        }
                        String replacement = builder.toString();
                        formatLine = formatLine.replace(s, replacement);
                        message = message.replace(s, replacement);
                    }
                    b1++;
                }
                StringBuilder sb = new StringBuilder(formatLine.replace(message, "") + message);
                int i = 0;
                while (i + this.length < sb.length() && (i = sb.lastIndexOf(" ", i + this.length)) != -1)
                    sb.replace(i, i + 1, "\n");
                byte b2;
                int k;
                String[] arrayOfString2;
                for (k = (arrayOfString2 = sb.toString().split("\\n")).length, b2 = 0; b2 < k;) {
                    String s = arrayOfString2[b2];
                    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        s = makeColors(s);
                        if (getConfig().getBoolean("ChatBubble_Strip_Formatting"))
                            s = ChatColor.stripColor(s);
                        s = placeholderShit.formatString(p, this.prefix + s + this.suffix);
                        s = makeColors(s);
                        lineList.add(s);
                    } else {
                        s = makeColors(s);
                        if (getConfig().getBoolean("ChatBubble_Strip_Formatting"))
                            s = ChatColor.stripColor(s);
                        s = makeColors(this.prefix + s + this.suffix);
                        lineList.add(s);
                    }
                    b2++;
                }
            }
            if (!addedToLine) {
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    formatLine = makeColors(formatLine);
                    if (getConfig().getBoolean("ChatBubble_Strip_Formatting")) {
                        formatLine = ChatColor.stripColor(formatLine);
                    }
                    formatLine = placeholderShit.formatString(p, this.prefix + formatLine + this.suffix);
                    formatLine = makeColors(formatLine);
                    lineList.add(formatLine);
                    continue;
                }
                formatLine = makeColors(formatLine);
                if (getConfig().getBoolean("ChatBubble_Strip_Formatting")) {
                    formatLine = ChatColor.stripColor(formatLine);
                }

                formatLine = makeColors(this.prefix + formatLine + this.suffix);
                lineList.add(formatLine);
            }
        }
        for (String s: lineList) {
            hologram.appendTextLine(s);
        }
        return lineList.size();
    }
    public int formatHologramLines(LivingEntity p, Hologram hologram, String message) {
        List < String > lineList = new ArrayList < > ();
        for (String formatLine: getConfig().getStringList("ChatBubble_Message_Format")) {
            if (formatLine.contains("%player_name%"))
                formatLine = formatLine.replace("%player_name%", Objects.requireNonNull(p.getCustomName()));
            boolean addedToLine = false;
            if (formatLine.contains("%chatbubble_message%")) {
                addedToLine = true;
                formatLine = formatLine.replace("%chatbubble_message%", message);
                byte b1;
                int j;
                String[] arrayOfString1;
                for (j = (arrayOfString1 = formatLine.split(" ")).length, b1 = 0; b1 < j;) {
                    String s = arrayOfString1[b1];
                    if (s.length() > this.length) {
                        String insert = "-\n";
                        int period = this.length - 1;
                        StringBuilder builder = new StringBuilder(
                            s.length() + insert.length() * s.length() / this.length + 1);
                        int index = 0;
                        String prefix = "";
                        while (index < s.length()) {
                            builder.append(prefix);
                            prefix = insert;
                            builder.append(s, index, Math.min(index + period, s.length()));
                            index += period;
                        }
                        String replacement = builder.toString();
                        formatLine = formatLine.replace(s, replacement);
                        message = message.replace(s, replacement);
                    }
                    b1++;
                }
                StringBuilder sb = new StringBuilder(formatLine.replace(message, "") + message);
                int i = 0;
                while (i + this.length < sb.length() && (i = sb.lastIndexOf(" ", i + this.length)) != -1)
                    sb.replace(i, i + 1, "\n");
                byte b2;
                int k;
                String[] arrayOfString2;
                for (k = (arrayOfString2 = sb.toString().split("\\n")).length, b2 = 0; b2 < k;) {
                    String s = arrayOfString2[b2];
                    s = makeColors(s);
                    s = makeColors(this.prefix + s + this.suffix);
                    lineList.add(s);
                    b2++;
                }
            }
            if (!addedToLine) {
                formatLine = makeColors(formatLine);
                formatLine = makeColors(this.prefix + formatLine + this.suffix);
                lineList.add(formatLine);
            }
        }
        for (String s: lineList)
            hologram.appendTextLine(s);
        return lineList.size();
    }
    public static final Pattern HEXPAT = Pattern.compile("&#[a-fA-F0-9]{6}");
    public static String makeColors(String s) {
        s = s.replace("&0", ChatColor.BLACK.toString());
        s = s.replace("&1", ChatColor.DARK_BLUE.toString());
        s = s.replace("&2", ChatColor.DARK_GREEN.toString());
        s = s.replace("&3", ChatColor.DARK_AQUA.toString());
        s = s.replace("&4", ChatColor.DARK_RED.toString());
        s = s.replace("&5", ChatColor.DARK_PURPLE.toString());
        s = s.replace("&6", ChatColor.GOLD.toString());
        s = s.replace("&7", ChatColor.GRAY.toString());
        s = s.replace("&8", ChatColor.DARK_GRAY.toString());
        s = s.replace("&9", ChatColor.BLUE.toString());
        s = s.replace("&a", ChatColor.GREEN.toString());
        s = s.replace("&b", ChatColor.AQUA.toString());
        s = s.replace("&c", ChatColor.RED.toString());
        s = s.replace("&d", ChatColor.LIGHT_PURPLE.toString());
        s = s.replace("&e", ChatColor.YELLOW.toString());
        s = s.replace("&f", ChatColor.WHITE.toString());
        s = s.replace("&k", ChatColor.MAGIC.toString());
        s = s.replace("&l", ChatColor.BOLD.toString());
        s = s.replace("&m", ChatColor.STRIKETHROUGH.toString());
        s = s.replace("&n", ChatColor.UNDERLINE.toString());
        s = s.replace("&o", ChatColor.ITALIC.toString());
        s = s.replace("&r", ChatColor.RESET.toString());
        Matcher match = HEXPAT.matcher(s);
        while (match.find()) {
            String color = s.substring(match.start(), match.end());
            s = s.replace(color, ChatColor.of(color.replace("&", "")).toString());
        }
        return s;
    }
}