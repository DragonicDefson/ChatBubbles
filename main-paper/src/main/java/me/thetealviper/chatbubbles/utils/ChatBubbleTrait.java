package me.thetealviper.chatbubbles.utils;

import java.util.Random;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.thetealviper.chatbubbles.ChatBubbles;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ChatBubbleTrait extends Trait {
    ChatBubbles plugin;

    public ChatBubbleTrait() {
        super("chatbubble");
        this.plugin = JavaPlugin.getPlugin(ChatBubbles.class);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onNPCSpeech(NPCSpeechEvent event) {
        if (this.npc != event.getNPC()) {
            return;
        }
        if (event.getNPC() != null && event.getNPC().isSpawned()) {
            NPC talker = event.getNPC();
            if (talker.getEntity() instanceof LivingEntity p) {
                SpeechContext sp = event.getContext();
                String msg = sp.getMessage();
                createBubble(p, msg);
                if (this.plugin.chatBubbleOverridesNPCChat) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public void onAttach() {
        this.plugin.getServer().getLogger().info(this.npc.getName() + " has been assigned trait ChatBubble!");
    }

    public void createBubble(final LivingEntity p, String msg) {
        if (this.plugin.existingHolograms.containsKey(p.getUniqueId())) {
            for (Hologram h : this.plugin.existingHolograms.get(p.getUniqueId())) {
                if (!h.isDeleted()) {
                    h.delete();
                }
            }
        }
        final Hologram hologram = HologramsAPI.createHologram(this.plugin, p.getLocation().add(0.0D, this.plugin.bubbleOffset, 0.0D));
        List<Hologram> hList = new ArrayList<>();
        hList.add(hologram);
        this.plugin.existingHolograms.put(p.getUniqueId(), hList);
        hologram.getVisibilityManager().setVisibleByDefault(false);
        for (Player oP : Bukkit.getOnlinePlayers()) {
            if (oP.getWorld().getName().equals(p.getWorld().getName())) {
                oP.getLocation().distance(p.getLocation());
                hologram.getVisibilityManager().showTo(oP);
            }
            final int lines = this.plugin.formatHologramLines(p, hologram, msg);
            (new BukkitRunnable() {
                int ticksRun = 0;
                public void run() {
                    this.ticksRun++;
                    if (!hologram.isDeleted()) {
                        hologram.teleport(p.getLocation().add(0.0D, ChatBubbleTrait.this.plugin.bubbleOffset + 0.25D * lines, 0.0D));
                        if (this.ticksRun > ChatBubbleTrait.this.plugin.life) {
                            hologram.delete();
                            cancel();
                        }
                    }
                }
            }).runTaskTimer(ChatBubbleTrait.this.plugin, 1L, 1L);

            if (this.plugin.getConfig().getBoolean("ChatBubble_Play_Sound")) {
                String sound = Objects.requireNonNull(this.plugin.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
                float volume = (float) this.plugin.getConfig().getDouble("ChatBubble_Sound_Volume");
                if (!sound.equals("")) {
                    try {
                        p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
                    } catch (Exception e) {
                        Bukkit.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
                        Bukkit.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
                    }
                }
            }
        }
    }
}