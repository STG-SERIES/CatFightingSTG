package dev.catfighting;

import dev.catfighting.command.CatFightingCommand;
import dev.catfighting.fight.CatFightGoal;
import dev.catfighting.fight.FightManager;
import dev.catfighting.flatten.FlattenManager;
import dev.catfighting.listeners.CatListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Cat;
import org.bukkit.plugin.java.JavaPlugin;

public final class CatFightingPlugin extends JavaPlugin {

    private PluginConfig pluginConfig;
    private FightManager fightManager;
    private FlattenManager flattenManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.pluginConfig = new PluginConfig(this);
        CatFightGoal.init(this);

        this.flattenManager = new FlattenManager(this);
        this.fightManager = new FightManager(this, flattenManager);

        Bukkit.getPluginManager().registerEvents(new CatListener(this), this);

        CatFightingCommand command = new CatFightingCommand(this);
        if (getCommand("catfighting") != null) {
            getCommand("catfighting").setExecutor(command);
            getCommand("catfighting").setTabCompleter(command);
        }

        attachToLoadedCats();
        getLogger().info("Cat Fighting " + getPluginMeta().getVersion() + " enabled (Paper port of Chesy's MIT mod).");
    }

    @Override
    public void onDisable() {
        if (fightManager != null) {
            fightManager.clear();
        }
    }

    public void reloadPluginConfig() {
        reloadConfig();
        pluginConfig.reload();
    }

    public PluginConfig config() {
        return pluginConfig;
    }

    public FightManager fights() {
        return fightManager;
    }

    public FlattenManager flatten() {
        return flattenManager;
    }

    public void attach(Cat cat) {
        flattenManager.applyIfFlattened(cat);
        fightManager.attach(cat);
    }

    private void attachToLoadedCats() {
        for (World world : Bukkit.getWorlds()) {
            for (Cat cat : world.getEntitiesByClass(Cat.class)) {
                attach(cat);
            }
        }
    }
}
