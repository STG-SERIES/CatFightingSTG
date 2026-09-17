package dev.catfighting;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConfig {

    private final JavaPlugin plugin;

    private boolean fightsEnabled;
    private boolean skipSitting;
    private boolean tamedCatsCanFight;
    private boolean sameOwnerCanFight;
    private boolean kittensCanFight;
    private int checkIntervalTicks;
    private double startChance;
    private double searchRange;
    private double meleeRange;
    private int durationTicks;
    private int cooldownTicks;
    private int hissIntervalTicks;
    private int swipeIntervalTicks;
    private double fightSpeed;
    private double knockback;

    private boolean flattenEnabled;
    private boolean shovelFlatten;
    private boolean minecartFlatten;
    private double minecartMinSpeed;
    private double flattenScale;
    private boolean sneakRightClickUnflatten;
    private boolean shovelToggles;

    public PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();

        fightsEnabled = config.getBoolean("fights.enabled", true);
        skipSitting = config.getBoolean("fights.skip-sitting", true);
        tamedCatsCanFight = config.getBoolean("fights.tamed-cats-can-fight", true);
        sameOwnerCanFight = config.getBoolean("fights.same-owner-can-fight", true);
        kittensCanFight = config.getBoolean("fights.kittens-can-fight", false);
        checkIntervalTicks = Math.max(1, config.getInt("fights.check-interval-ticks", 40));
        startChance = clamp01(config.getDouble("fights.start-chance", 0.08));
        searchRange = Math.max(1.0, config.getDouble("fights.search-range", 8.0));
        meleeRange = Math.max(0.4, config.getDouble("fights.melee-range", 1.45));
        durationTicks = Math.max(20, config.getInt("fights.duration-ticks", 160));
        cooldownTicks = Math.max(0, config.getInt("fights.cooldown-ticks", 400));
        hissIntervalTicks = Math.max(5, config.getInt("fights.hiss-interval-ticks", 22));
        swipeIntervalTicks = Math.max(5, config.getInt("fights.swipe-interval-ticks", 14));
        fightSpeed = Math.max(0.4, config.getDouble("fights.fight-speed", 1.7));
        knockback = Math.max(0.0, config.getDouble("fights.knockback", 0.08));

        flattenEnabled = config.getBoolean("flatten.enabled", true);
        shovelFlatten = config.getBoolean("flatten.shovel", true);
        minecartFlatten = config.getBoolean("flatten.minecart", true);
        minecartMinSpeed = Math.max(0.05, config.getDouble("flatten.minecart-min-speed", 0.28));
        flattenScale = Math.max(0.05, Math.min(1.0, config.getDouble("flatten.scale", 0.22)));
        sneakRightClickUnflatten = config.getBoolean("flatten.sneak-right-click-unflatten", true);
        shovelToggles = config.getBoolean("flatten.shovel-toggles", true);
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public NamespacedKey key(String name) {
        return new NamespacedKey(plugin, name);
    }

    public boolean fightsEnabled() {
        return fightsEnabled;
    }

    public boolean skipSitting() {
        return skipSitting;
    }

    public boolean tamedCatsCanFight() {
        return tamedCatsCanFight;
    }

    public boolean sameOwnerCanFight() {
        return sameOwnerCanFight;
    }

    public boolean kittensCanFight() {
        return kittensCanFight;
    }

    public int checkIntervalTicks() {
        return checkIntervalTicks;
    }

    public double startChance() {
        return startChance;
    }

    public double searchRange() {
        return searchRange;
    }

    public double meleeRange() {
        return meleeRange;
    }

    public int durationTicks() {
        return durationTicks;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public int hissIntervalTicks() {
        return hissIntervalTicks;
    }

    public int swipeIntervalTicks() {
        return swipeIntervalTicks;
    }

    public double fightSpeed() {
        return fightSpeed;
    }

    public double knockback() {
        return knockback;
    }

    public boolean flattenEnabled() {
        return flattenEnabled;
    }

    public boolean shovelFlatten() {
        return shovelFlatten;
    }

    public boolean minecartFlatten() {
        return minecartFlatten;
    }

    public double minecartMinSpeed() {
        return minecartMinSpeed;
    }

    public double flattenScale() {
        return flattenScale;
    }

    public boolean sneakRightClickUnflatten() {
        return sneakRightClickUnflatten;
    }

    public boolean shovelToggles() {
        return shovelToggles;
    }
}
