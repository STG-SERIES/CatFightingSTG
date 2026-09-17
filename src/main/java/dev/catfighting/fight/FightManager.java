package dev.catfighting.fight;

import dev.catfighting.CatFightingPlugin;
import dev.catfighting.PluginConfig;
import dev.catfighting.flatten.FlattenManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Cat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class FightManager {

    private final CatFightingPlugin plugin;
    private final FlattenManager flatten;
    private final Map<UUID, UUID> opponents = new ConcurrentHashMap<>();
    private final Map<UUID, Long> fightUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldownUntil = new ConcurrentHashMap<>();

    public FightManager(CatFightingPlugin plugin, FlattenManager flatten) {
        this.plugin = plugin;
        this.flatten = flatten;
    }

    public void attach(Cat cat) {
        if (Bukkit.getMobGoals().hasGoal(cat, CatFightGoal.key())) {
            return;
        }
        Bukkit.getMobGoals().addGoal(cat, 4, new CatFightGoal(plugin, cat, this));
    }

    public boolean tryStartFight(Cat cat) {
        PluginConfig config = plugin.config();
        if (!config.fightsEnabled() || !canStartFight(cat)) {
            return false;
        }
        if (cat.getTicksLived() % config.checkIntervalTicks() != 0) {
            return false;
        }
        if (ThreadLocalRandom.current().nextDouble() > config.startChance()) {
            return false;
        }

        Cat opponent = findOpponent(cat);
        if (opponent == null) {
            return false;
        }
        return startFight(cat, opponent);
    }

    public boolean startFight(Cat a, Cat b) {
        if (isFighting(a) || isFighting(b)) {
            return false;
        }
        long until = now() + plugin.config().durationTicks();
        opponents.put(a.getUniqueId(), b.getUniqueId());
        opponents.put(b.getUniqueId(), a.getUniqueId());
        fightUntil.put(a.getUniqueId(), until);
        fightUntil.put(b.getUniqueId(), until);
        a.setSitting(false);
        b.setSitting(false);
        a.setLyingDown(false);
        b.setLyingDown(false);
        a.setCollidable(true);
        b.setCollidable(true);
        return true;
    }

    public void endFight(Cat cat) {
        UUID id = cat.getUniqueId();
        UUID otherId = opponents.remove(id);
        fightUntil.remove(id);
        long cool = now() + plugin.config().cooldownTicks();
        cooldownUntil.put(id, cool);
        restoreAfterFight(cat);
        if (otherId != null) {
            opponents.remove(otherId);
            fightUntil.remove(otherId);
            cooldownUntil.put(otherId, cool);
            if (Bukkit.getEntity(otherId) instanceof Cat other) {
                restoreAfterFight(other);
            }
        }
    }

    private static void restoreAfterFight(Cat cat) {
        if (!cat.isValid()) {
            return;
        }
        cat.setCollidable(true);
        cat.getPathfinder().stopPathfinding();
    }

    public boolean isFighting(Cat cat) {
        UUID id = cat.getUniqueId();
        if (!opponents.containsKey(id)) {
            return false;
        }
        Long until = fightUntil.get(id);
        return until != null && until > now();
    }

    public Cat opponentOf(Cat cat) {
        UUID otherId = opponents.get(cat.getUniqueId());
        if (otherId == null) {
            return null;
        }
        if (!(Bukkit.getEntity(otherId) instanceof Cat other) || !other.isValid()) {
            endFight(cat);
            return null;
        }
        if (!stillEligible(other) || !stillEligible(cat)) {
            endFight(cat);
            return null;
        }
        Long until = fightUntil.get(cat.getUniqueId());
        if (until == null || until <= now()) {
            endFight(cat);
            return null;
        }
        return other;
    }

    public boolean areFighting(Cat a, Cat b) {
        UUID other = opponents.get(a.getUniqueId());
        return other != null && other.equals(b.getUniqueId());
    }

    public boolean canStartFight(Cat cat) {
        if (!stillEligible(cat)) {
            return false;
        }
        Long cool = cooldownUntil.get(cat.getUniqueId());
        return cool == null || cool <= now();
    }

    public boolean stillEligible(Cat cat) {
        PluginConfig config = plugin.config();
        if (!cat.isValid() || cat.isDead() || flatten.isFlattened(cat)) {
            return false;
        }
        if (config.skipSitting() && cat.isSitting()) {
            return false;
        }
        if (cat.isSleeping()) {
            return false;
        }
        if (!config.kittensCanFight() && !cat.isAdult()) {
            return false;
        }
        if (!config.tamedCatsCanFight() && cat.isTamed()) {
            return false;
        }
        return true;
    }

    public Cat findOpponent(Cat cat) {
        PluginConfig config = plugin.config();
        Cat best = null;
        double bestDist = Double.MAX_VALUE;
        for (Cat other : cat.getWorld().getNearbyEntitiesByType(Cat.class, cat.getLocation(), config.searchRange())) {
            if (other.getUniqueId().equals(cat.getUniqueId()) || isFighting(other) || !canStartFight(other)) {
                continue;
            }
            if (!config.sameOwnerCanFight() && sameOwner(cat, other)) {
                continue;
            }
            double dist = cat.getLocation().distanceSquared(other.getLocation());
            if (dist < bestDist) {
                bestDist = dist;
                best = other;
            }
        }
        return best;
    }

    public void clear() {
        for (UUID id : opponents.keySet()) {
            if (Bukkit.getEntity(id) instanceof Cat cat) {
                restoreAfterFight(cat);
            }
        }
        opponents.clear();
        fightUntil.clear();
        cooldownUntil.clear();
    }

    private static boolean sameOwner(Cat a, Cat b) {
        if (!a.isTamed() || !b.isTamed() || a.getOwner() == null || b.getOwner() == null) {
            return false;
        }
        return a.getOwner().getUniqueId().equals(b.getOwner().getUniqueId());
    }

    private static long now() {
        return Bukkit.getCurrentTick();
    }
}
