package dev.catfighting.flatten;

import dev.catfighting.CatFightingPlugin;
import dev.catfighting.Keys;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Cat;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class FlattenManager {

    private static final double DEFAULT_SCALE = 1.0;

    private final CatFightingPlugin plugin;
    private final Keys keys;

    public FlattenManager(CatFightingPlugin plugin) {
        this.plugin = plugin;
        this.keys = new Keys(plugin);
    }

    public boolean isFlattened(Cat cat) {
        Byte value = cat.getPersistentDataContainer().get(keys.flattened, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    public void applyIfFlattened(Cat cat) {
        if (isFlattened(cat)) {
            applyVisuals(cat);
        }
    }

    public boolean flatten(Cat cat) {
        if (!plugin.config().flattenEnabled() || isFlattened(cat)) {
            return false;
        }
        if (plugin.fights() != null) {
            plugin.fights().endFight(cat);
        }

        PersistentDataContainer data = cat.getPersistentDataContainer();
        data.set(keys.flattened, PersistentDataType.BYTE, (byte) 1);
        data.set(keys.originalScale, PersistentDataType.DOUBLE, currentScale(cat));

        applyVisuals(cat);
        playFlattenEffects(cat);
        return true;
    }

    public boolean unflatten(Cat cat) {
        if (!isFlattened(cat)) {
            return false;
        }
        PersistentDataContainer data = cat.getPersistentDataContainer();
        double original = data.getOrDefault(keys.originalScale, PersistentDataType.DOUBLE, DEFAULT_SCALE);
        data.remove(keys.flattened);
        data.remove(keys.originalScale);

        setScale(cat, original);
        cat.setAware(true);
        cat.setLyingDown(false);
        cat.setGravity(true);

        cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_PURR, 0.8f, 1.2f);
        cat.getWorld().spawnParticle(Particle.CLOUD, cat.getLocation().add(0.0, 0.2, 0.0), 8, 0.25, 0.1, 0.25, 0.02);
        return true;
    }

    public boolean toggle(Cat cat) {
        if (isFlattened(cat)) {
            return unflatten(cat);
        }
        return flatten(cat);
    }

    private void applyVisuals(Cat cat) {
        setScale(cat, plugin.config().flattenScale());
        cat.setSitting(false);
        cat.setLyingDown(true);
        cat.setAware(false);
        cat.setGravity(true);
        if (cat.getPathfinder() != null) {
            cat.getPathfinder().stopPathfinding();
        }
    }

    private void playFlattenEffects(Cat cat) {
        cat.getWorld().playSound(cat.getLocation(), Sound.BLOCK_WOOL_BREAK, 1.0f, 0.7f);
        cat.getWorld().playSound(cat.getLocation(), Sound.ENTITY_CAT_HURT, 0.4f, 1.6f);
        cat.getWorld().spawnParticle(Particle.CLOUD, cat.getLocation().add(0.0, 0.15, 0.0), 12, 0.3, 0.05, 0.3, 0.02);
        cat.getWorld().spawnParticle(Particle.POOF, cat.getLocation().add(0.0, 0.1, 0.0), 6, 0.2, 0.05, 0.2, 0.01);
    }

    private static double currentScale(Cat cat) {
        AttributeInstance scale = cat.getAttribute(Attribute.SCALE);
        return scale != null ? scale.getBaseValue() : DEFAULT_SCALE;
    }

    private static void setScale(Cat cat, double value) {
        AttributeInstance scale = cat.getAttribute(Attribute.SCALE);
        if (scale != null) {
            scale.setBaseValue(value);
        }
    }
}
