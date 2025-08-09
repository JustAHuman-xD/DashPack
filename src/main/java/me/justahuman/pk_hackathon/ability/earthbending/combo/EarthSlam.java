package me.justahuman.pk_hackathon.ability.earthbending.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.earthbending.RaiseEarthWall;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import lombok.Getter;
import me.justahuman.pk_hackathon.ability.AddonComboAbility;
import me.justahuman.pk_hackathon.util.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class EarthSlam extends EarthAbility implements AddonComboAbility {
    public static final ComboManager.AbilityInformation COMBO_INFO = new ComboManager.AbilityInformation("EarthSmash", ClickType.RIGHT_CLICK_BLOCK);

    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.SELECT_RANGE)
    private double selectRange = getDouble("SelectRange");
    @Attribute(Attribute.HEIGHT)
    private double height = getDouble("Height");
    @Attribute(Attribute.WIDTH)
    private double width = getDouble("Width");
    @Attribute(Attribute.DAMAGE)
    private double damage = getDouble("Damage");
    @Attribute(Attribute.SPEED)
    private double speed = getDouble("Speed");
    @Attribute(Attribute.KNOCKBACK)
    private double pushFactor = getDouble("PushFactor");
    @Attribute(Attribute.RANGE)
    private double pushRange = getDouble("PushRange");
    private int maxHits = (int) getDouble("MaxHits");
    private boolean canHitSelf = getBoolean("CanHitSelf");

    private long interval = (long) (1000.0 / speed);

    private int hits = 0;
    private long time = System.currentTimeMillis();
    private BlockFace slamDirection;
    private final Set<Block> slamming = new LinkedHashSet<>();
    private Location origin;
    private Location location;

    public EarthSlam(Player player) {
        super(player);

        if (!bPlayer.canBendIgnoreBinds(this)) {
            return;
        }

        recalculateAttributes();

        Block sourceBlock = BlockSource.getEarthSourceBlock(this.player, this.selectRange, ClickType.SHIFT_DOWN);
        if (sourceBlock == null) {
            sourceBlock = getTargetEarthBlock((int) this.selectRange);
        }

        if (!applicable(sourceBlock)) {
            return;
        }

        for (int i = 1; i <= height; i++) {
            Block targetBlock = sourceBlock.getRelative(0, -i, 0);
            if (applicable(targetBlock)) {
                sourceBlock = targetBlock;
            }
        }
        origin = sourceBlock.getLocation().add(0, height / 2, 0);
        slamming.add(sourceBlock);

        Location eyeLocation = this.player.getEyeLocation();
        this.slamDirection = Utils.yawToFace(eyeLocation.getYaw());

        Vector direction = eyeLocation.getDirection().normalize();
        Vector orth = getDegreeRoundedVector(new Vector(-direction.getZ(), 0, direction.getX()).normalize(), 0.25);

        Set<Block> missed = new LinkedHashSet<>();
        for (int x = 0; x <= width; x++) {
            int offset = (int) (x - (width / 2));
            Block adjacent = sourceBlock.getLocation().add(orth.clone().multiply(offset)).getBlock();
            if (!applicable(adjacent)) {
                missed.add(adjacent);
            } else {
                slamming.add(adjacent);
            }
        }

        for (Block miss : missed) {
            for (BlockFace face : Utils.getAxis()) {
                Block adjacent = miss.getRelative(face);
                if (!slamming.contains(adjacent) && applicable(adjacent)) {
                    slamming.add(adjacent);
                    break;
                }
            }
        }

        for (Block base : new LinkedHashSet<>(slamming)) {
            for (int i = 0; i <= height; i++) {
                Block block = base.getRelative(0, i, 0);
                if (applicable(block)) {
                    slamming.add(block);
                }
            }
        }

        bPlayer.addCooldown(this);
        start();
    }

    @Override
    public void recalculateAttributes() {
        super.recalculateAttributes();
        this.interval = (long) (1000.0 / this.speed);
    }

    @Override
    public void progress() {
        if (System.currentTimeMillis() - this.time < this.interval) {
            return;
        }
        this.time = System.currentTimeMillis();

        Set<Block> old = new LinkedHashSet<>(slamming);
        slamming.clear();

        boolean sound = false;
        Location min = null;
        Location max = null;
        for (Block block : old) {
            if (!applicable(block)) {
                continue;
            }

            Block pushed = block.getRelative(slamDirection);
            if (isTransparent(pushed) && !pushed.isLiquid()) {
                GeneralMethods.breakBlock(pushed);
            } else {
                continue;
            }

            if (!sound) {
                playEarthbendingSound(pushed.getLocation());
                sound = true;
            }

            if (isEarthRevertOn()) {
                moveEarthBlock(block, pushed);
            } else {
                Material material = block.getType();
                block.setType(Material.AIR);
                pushed.setType(material);
            }
            slamming.add(pushed);

            if (min == null || block.getX() < min.getX() || block.getY() < min.getY() || block.getZ() < min.getZ()) {
                min = block.getLocation();
            }
            if (max == null || block.getX() > max.getX() || block.getY() > max.getY() || block.getZ() > max.getZ()) {
                max = block.getLocation();
            }
        }

        if (min != null) {
            boolean hit = false;
            BoundingBox box = BoundingBox.of(min, max);
            location = box.getCenter().toLocation(min.getWorld());

            for (Entity entity : min.getWorld().getNearbyEntities(box)) {
                if (!(entity instanceof LivingEntity) || (entity.getEntityId() == this.player.getEntityId() && !this.canHitSelf)
                        || GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
                    continue;
                }

                AirAbility.breakBreathbendingHold(entity);
                GeneralMethods.setVelocity(this, entity, player.getEyeLocation().getDirection().multiply(this.pushFactor));
                DamageHandler.damageEntity(entity, damage, this);
                hit = true;
            }

            if (hit) {
                hits++;
            }
        }

        if (slamming.isEmpty() || hits >= maxHits || (location != null && location.distanceSquared(origin) >= pushRange * pushRange)) {
            remove();
        }
    }

    private boolean applicable(Block block) {
        return block != null && isEarthbendable(player, getName(), block) && (!isEarthRevertOn() || getMovedEarth().containsKey(block));
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
    public String getName() {
        return "EarthSlam";
    }

    @Override
    public Location getLocation() {
        return location;
    }

    /**
     * TODO: Make this method, which is in {@link RaiseEarthWall} public
     */
    private static Vector getDegreeRoundedVector(Vector vec, double degreeIncrement) {
        if (vec == null) {
            return null;
        }
        vec = vec.normalize();
        final double[] dims = { vec.getX(), vec.getY(), vec.getZ() };

        for (int i = 0; i < dims.length; i++) {
            final double dim = dims[i];
            final int sign = dim >= 0 ? 1 : -1;
            final int dimDivIncr = (int) (dim / degreeIncrement);

            final double lowerBound = dimDivIncr * degreeIncrement;
            final double upperBound = (dimDivIncr + (1 * sign)) * degreeIncrement;

            if (Math.abs(dim - lowerBound) < Math.abs(dim - upperBound)) {
                dims[i] = lowerBound;
            } else {
                dims[i] = upperBound;
            }
        }
        return new Vector(dims[0], dims[1], dims[2]);
    }
}
