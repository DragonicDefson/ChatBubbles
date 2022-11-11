/*     */ package me.thetealviper.chatbubbles;
/*     */ 
/*     */ import com.gmail.filoghost.holographicdisplays.api.Hologram;
/*     */ import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
/*     */ import java.util.*;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
          import me.thetealviper.chatbubbles.utils.ChatBubbleTrait;
/*     */ import me.thetealviper.chatbubbles.listeners.*;
          import me.thetealviper.chatbubbles.utils.EnableShit;
          import me.thetealviper.chatbubbles.utils.PluginFile;
          import net.citizensnpcs.api.CitizensAPI;
/*     */ import net.citizensnpcs.api.trait.TraitInfo;
/*     */ import net.md_5.bungee.api.ChatColor;
/*     */ import org.bukkit.Bukkit;
/*     */ import org.bukkit.command.Command;
/*     */ import org.bukkit.command.CommandSender;
/*     */ import org.bukkit.entity.LivingEntity;
/*     */ import org.bukkit.entity.Player;
/*     */ import org.bukkit.event.EventHandler;
/*     */ import org.bukkit.event.EventPriority;
/*     */ import org.bukkit.event.Listener;
/*     */ import org.bukkit.event.player.PlayerJoinEvent;
/*     */ import org.bukkit.event.player.PlayerQuitEvent;
/*     */ import org.bukkit.plugin.Plugin;
/*     */ import org.bukkit.plugin.java.JavaPlugin;
/*     */ import org.bukkit.scheduler.BukkitRunnable;
          import org.jetbrains.annotations.NotNull;
/*     */
/*     */ public class ChatBubbles extends JavaPlugin implements Listener {
/*  39 */   public int life = -1; public int distance = -1; public int length = -1;
/*  40 */   public String prefix = ""; public String suffix = "";
/*     */   public boolean seeOwnBubble = false;
/*     */   private boolean useTrait = true;
/*     */   public boolean chatBubbleOverridesNPCChat;
/*  44 */   public double bubbleOffset = 2.5D;
/*  45 */   public EventPriority chatPriority = EventPriority.NORMAL;
/*     */   
/*     */   private PluginFile togglePF;
/*     */   public ChatBubbles plugin;
/*  49 */   public Map<UUID, List<Hologram>> existingHolograms = new HashMap<>();
/*     */   public void onEnable() {
/*  52 */     EnableShit.handleOnEnable(this, this, "49387");
/*  53 */     this.life = getConfig().getInt("ChatBubble_Life");
/*  54 */     this.distance = getConfig().getInt("ChatBubble_Viewing_Distance");
/*  55 */     this.length = getConfig().getInt("ChatBubble_Maximum_Line_Length");
/*  56 */     this.prefix = getConfig().getString("ChatBubble_Message_Prefix");
/*  57 */     if (this.prefix == null)
/*  58 */       this.prefix = ""; 
/*  59 */     this.suffix = getConfig().getString("ChatBubble_Message_Suffix");
/*  60 */     if (this.suffix == null)
/*  61 */       this.suffix = ""; 
/*  62 */     this.seeOwnBubble = getConfig().getBoolean("ChatBubble_See_Own_Bubbles");
/*  63 */     this.bubbleOffset = getConfig().getDouble("ChatBubble_Height_Offset");
/*  64 */     this.useTrait = getConfig().getBoolean("Use_ChatBubble_Trait_Citizens");
/*  65 */     this.chatBubbleOverridesNPCChat = getConfig().getBoolean("ChatBubble_Overrides_NPC_Chat");
/*  66 */     this.chatPriority = EventPriority.valueOf(Objects.requireNonNull(getConfig().getString("ChatBubble_Chat_Priority")).toUpperCase());
/*  67 */     this.togglePF = new PluginFile(this, "toggleData");
/*  68 */     if (getServer().getPluginManager().getPlugin("Citizens") != null && 
/*  69 */       Objects.requireNonNull(getServer().getPluginManager().getPlugin("Citizens")).isEnabled() && this.useTrait) {
/*  70 */       Bukkit.getServer().getConsoleSender().sendMessage("Citizens found and trait chatbubble enabled");
/*  71 */       CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ChatBubbleTrait.class).withName("chatbubble"));
/*     */     }
/*  74 */     this.plugin = this;
/*  75 */     setChatPriority(this.chatPriority);
/*     */   }
/*     */   public void onDisable() {
/*  80 */       getLogger().info(makeColors("ChatBubbles from TheTealViper shutting down."));
/*     */   }
/*     */   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
/*  84 */     if (sender instanceof Player) {
/*  85 */       Player p = (Player)sender;
/*  86 */       if (label.equalsIgnoreCase("chatbubble") || label.equalsIgnoreCase("cb")) {
/*  87 */         if (args.length == 0) {
/*  88 */           return false;
/*     */         }
/*  90 */         String message = "";
/*  91 */         for (int i = 0; i < args.length; i++) {
/*  92 */           if (i == args.length - 1)
/*  93 */           { message = String.valueOf(message) + args[i]; }
/*     */           else
/*  95 */           { message = String.valueOf(message) + args[i] + " "; } 
/*  96 */         }  handleOne(message, p);
/*     */       } 
/*     */       
/*  99 */       if ((label.equalsIgnoreCase("chatbubblereload") || label.equalsIgnoreCase("cbreload")) && p.hasPermission("chatbubbles.reload")) {
/* 100 */         reloadConfig();
/* 101 */         p.sendMessage("Reloaded Successfully");
/*     */       } 
/* 103 */       if ((label.equalsIgnoreCase("chatbubbletoggle") || label.equalsIgnoreCase("cbtoggle") || label.equalsIgnoreCase("cbt")) && p.hasPermission("chatbubbles.toggle")) {
/* 104 */         boolean currentState = this.togglePF.getBoolean(p.getUniqueId().toString());
/* 105 */         if (currentState) {
/* 106 */           p.sendMessage("ChatBubbles toggled off!");
/*     */         } else {
/* 108 */           p.sendMessage("ChatBubbles toggled on!");
/* 109 */         }  this.togglePF.set(p.getUniqueId().toString(), !currentState);
/* 110 */         this.togglePF.save();
/*     */       } 
/*     */     } 
/* 113 */     return false;
/*     */   }
/*     */   private void setChatPriority(EventPriority priority) {
/* 117 */     switch (priority) {
/*     */       case LOWEST:
/* 119 */         getServer().getPluginManager().registerEvents(new ChatListenerLowest(), (Plugin)this);
/*     */       case LOW:
/* 121 */         getServer().getPluginManager().registerEvents(new ChatListenerLow(), (Plugin)this);
/*     */       case NORMAL:
/* 123 */         getServer().getPluginManager().registerEvents(new ChatListenerNormal(), (Plugin)this);
/*     */       case HIGH:
/* 125 */         getServer().getPluginManager().registerEvents(new ChatListenerHigh(), (Plugin)this);
/*     */       case HIGHEST:
/* 127 */         getServer().getPluginManager().registerEvents(new ChatListenerHighest(), (Plugin)this);
/*     */       case MONITOR:
/* 129 */         getServer().getPluginManager().registerEvents(new ChatListenerMonitor(), (Plugin)this); break;
/*     */     } 
/* 131 */     getServer().getPluginManager().registerEvents(new ChatListenerNormal(), (Plugin)this);
/*     */   }
/*     */   
/*     */   @EventHandler
/*     */   public void onJoin(PlayerJoinEvent e) {
/* 137 */     Player p = e.getPlayer();
/* 138 */     if (!this.togglePF.contains(p.getUniqueId().toString())) {
/* 139 */       this.togglePF.set(p.getUniqueId().toString(), Boolean.TRUE);
/* 140 */       this.togglePF.save();
/*     */     } 
/*     */   }
/*     */   
/*     */   @EventHandler
/*     */   public void onQuit(PlayerQuitEvent e) {
/* 146 */     this.existingHolograms.remove(e.getPlayer().getUniqueId());
/*     */   }
/*     */   public void handleZero(final String message, final Player p) {
/* 150 */     final boolean requirePerm = getConfig().getBoolean("ConfigZero_Require_Permissions");
/* 151 */     final String usePerm = getConfig().getString("ConfigZero_Use_Permission");
/* 152 */     final String seePerm = getConfig().getString("ConfigZero_See_Permission");
/* 153 */     (new BukkitRunnable() {
/*     */         public void run() {
/* 156 */           if (requirePerm)
/*     */           {
/*     */             assert usePerm != null;
/*     */               if (!p.hasPermission(usePerm)) return;
/*     */           }
/* 158 */           if (!ChatBubbles.this.togglePF.getBoolean(p.getUniqueId().toString()))
/*     */             return; 
/* 160 */           if (ChatBubbles.this.existingHolograms.containsKey(p.getUniqueId()))
/* 161 */             for (Hologram h : ChatBubbles.this.existingHolograms.get(p.getUniqueId())) {
/* 162 */               if (!h.isDeleted()) {
/* 163 */                 h.delete();
/*     */               }
/*     */             }  
/* 166 */           final Hologram hologram = HologramsAPI.createHologram((Plugin)ChatBubbles.this.plugin, p.getLocation().add(0.0D, ChatBubbles.this.bubbleOffset, 0.0D));
/* 167 */           List<Hologram> hList = new ArrayList<>();
/* 168 */           hList.add(hologram);
/* 169 */           ChatBubbles.this.existingHolograms.put(p.getUniqueId(), hList);
/* 170 */           hologram.getVisibilityManager().setVisibleByDefault(false);
/* 171 */           for (Player oP : Bukkit.getOnlinePlayers()) {
                    if ((ChatBubbles.this.seeOwnBubble || !oP.getName().equals(p.getName())) && oP.getWorld().getName().equals(p.getWorld().getName()) && oP.getLocation().distance(p.getLocation()) <= ChatBubbles.this.distance && !requirePerm || oP.hasPermission(Objects.requireNonNull(seePerm)) && oP.canSee(p))
/* 177 */               hologram.getVisibilityManager().showTo(oP); 
/*     */           } 
/* 179 */           final int lines = ChatBubbles.this.formatHologramLines(p, hologram, message);
/*     */           
/* 181 */           (new BukkitRunnable() {
/* 182 */               int ticksRun = 0;
/*     */               
/*     */               public void run() {
/* 185 */                 this.ticksRun++;
/* 186 */                 if (!hologram.isDeleted())
/* 187 */                   hologram.teleport(p.getLocation().add(0.0D, (new ChatBubbles()).bubbleOffset + 0.25D * lines, 0.0D));
/* 188 */                 if (this.ticksRun > (new ChatBubbles()).life) {
/* 189 */                   hologram.delete();
/* 190 */                   cancel();
/*     */                 }  }
/* 192 */             }).runTaskTimer((Plugin)ChatBubbles.this.plugin, 1L, 1L);
/*     */           
/* 194 */           if (ChatBubbles.this.getConfig().getBoolean("ChatBubble_Play_Sound")) {
/* 195 */             String sound = Objects.requireNonNull(ChatBubbles.this.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
/* 196 */             float volume = (float)ChatBubbles.this.getConfig().getDouble("ChatBubble_Sound_Volume");
/* 197 */             if (!sound.equals(""))
/*     */               try {
/* 199 */                 p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
/* 200 */               } catch (Exception e) {
/* 201 */                 ChatBubbles.this.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
/* 202 */                 ChatBubbles.this.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
/*     */               }  
/*     */           } 
/*     */         }
/* 206 */       }).runTask((Plugin)this);
/*     */   }
/*     */ 
/*     */   
/*     */   public void handleOne(final String message, final Player p) {
/* 211 */     final boolean sendOriginal = getConfig().getBoolean("ChatBubble_Send_Original_Message");
/* 212 */     final boolean requirePerm = getConfig().getBoolean("ConfigOne_Require_Permissions");
/* 213 */     final String usePerm = getConfig().getString("ConfigOne_Use_Permission");
/* 214 */     final String seePerm = getConfig().getString("ConfigOne_See_Permission");
/* 215 */     (new BukkitRunnable() {
/*     */         public void run() {
/* 218 */           if (requirePerm) {
                        assert usePerm != null;
                        if (!p.hasPermission(usePerm)) return;
                    }
/* 220 */           if (!ChatBubbles.this.togglePF.getBoolean(p.getUniqueId().toString()))
/*     */             return; 
/* 222 */           if (ChatBubbles.this.existingHolograms.containsKey(p.getUniqueId()))
/* 223 */             for (Hologram h : ChatBubbles.this.existingHolograms.get(p.getUniqueId())) {
/* 224 */               if (!h.isDeleted()) {
/* 225 */                 h.delete();
/*     */               }
/*     */             }  
/* 228 */           final Hologram hologram = HologramsAPI.createHologram((Plugin)ChatBubbles.this.plugin, p.getLocation().add(0.0D, ChatBubbles.this.bubbleOffset, 0.0D));
/* 229 */           List<Hologram> hList = new ArrayList<>();
/* 230 */           hList.add(hologram);
/* 231 */           ChatBubbles.this.existingHolograms.put(p.getUniqueId(), hList);
/* 232 */           hologram.getVisibilityManager().setVisibleByDefault(false);
/* 233 */           for (Player oP : Bukkit.getOnlinePlayers()) {
                    if ((ChatBubbles.this.seeOwnBubble || !oP.getName().equals(p.getName())) && oP.getWorld().getName().equals(p.getWorld().getName()) && oP.getLocation().distance(p.getLocation()) <= ChatBubbles.this.distance && !requirePerm || oP.hasPermission(Objects.requireNonNull(seePerm)) && oP.canSee(p))
/* 239 */               hologram.getVisibilityManager().showTo(oP); 
/*     */           } 
/* 241 */           final int lines = ChatBubbles.this.formatHologramLines(p, hologram, message);
/* 242 */           if (sendOriginal) {
/* 243 */             p.chat(message);
/*     */           }
/* 245 */           (new BukkitRunnable() {
/* 246 */               int ticksRun = 0;
/*     */               
/*     */               public void run() {
/* 249 */                 this.ticksRun++;
/* 250 */                 if (!hologram.isDeleted())
/* 251 */                   hologram.teleport(p.getLocation().add(0.0D, ((new ChatBubbles())).bubbleOffset + 0.25D * lines, 0.0D));
/* 252 */                 if (this.ticksRun > (new ChatBubbles()).life) {
/* 253 */                   hologram.delete();
/* 254 */                   cancel();
/*     */                 }  }
/* 256 */             }).runTaskTimer((Plugin)ChatBubbles.this.plugin, 1L, 1L);
/*     */           
/* 258 */           if (ChatBubbles.this.getConfig().getBoolean("ChatBubble_Play_Sound")) {
/* 259 */             String sound = Objects.requireNonNull(ChatBubbles.this.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
/* 260 */             float volume = (float)ChatBubbles.this.getConfig().getDouble("ChatBubble_Sound_Volume");
/* 261 */             if (!sound.equals(""))
/*     */               try {
/* 263 */                 p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
/* 264 */               } catch (Exception e) {
/* 265 */                 ChatBubbles.this.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
/* 266 */                 ChatBubbles.this.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
/*     */               }  
/*     */           } 
/*     */         }
/* 270 */       }).runTask((Plugin)this);
/*     */   }
/*     */   public void handleTwo(final String message, final Player p) {
/* 275 */     final boolean sendOriginal = getConfig().getBoolean("ChatBubble_Send_Original_Message");
/* 276 */     final boolean requirePerm = getConfig().getBoolean("ConfigOne_Require_Permissions");
/* 277 */     final String usePerm = getConfig().getString("ConfigOne_Use_Permission");
/* 278 */     (new BukkitRunnable() {
/*     */         public void run() {
/* 281 */           if (requirePerm)
/*     */           {
                      assert usePerm != null;
                      if (!p.hasPermission(usePerm)) return;
                    }
/* 283 */           if (!ChatBubbles.this.togglePF.getBoolean(p.getUniqueId().toString()))
/*     */             return; 
/* 285 */           if (ChatBubbles.this.existingHolograms.containsKey(p.getUniqueId()))
/* 286 */             for (Hologram h : ChatBubbles.this.existingHolograms.get(p.getUniqueId())) {
/* 287 */               if (!h.isDeleted()) {
/* 288 */                 h.delete();
/*     */               }
/*     */             }
/* 291 */           String permGroup = null;
/* 292 */           for (String testPerm : ChatBubbles.this.getConfig().getStringList("ConfigTwo_Permision_Groups")) {
/* 293 */             if (p.hasPermission(testPerm)) {
/* 294 */               permGroup = testPerm;
/*     */               break;
/*     */             }
/*     */           } 
/* 298 */
                    assert permGroup != null;
                    if (permGroup.equals(""))
/*     */             return; 
/* 300 */           final Hologram hologram = HologramsAPI.createHologram((Plugin)ChatBubbles.this.plugin, p.getLocation().add(0.0D, ChatBubbles.this.bubbleOffset, 0.0D));
/* 301 */           List<Hologram> hList = new ArrayList<>();
/* 302 */           hList.add(hologram);
/* 303 */           ChatBubbles.this.existingHolograms.put(p.getUniqueId(), hList);
/* 304 */           hologram.getVisibilityManager().setVisibleByDefault(false);
/* 305 */           for (Player oP : Bukkit.getOnlinePlayers()) {
                    if ((ChatBubbles.this.seeOwnBubble || !oP.getName().equals(p.getName())) && oP.getWorld().getName().equals(p.getWorld().getName()) && oP.getLocation().distance(p.getLocation()) <= ChatBubbles.this.distance && oP.hasPermission(permGroup) && oP.canSee(p))
/* 311 */               hologram.getVisibilityManager().showTo(oP);
/*     */           } 
/* 313 */           final int lines = ChatBubbles.this.formatHologramLines(p, hologram, message);
/* 314 */           if (sendOriginal) {
/* 315 */             p.chat(message);
/*     */           }
/* 317 */           (new BukkitRunnable() {
/* 318 */               int ticksRun = 0;
/*     */               public void run() {
/* 321 */                 this.ticksRun++;
/* 322 */                 if (!hologram.isDeleted())
/* 323 */                   hologram.teleport(p.getLocation().add(0.0D, (new ChatBubbles()).bubbleOffset + 0.25D * lines, 0.0D));
/* 324 */                 if (this.ticksRun > (new ChatBubbles()).life) {
/* 325 */                   hologram.delete();
/* 326 */                   cancel();
/*     */                 }  }
/* 328 */             }).runTaskTimer(ChatBubbles.this.plugin, 1L, 1L);
/*     */           
/* 330 */           if (ChatBubbles.this.getConfig().getBoolean("ChatBubble_Play_Sound")) {
/* 331 */             String sound = Objects.requireNonNull(ChatBubbles.this.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
/* 332 */             float volume = (float)ChatBubbles.this.getConfig().getDouble("ChatBubble_Sound_Volume");
/* 333 */             if (!sound.equals(""))
/*     */               try {
/* 335 */                 p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
/* 336 */               } catch (Exception e) {
/* 337 */                 ChatBubbles.this.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
/* 338 */                 ChatBubbles.this.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
/*     */               }  
/*     */           } 
/*     */         }
/* 342 */       }).runTask(this);
/*     */   }
/*     */   public void handleFour(String message, final Player p) {
/* 347 */     final boolean requirePerm = getConfig().getBoolean("ConfigZero_Require_Permissions");
/* 348 */     final String usePerm = getConfig().getString("ConfigZero_Use_Permission");
/* 349 */     (new BukkitRunnable() {
/*     */         public void run() {
/* 352 */           if (requirePerm) {
                      assert usePerm != null;
                      if (!p.hasPermission(usePerm)) return;
                    }
/* 354 */           if (!ChatBubbles.this.togglePF.getBoolean(p.getUniqueId().toString())) {
/*     */             return;
/*     */           }
/* 357 */           if (ChatBubbles.this.getConfig().getBoolean("ChatBubble_Play_Sound")) {
/* 358 */             String sound = Objects.requireNonNull(ChatBubbles.this.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
/* 359 */             float volume = (float)ChatBubbles.this.getConfig().getDouble("ChatBubble_Sound_Volume");
/* 360 */             if (!sound.equals(""))
/*     */               try {
/* 362 */                 p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
/* 363 */               } catch (Exception e) {
/* 364 */                 ChatBubbles.this.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
/* 365 */                 ChatBubbles.this.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
/*     */               }  
/*     */           } 
/*     */         }
/* 369 */       }).runTask(this);
/*     */   }
/*     */   public int formatHologramLines(Player p, Hologram hologram, String message) {
/* 376 */     List<String> lineList = new ArrayList<>();
/* 377 */     for (String formatLine : getConfig().getStringList("ChatBubble_Message_Format")) {
/* 378 */       if (formatLine.contains("%player_name%"))
/* 379 */         formatLine = formatLine.replace("%player_name%", p.getName()); 
/* 380 */       boolean addedToLine = false;
/* 381 */       if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
/* 382 */         formatLine = placeholderShit.formatString(p, formatLine); 
/* 383 */       if (formatLine.contains("%chatbubble_message%")) {
/* 384 */         addedToLine = true;
/* 385 */         formatLine = formatLine.replace("%chatbubble_message%", message); byte b1; int j;
/*     */         String[] arrayOfString1;
/* 387 */         for (j = (arrayOfString1 = formatLine.split(" ")).length, b1 = 0; b1 < j; ) { String s = arrayOfString1[b1];
/* 388 */           if (s.length() > this.length) {
/* 389 */             String insert = "-\n";
/* 390 */             int period = this.length - 1;
/* 391 */             StringBuilder builder = new StringBuilder(
/* 392 */                 s.length() + insert.length() * s.length() / this.length + 1);
/* 394 */             int index = 0;
/* 395 */             String prefix = "";
/* 396 */             while (index < s.length()) {
/* 400 */               builder.append(prefix);
/* 401 */               prefix = insert;
/* 402 */               /* 403 */
                        builder.append(s, index, Math.min(index + period, s.length()));
/* 404 */               index += period;
/*     */             } 
/* 406 */             String replacement = builder.toString();
/* 407 */             formatLine = formatLine.replace(s, replacement);
/* 408 */             message = message.replace(s, replacement);
/*     */           }
/*     */           b1++; }
/* 412 */         StringBuilder sb = new StringBuilder(formatLine.replace(message, "") + message);
/* 413 */         int i = 0;
/* 414 */         while (i + this.length < sb.length() && (i = sb.lastIndexOf(" ", i + this.length)) != -1)
/* 415 */           sb.replace(i, i + 1, "\n");  byte b2; int k;
/*     */         String[] arrayOfString2;
/* 417 */         for (k = (arrayOfString2 = sb.toString().split("\\n")).length, b2 = 0; b2 < k; ) { String s = arrayOfString2[b2];
/* 418 */           if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
/* 419 */             s = makeColors(s);
/* 420 */             if (getConfig().getBoolean("ChatBubble_Strip_Formatting"))
/* 421 */               s = ChatColor.stripColor(s); 
/* 422 */             s = placeholderShit.formatString(p, String.valueOf(this.prefix) + s + this.suffix);
/* 423 */             s = makeColors(s);
/* 424 */             lineList.add(s);
/*     */           } else {
/* 426 */             s = makeColors(s);
/* 427 */             if (getConfig().getBoolean("ChatBubble_Strip_Formatting"))
/* 428 */               s = ChatColor.stripColor(s); 
/* 429 */             s = makeColors(String.valueOf(this.prefix) + s + this.suffix);
/* 430 */             lineList.add(s);
/*     */           }  b2++; }
/*     */       
/*     */       } 
/* 434 */       if (!addedToLine) {
/* 435 */         if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
/* 436 */           formatLine = makeColors(formatLine);
/* 437 */           if (getConfig().getBoolean("ChatBubble_Strip_Formatting"))
/* 438 */             formatLine = ChatColor.stripColor(formatLine); 
/* 439 */           formatLine = placeholderShit.formatString(p, String.valueOf(this.prefix) + formatLine + this.suffix);
/* 440 */           formatLine = makeColors(formatLine);
/* 441 */           lineList.add(formatLine); continue;
/*     */         } 
/* 443 */         formatLine = makeColors(formatLine);
/* 444 */         if (getConfig().getBoolean("ChatBubble_Strip_Formatting"))
/* 445 */           formatLine = ChatColor.stripColor(formatLine); 
/* 446 */         formatLine = makeColors(String.valueOf(this.prefix) + formatLine + this.suffix);
/* 447 */         lineList.add(formatLine);
/*     */       } 
/*     */     } 
/*     */     
/* 451 */     for (String s : lineList)
/* 452 */       hologram.appendTextLine(s); 
/* 453 */     return lineList.size();
/*     */   }
/*     */   public int formatHologramLines(LivingEntity p, Hologram hologram, String message) {
/* 457 */     List<String> lineList = new ArrayList<>();
/* 458 */     for (String formatLine : getConfig().getStringList("ChatBubble_Message_Format")) {
/* 459 */       if (formatLine.contains("%player_name%"))
/* 460 */         formatLine = formatLine.replace("%player_name%", Objects.requireNonNull(p.getCustomName()));
/* 461 */       boolean addedToLine = false;
/* 462 */       if (formatLine.contains("%chatbubble_message%")) {
/* 463 */         addedToLine = true;
/* 464 */         formatLine = formatLine.replace("%chatbubble_message%", message); byte b1; int j;
/*     */         String[] arrayOfString1;
/* 466 */         for (j = (arrayOfString1 = formatLine.split(" ")).length, b1 = 0; b1 < j; ) { String s = arrayOfString1[b1];
/* 467 */           if (s.length() > this.length) {
/* 468 */             String insert = "-\n";
/* 469 */             int period = this.length - 1;
/* 470 */             StringBuilder builder = new StringBuilder(
/* 471 */                 s.length() + insert.length() * s.length() / this.length + 1);
/* 473 */             int index = 0;
/* 474 */             String prefix = "";
/* 475 */             while (index < s.length()) {
/* 479 */               builder.append(prefix);
/* 480 */               prefix = insert;
/* 481 */               builder.append(s.substring(index, 
/* 482 */                     Math.min(index + period, s.length())));
/* 483 */               index += period;
/*     */             } 
/* 485 */             String replacement = builder.toString();
/* 486 */             formatLine = formatLine.replace(s, replacement);
/* 487 */             message = message.replace(s, replacement);
/*     */           } 
/*     */           b1++; }
/* 491 */         StringBuilder sb = new StringBuilder(String.valueOf(formatLine.replace(message, "")) + message);
/* 492 */         int i = 0;
/* 493 */         while (i + this.length < sb.length() && (i = sb.lastIndexOf(" ", i + this.length)) != -1)
/* 494 */           sb.replace(i, i + 1, "\n");  byte b2; int k;
/*     */         String[] arrayOfString2;
/* 496 */         for (k = (arrayOfString2 = sb.toString().split("\\n")).length, b2 = 0; b2 < k; ) { String s = arrayOfString2[b2];
/* 497 */           s = makeColors(s);
/* 498 */           s = makeColors(String.valueOf(this.prefix) + s + this.suffix);
/* 499 */           lineList.add(s); b2++; }
/*     */       } 
/* 502 */       if (!addedToLine) {
/* 503 */         formatLine = makeColors(formatLine);
/* 504 */         formatLine = makeColors(String.valueOf(this.prefix) + formatLine + this.suffix);
/* 505 */         lineList.add(formatLine);
/*     */       } 
/*     */     } 
/* 508 */     for (String s : lineList)
/* 509 */       hologram.appendTextLine(s); 
/* 510 */     return lineList.size();
/*     */   }
/* 513 */   public static final Pattern HEXPAT = Pattern.compile("&#[a-fA-F0-9]{6}");
/*     */   public static String makeColors(String s) {
/* 516 */     s = s.replace("&0", ChatColor.BLACK.toString());
/* 517 */     s = s.replace("&1", ChatColor.DARK_BLUE.toString());
/* 518 */     s = s.replace("&2", ChatColor.DARK_GREEN.toString());
/* 519 */     s = s.replace("&3", ChatColor.DARK_AQUA.toString());
/* 520 */     s = s.replace("&4", ChatColor.DARK_RED.toString());
/* 521 */     s = s.replace("&5", ChatColor.DARK_PURPLE.toString());
/* 522 */     s = s.replace("&6", ChatColor.GOLD.toString());
/* 523 */     s = s.replace("&7", ChatColor.GRAY.toString());
/* 524 */     s = s.replace("&8", ChatColor.DARK_GRAY.toString());
/* 525 */     s = s.replace("&9", ChatColor.BLUE.toString());
/* 526 */     s = s.replace("&a", ChatColor.GREEN.toString());
/* 527 */     s = s.replace("&b", ChatColor.AQUA.toString());
/* 528 */     s = s.replace("&c", ChatColor.RED.toString());
/* 529 */     s = s.replace("&d", ChatColor.LIGHT_PURPLE.toString());
/* 530 */     s = s.replace("&e", ChatColor.YELLOW.toString());
/* 531 */     s = s.replace("&f", ChatColor.WHITE.toString());
/* 532 */     s = s.replace("&k", ChatColor.MAGIC.toString());
/* 533 */     s = s.replace("&l", ChatColor.BOLD.toString());
/* 534 */     s = s.replace("&m", ChatColor.STRIKETHROUGH.toString());
/* 535 */     s = s.replace("&n", ChatColor.UNDERLINE.toString());
/* 536 */     s = s.replace("&o", ChatColor.ITALIC.toString());
/* 537 */     s = s.replace("&r", ChatColor.RESET.toString());
/*     */
/* 539 */     Matcher match = HEXPAT.matcher(s);
/* 540 */     while (match.find()) {
/* 541 */       String color = s.substring(match.start(), match.end());
/* 542 */       s = s.replace(color, ChatColor.of(color.replace("&", "")).toString());
/*     */     }
/* 545 */     return s;
/*     */   }
/*     */ }


/* Location:              C:\Users\Bradley Methorst\Desktop\chatbubbles.jar!\me\TheTealViper\chatbubbles\ChatBubbles.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */