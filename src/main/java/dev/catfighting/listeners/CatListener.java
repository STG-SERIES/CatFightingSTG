package dev.catfighting.listeners;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import dev.catfighting.CatFightingPlugin;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class CatListener implements Listener {

    private final CatFightingPlugin plugin;

    public CatListener(CatFightingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAddToWorld(EntityAddToWorldEvent event) {
        if (event.getEntity() instanceof Cat cat) {
            plugin.attach(cat);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCatVsCatDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Cat victim && event.getDamager() instanceof Cat attacker) {
            event.setCancelled(true);
            if (plugin.fights().areFighting(attacker, victim)) {
                victim.playHurtAnimation(0.0f);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShovelHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Cat cat) || !(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!plugin.config().flattenEnabled() || !plugin.config().shovelFlatten()) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isShovel(item)) {
            return;
        }

        event.setCancelled(true);
        if (plugin.flatten().isFlattened(cat) && plugin.config().shovelToggles()) {
            plugin.flatten().unflatten(cat);
        } else if (!plugin.flatten().isFlattened(cat)) {
            plugin.flatten().flatten(cat);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMinecartSquash(VehicleEntityCollisionEvent event) {
        if (!plugin.config().flattenEnabled() || !plugin.config().minecartFlatten()) {
            return;
        }
        if (!(event.getVehicle() instanceof Minecart minecart) || !(event.getEntity() instanceof Cat cat)) {
            return;
        }
        if (plugin.flatten().isFlattened(cat) || minecart.getPassengers().contains(cat)) {
            return;
        }
        if (minecart.getVelocity().lengthSquared() < square(plugin.config().minecartMinSpeed())) {
            return;
        }
        plugin.flatten().flatten(cat);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneakUnflatten(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!plugin.config().flattenEnabled() || !plugin.config().sneakRightClickUnflatten()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof Cat cat) || !plugin.flatten().isFlattened(cat)) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.AIR) {
            return;
        }
        event.setCancelled(true);
        plugin.flatten().unflatten(cat);
    }

    private static boolean isShovel(ItemStack item) {
        return item != null && Tag.ITEMS_SHOVELS.isTagged(item.getType());
    }

    private static double square(double value) {
        return value * value;
    }
}
