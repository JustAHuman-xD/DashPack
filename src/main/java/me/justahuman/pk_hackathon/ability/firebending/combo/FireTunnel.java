package me.justahuman.pk_hackathon.ability.firebending.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import lombok.Getter;
import me.justahuman.pk_hackathon.ability.AddonComboAbility;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Getter
public class FireTunnel extends FireAbility implements AddonComboAbility {
    private static final Vector NORTH = new Vector(0, 0, 1);

    @Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.RANGE) @DayNightFactor
    private double maxLength = getDouble("Length");
    @Attribute(Attribute.HEIGHT) @DayNightFactor
    private double radius = getDouble("Radius");
    @Attribute(Attribute.DAMAGE) @DayNightFactor
    private double damage = getDouble("Damage");
    private long damageInterval = getLong("DamageInterval");
    @Attribute(Attribute.FIRE_TICK) @DayNightFactor
    private double fireTicks = getDouble("FireTicks");
    @Attribute(Attribute.DURATION) @DayNightFactor
    private long duration = getLong("Duration");
    private double speed = getDouble("Speed");
    private double growRate = getDouble("GrowRate");
    private long growInterval = (long) (1000.0 / speed);
    private double growAmount = (maxLength / ((double) duration / growInterval)) * growRate;
    private long displayInterval = getLong("DisplayInterval");
    private int displayRate = getInt("DisplayRate");
    private double degreesPerParticle = getDouble("DegreesPerParticle");
    private double particleOffset = getDouble("ParticleOffset");
    private double ringLength = getDouble("RingLength");
    private long soundInterval = getLong("SoundInterval");

    private final List<Location> locations = new ArrayList<>();
    private Location location;
    private Vector direction;

    private long time;
    private int growTick;
    private int displayTick;
    private int soundTick;
    private int damageTick;
    private double length = growAmount;
    private BoundingBox volume;
    private final Map<Double, Vector> ringCache = new HashMap<>();

    public FireTunnel(Player player) {
        super(player);
        if (!bPlayer.canBendIgnoreBinds(this) || CoreAbility.hasAbility(player, FireTunnel.class)) {
            return;
        }

        Location playerLocation = player.getLocation();
        playerLocation.setPitch(0);
        this.location = playerLocation.getBlock().getLocation().add(0.5, 0, 0.5);
        this.direction = playerLocation.getDirection();
        this.locations.add(this.location);
        this.start();
    }

    @Override
    public void recalculateAttributes() {
        super.recalculateAttributes();
        growInterval = (long) (1000.0 / speed);
        growAmount = (maxLength / ((double) duration / growInterval)) * growRate;
        length = Math.max(length, growAmount);
        ringCache.clear();
        calculateVolume();
    }

    @Override
    public void progress() {
        this.time = System.currentTimeMillis();
        if (this.time - getStartTime() >= duration) {
            bPlayer.addCooldown(this);
            remove();
            return;
        }

        if (this.length < this.maxLength && this.time - getStartTime() > this.growTick * this.growInterval) {
            this.growTick++;
            double oldLength = this.length;
            this.length = Math.min(this.length + growAmount, this.maxLength);
            for (double l = oldLength; l < this.length; l += ringLength) {
                Location last = this.locations.getLast();
                Location location = this.location.clone().add(this.direction.clone().multiply(l));
                if (!this.locations.contains(location) && last.distanceSquared(location) >= ringLength * ringLength) {
                    this.locations.add(location);
                    displayRing(location);
                }
            }
            calculateVolume();
        }
        if (this.time - getStartTime() > this.displayTick * this.displayInterval) {
            this.displayTick++;
            display();
        }
        if (this.time - getStartTime() > this.soundTick * this.soundInterval) {
            this.soundTick++;
            sound();
        }
        if (this.time - getStartTime() > this.damageTick * this.damageInterval) {
            this.damageTick++;
            damage();
        }
    }

    private void calculateVolume() {
        Location min = this.location.clone().subtract(radius, 0, radius);
        Location max = this.location.clone().add(direction.clone().multiply(length)).add(radius, radius * 2, radius);
        volume = BoundingBox.of(min, max);
    }

    private void display() {
        for (Location base : this.locations) {
            displayRing(base);
        }
    }

    private void displayRing(Location base) {
        Location center = base.clone().add(0, radius, 0);
        int tickMod = displayTick % displayRate;
        int particleIndex = 0;
        for (double angle = 0; angle < 360; angle += degreesPerParticle) {
            if (particleIndex % displayRate == tickMod) {
                Vector offset = ringCache.computeIfAbsent(angle, a -> {
                    double radians = Math.toRadians(a);
                    double x = radius * Math.cos(radians);
                    double y = radius * Math.sin(radians);
                    return rotateVector(new Vector(x, y, 0), direction);
                });
                Location particleLocation = center.clone().add(offset);
                playFirebendingParticles(particleLocation, 1, particleOffset, particleOffset, particleOffset);
            }
            particleIndex++;
        }
    }

    private Vector rotateVector(Vector vector, Vector direction) {
        if (direction.equals(NORTH)) {
            return vector;
        }
        Vector axis = NORTH.getCrossProduct(direction).normalize();
        double angle = Math.acos(direction.dot(NORTH) / direction.length());
        return vector.clone().rotateAroundAxis(axis, angle);
    }

    private void sound() {
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            if (i % displayRate == soundTick % displayRate) {
                playFirebendingSound(loc);
            }
        }
    }

    private boolean inTunnel(Entity entity) {
        Location entityLoc = entity.getLocation().add(0, entity.getHeight() / 2, 0);
        Vector start = this.location.toVector();
        Vector toEntity = entityLoc.toVector().subtract(start);

        double projection = toEntity.dot(this.direction);
        if (projection < 0 || projection > this.length) {
            return false;
        }

        Vector sliceCenter = start.clone().add(this.direction.clone().multiply(projection)).add(new Vector(0, radius, 0));
        double sqDistance = entityLoc.toVector().distanceSquared(sliceCenter);
        return sqDistance <= this.radius * this.radius;
    }

    private void damage() {
        Predicate<Entity> predicate = GeneralMethods.getEntityFilter().and(this::inTunnel).and(entity -> entity != player && entity instanceof LivingEntity && !RegionProtection.isRegionProtected(this, entity.getLocation()));
        final Collection<Entity> entities = this.location.getWorld().getNearbyEntities(volume, predicate);
        for (final Entity entity : entities) {
            LivingEntity living = (LivingEntity) entity;
            GeneralMethods.setVelocity(this, entity, new Vector(0, 0, 0));
            final Block block = living.getEyeLocation().getBlock();
            if (TempBlock.isTempBlock(block) && isIce(block)) {
                continue;
            }

            DamageHandler.damageEntity(entity, this.damage, this);
            AirAbility.breakBreathbendingHold(entity);
            entity.setFireTicks((int) (this.fireTicks * 20));
            new FireDamageTimer(entity, this.player, this);
        }
    }

    @Override
    public boolean consumesDash() {
        return true;
    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public Object createNewComboInstance(Player player) {
        return new FireTunnel(player);
    }

    @Override
    public String getName() {
        return "FireTunnel";
    }
}
