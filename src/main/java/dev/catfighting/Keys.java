package dev.catfighting;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {

    public final NamespacedKey flattened;
    public final NamespacedKey originalScale;
    public final NamespacedKey fighting;

    public Keys(Plugin plugin) {
        this.flattened = new NamespacedKey(plugin, "flattened");
        this.originalScale = new NamespacedKey(plugin, "original_scale");
        this.fighting = new NamespacedKey(plugin, "fighting");
    }
}
