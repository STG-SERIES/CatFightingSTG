package dev.catfighting.fight;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import dev.catfighting.CatFightingPlugin;
import dev.catfighting.PluginConfig;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Cat;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

public final class CatFightGoal implements Goal<Cat> {

    private static final double CONTACT_DISTANCE = 0.9;
    private static final double TOO_CLOSE = 0.7;

    private static GoalKey<Cat> KEY;

    private final CatFightingPlugin plugin;
    private final Cat cat;
    private final FightManager fights;

    private int hissCooldown;
    private int swipeCooldown;

    public CatFightGoal(CatFightingPlugin plugin, Cat cat, FightManager fights) {
        this.plugin = plugin;
        this.cat = cat;
        this.fights = fights;
    }

    public static void init(CatFightingPlugin plugin) {
        KEY = GoalKey.of(Cat.class, new NamespacedKey(plugin, "fight"));
    }

    public static GoalKey<Cat> key() {
        return KEY;
    }

    @Override
    public GoalKey<Cat> getKey() {
        return KEY;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK, GoalType.TARGET);
    }

    @Override
    public boolean shouldActivate() {
        if (!cat.isValid()) {
            return false;
        }
        if (fights.isFighting(cat)) {
            return fights.opponentOf(cat) != null;
        }
        return fights.tryStartFight(cat);
    }

    @Override
    public boolean shouldStayActive() {
        return fights.isFighting(cat) && fights.opponentOf(cat) != null;
    }

    @Override
    public void start() {
        hissCooldown = 0;
        swipeCooldown = 4;
        Cat opponent = fights.opponentOf(cat);
        if (opponent != null) {
            playHiss(cat);
            playHiss(opponent);
            spawnAngry(cat);
            spawnAngry(opponent);
        }
    }

    @Override
    public void stop() {
        fights.endFight(cat);
        if (cat.isValid()) {
            cat.getPathfinder().stopPathfinding();
        }
    }

    @Override
    public void tick() {
        Cat opponent = fights.opponentOf(cat);
        if (opponent == null) {
            return;
        }

        PluginConfig config = plugin.config();
        cat.setSitting(false);
        cat.setLyingDown(false);
        cat.lookAt(opponent);

        double distance = horizontalDistance(cat, opponent);
        closeIn(opponent, distance, config.fightSpeed());

        if (hissCooldown-- <= 0) {
            playHiss(cat);
            spawnAngry(cat);
            hissCooldown = config.hissIntervalTicks();
        }

        if (distance <= config.meleeRange() && swipeCooldown-- <= 0) {
            swipe(opponent);
            swipeCooldown = config.swipeIntervalTicks();
        }
    }

    private void closeIn(Cat opponent, double distance, double speed) {
        Vector away = horizontalOffset(cat, opponent);
        Location stand = opponent.getLocation().add(away.clone().multiply(CONTACT_DISTANCE));

        if (distance > CONTACT_DISTANCE + 0.2) {
            cat.getPathfinder().moveTo(stand, Math.max(1.2, speed));
            setGroundVelocity(toward(stand, 0.14));
            return;
        }

        cat.getPathfinder().stopPathfinding();
        if (distance < TOO_CLOSE) {
            setGroundVelocity(away.multiply(0.14));
            return;
        }
        setGroundVelocity(new Vector(0.0, Math.min(cat.getVelocity().getY(), 0.0), 0.0));
    }

    private void swipe(Cat opponent) {
        cat.swingMainHand();
        opponent.playHurtAnimation(yawTo(cat, opponent));

        Location hit = opponent.getLocation().add(0.0, 0.25, 0.0);
        opponent.getWorld().spawnParticle(Particle.CRIT, hit, 8, 0.12, 0.08, 0.12, 0.02);
        opponent.getWorld().spawnParticle(Particle.SWEEP_ATTACK, hit, 1, 0.0, 0.0, 0.0, 0.0);
        opponent.getWorld().playSound(hit, Sound.ENTITY_CAT_HURT, 0.7f, 1.45f);
        opponent.getWorld().playSound(hit, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.25f, 1.8f);

        Vector away = horizontalOffset(cat, opponent);
        setGroundVelocity(away.multiply(plugin.config().knockback()));
        Vector recoil = away.clone().multiply(-plugin.config().knockback());
        recoil.setY(Math.min(opponent.getVelocity().getY(), 0.0));
        opponent.setVelocity(recoil);
    }

    private void setGroundVelocity(Vector velocity) {
        velocity.setY(Math.min(cat.getVelocity().getY(), 0.0));
        cat.setVelocity(velocity);
    }

    private Vector toward(Location target, double scale) {
        Vector vector = target.toVector().subtract(cat.getLocation().toVector());
        vector.setY(0.0);
        if (vector.lengthSquared() < 1.0E-4) {
            return new Vector(0.0, 0.0, 0.0);
        }
        return vector.normalize().multiply(scale);
    }

    private static Vector horizontalOffset(Cat from, Cat to) {
        Vector offset = from.getLocation().toVector().subtract(to.getLocation().toVector());
        offset.setY(0.0);
        if (offset.lengthSquared() < 1.0E-4) {
            return new Vector(1.0, 0.0, 0.0);
        }
        return offset.normalize();
    }

    private static double horizontalDistance(Cat a, Cat b) {
        Location from = a.getLocation();
        Location to = b.getLocation();
        double dx = from.getX() - to.getX();
        double dz = from.getZ() - to.getZ();
        return Math.hypot(dx, dz);
    }

    private static void playHiss(Cat cat) {
        boolean low = ThreadLocalRandom.current().nextBoolean();
        Sound sound = low ? Sound.ENTITY_CAT_HISS : Sound.ENTITY_CAT_HURT;
        float pitch = low ? 0.75f : 1.25f;
        cat.getWorld().playSound(cat.getLocation(), sound, 0.9f, pitch);
    }

    private static void spawnAngry(Cat cat) {
        Location above = cat.getLocation().add(0.0, 0.55, 0.0);
        cat.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, above, 2, 0.15, 0.1, 0.15, 0.0);
    }

    private static float yawTo(Cat from, Cat to) {
        Location a = from.getLocation();
        Location b = to.getLocation();
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return (float) Math.toDegrees(Math.atan2(dz, dx));
    }
}
