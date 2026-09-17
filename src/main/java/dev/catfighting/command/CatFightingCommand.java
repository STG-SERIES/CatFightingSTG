package dev.catfighting.command;

import dev.catfighting.CatFightingPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.List;
import java.util.Locale;

public final class CatFightingCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "flatten", "unflatten");

    private final CatFightingPlugin plugin;

    public CatFightingCommand(CatFightingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + label + " <reload|flatten|unflatten>", NamedTextColor.YELLOW));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                plugin.reloadPluginConfig();
                sender.sendMessage(Component.text("Cat Fighting config reloaded.", NamedTextColor.GREEN));
            }
            case "flatten" -> flattenOrUnflatten(sender, true);
            case "unflatten" -> flattenOrUnflatten(sender, false);
            default -> sender.sendMessage(Component.text("Unknown subcommand. Use reload, flatten, or unflatten.", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return SUBCOMMANDS.stream().filter(name -> name.startsWith(prefix)).toList();
        }
        return List.of();
    }

    private void flattenOrUnflatten(CommandSender sender, boolean flatten) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can target a cat.", NamedTextColor.RED));
            return;
        }
        Cat cat = targetedCat(player);
        if (cat == null) {
            player.sendMessage(Component.text("Look at a cat within 6 blocks.", NamedTextColor.RED));
            return;
        }
        boolean changed = flatten ? plugin.flatten().flatten(cat) : plugin.flatten().unflatten(cat);
        if (changed) {
            player.sendMessage(Component.text(flatten ? "Flattened that cat." : "Restored that cat.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text(flatten ? "That cat is already flattened." : "That cat is not flattened.", NamedTextColor.YELLOW));
        }
    }

    private static Cat targetedCat(Player player) {
        RayTraceResult hit = player.getWorld().rayTraceEntities(
            player.getEyeLocation(),
            player.getEyeLocation().getDirection(),
            6.0,
            0.4,
            entity -> entity instanceof Cat && entity != player
        );
        if (hit != null && hit.getHitEntity() instanceof Cat cat) {
            return cat;
        }
        Cat nearest = null;
        double best = 36.0;
        for (Entity entity : player.getNearbyEntities(6.0, 6.0, 6.0)) {
            if (entity instanceof Cat cat) {
                double dist = cat.getLocation().distanceSquared(player.getLocation());
                if (dist < best) {
                    best = dist;
                    nearest = cat;
                }
            }
        }
        return nearest;
    }
}
