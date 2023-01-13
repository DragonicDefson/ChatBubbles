package me.thetealviper.chatbubbles;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.massivecraft.factions.entity.MPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
public class handleThree {
    public static void run(final ChatBubbles cb, final String message, final Player p) {
        final boolean sendOriginal = cb.getConfig().getBoolean("ChatBubble_Send_Original_Message");
        final boolean requirePerm = cb.getConfig().getBoolean("ConfigOne_Require_Permissions");
        final String usePerm = cb.getConfig().getString("ConfigOne_Use_Permission");
        (new BukkitRunnable() {
            public void run() {
                if (requirePerm) {
                    assert usePerm != null;
                    if (!p.hasPermission(usePerm)) return;
                }
                if (cb.existingHolograms.containsKey(p.getUniqueId()))
                    for (Hologram h: cb.existingHolograms.get(p.getUniqueId())) {
                        if (!h.isDeleted()) {
                            h.delete();
                        }
                    }
                MPlayer mPlayer = MPlayer.get(p);
                String faction = mPlayer.getFactionName();
                final Hologram hologram = HologramsAPI.createHologram(cb, p.getLocation().add(0.0D, cb.bubbleOffset, 0.0D));
                List < Hologram > hList = new ArrayList < > ();
                hList.add(hologram);
                cb.existingHolograms.put(p.getUniqueId(), hList);
                hologram.getVisibilityManager().setVisibleByDefault(false);
                for (Player oP: Bukkit.getOnlinePlayers()) {
                    if ((cb.seeOwnBubble || !oP.getName().equals(p.getName())) && oP.getWorld().getName().equals(p.getWorld().getName()) && oP.getLocation().distance(p.getLocation()) <= cb.distance && MPlayer.get(oP).getFactionName().equals(faction) && oP.canSee(p)) {
                        hologram.getVisibilityManager().showTo(oP);
                    }
                }
                final int lines = cb.formatHologramLines(p, hologram, message);
                if (sendOriginal) {
                    p.chat(message);
                }
                (new BukkitRunnable() {
                    int ticksRun = 0;

                    public void run() {
                        this.ticksRun++;
                        if (!hologram.isDeleted())
                            hologram.teleport(p.getLocation().add(0.0D, cb.bubbleOffset + 0.25D * lines, 0.0D));
                        if (this.ticksRun > cb.life) {
                            hologram.delete();
                            cancel();
                        }
                    }
                }).runTaskTimer(cb, 1L, 1L);

                if (cb.getConfig().getBoolean("ChatBubble_Play_Sound")) {
                    String sound = Objects.requireNonNull(cb.getConfig().getString("ChatBubble_Sound_Name")).toLowerCase();
                    float volume = (float) cb.getConfig().getDouble("ChatBubble_Sound_Volume");
                    if (!sound.equals(""))
                        try {
                            p.getWorld().playSound(p.getLocation(), sound, volume, 1.0F);
                        } catch (Exception e) {
                            cb.getServer().getConsoleSender().sendMessage("Something is wrong in your ChatBubble config.yml sound settings!");
                            cb.getServer().getConsoleSender().sendMessage("Please ensure that 'ChatBubble_Sound_Name' works in a '/playsound' command test.");
                        }
                }
            }
        }).runTask(cb);
    }
}