package me.VAL.grapplingHook;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.bukkit.enchantments.Enchantment.POWER;

public class Hooks implements Listener, CommandExecutor {


    private final int Power = 3;
    private final ArrayList<UUID> grappling = new ArrayList<UUID>();

    private final int COOLDOWN = 3;
    private final Cache<UUID, Long> onCooldown = CacheBuilder.newBuilder().expireAfterWrite(COOLDOWN, TimeUnit.SECONDS).build();



    @EventHandler
    public void onGrappling(PlayerFishEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if(event.getState() == PlayerFishEvent.State.FISHING){
            if(event.getHand() == null)return;
            ItemStack inHand = event.getPlayer().getInventory().getItem(event.getHand());
            if(inHand  == null || !inHand.hasItemMeta() || !inHand.getItemMeta().hasDisplayName() || !inHand.getItemMeta().getDisplayName().equals("GrapplingHook")) return;

            long timeUntilUse = onCooldown.getIfPresent(uuid) == null ? 0 : onCooldown.getIfPresent(uuid) - System.currentTimeMillis();
            if (timeUntilUse > 0) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You cannot use this for another " + Math.round(timeUntilUse / 1000.0) + " seconds");
                return;
            }
            grappling.add(uuid);
        }else if (event.getState() == PlayerFishEvent.State.REEL_IN && grappling.contains(uuid)) {

            Vector additionalVelocity = event.getHook().getLocation().subtract(event.getPlayer().getLocation()).toVector().normalize().multiply(Power);
            event.getPlayer().setVelocity(event.getPlayer().getVelocity().add(additionalVelocity));
            grappling.remove(uuid);

            onCooldown.put(uuid, System.currentTimeMillis() + (COOLDOWN * 1000));

        } else if (grappling.contains(uuid)) {
            grappling.remove(uuid);
        }
    }

    public static ItemStack getItem() {
        ItemStack item = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = item.getItemMeta();


        meta.setDisplayName("GrapplingHook");
        meta.addEnchant(Enchantment.UNBREAKING, 10000000, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setLore(Arrays.asList("This is a grappling hook"));

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("grapplinghook.give")) {
            sender.sendMessage("You do not have permission for this");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player");
            return true;
        }


        ((Player) sender).getInventory().addItem(getItem());
        sender.sendMessage("Recived a Grappling Hook!");
        return true;
    }
}



