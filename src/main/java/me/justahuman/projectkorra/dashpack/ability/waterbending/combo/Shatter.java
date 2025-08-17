package me.justahuman.projectkorra.dashpack.ability.waterbending.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.SurgeWall;
import com.projectkorra.projectkorra.waterbending.SurgeWave;
import com.projectkorra.projectkorra.waterbending.Torrent;
import com.projectkorra.projectkorra.waterbending.WaterSpoutWave;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArmsSpear;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.AddonComboAbility;
import me.justahuman.projectkorra.dashpack.util.FreezeHandler;
import me.justahuman.projectkorra.dashpack.util.Utils;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Getter
public class Shatter extends IceAbility implements AddonComboAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.RANGE)
    private double range = getDouble("Range");
    @Attribute(Attribute.RANGE)
    private double shatterRange = getDouble("ShatterRange");
    private int maxSize = getInt("MaxSize");
    @Attribute(Attribute.DAMAGE)
    private double damage = getDouble("Damage");
    @Attribute(Attribute.DURATION)
    private int freezeTicks = getInt("FreezeTicks");
    private int soundInterval = getInt("Sound.Interval");
    private int shatterParticles = getInt("ShatterParticles");

    private Location location;
    private Location minLocation;
    private Location maxLocation;
    private List<Location> locations = new ArrayList<>();

    public Shatter(Player player) {
        super(player);

        if (!bPlayer.canBendIgnoreBinds(this)) {
            return;
        }

        RayTraceResult result = player.rayTraceBlocks(range, FluidCollisionMode.NEVER);
        if (result == null || result.getHitBlock() == null) {
            return;
        }

        Block block = result.getHitBlock();
        if (!canShatter(block)) {
            return;
        }

        this.location = block.getLocation();
        this.minLocation = this.location.clone();
        this.maxLocation = this.location.clone();
        this.locations.add(this.location);
        for (BlockFace face : Utils.getSurround()) {
            scanShatterable(face, block.getRelative(face));
        }

        if (locations.size() <= maxSize && !locations.isEmpty()) {
            start();
            if (isStarted()) {
                progress();
            }
        }
    }

    private void scanShatterable(BlockFace sourceFace, Block block) {
        if (locations.size() > maxSize) {
            return;
        }

        Location loc = block.getLocation();
        if (locations.contains(loc) || !canShatter(block)) {
            return;
        }

        locations.add(loc);
        minLocation.setX(Math.min(minLocation.getX(), loc.getX()));
        minLocation.setY(Math.min(minLocation.getY(), loc.getY()));
        minLocation.setZ(Math.min(minLocation.getZ(), loc.getZ()));
        maxLocation.setX(Math.max(maxLocation.getX(), loc.getX()));
        maxLocation.setY(Math.max(maxLocation.getY(), loc.getY()));
        maxLocation.setZ(Math.max(maxLocation.getZ(), loc.getZ()));

        for (BlockFace face : createWeightedFaces(block)) {
            if (face != sourceFace.getOppositeFace()) {
                scanShatterable(face, block.getRelative(face));
            }
        }
    }

    @Override
    public void progress() {
        bPlayer.addCooldown(this);
        remove();

        BoundingBox box = BoundingBox.of(minLocation, maxLocation);
        Predicate<Entity> predicate = GeneralMethods.getEntityFilter().and(entity -> entity != player && entity instanceof LivingEntity && !RegionProtection.isRegionProtected(this, entity.getLocation()));
        for (Entity entity : player.getWorld().getNearbyEntities(box, predicate)) {
            LivingEntity target = (LivingEntity) entity;
            DamageHandler.damageEntity(entity, this.damage, this);
            new FreezeHandler(target, freezeTicks);
        }

        for (int i = 0, locationsSize = locations.size(); i < locationsSize; i++) {
            Location location = locations.get(i);
            Block block = location.getBlock();
            BlockData blockData = block.getBlockData();
            if (!SurgeWave.canThaw(block)) {
                SurgeWave.thaw(block);
            } else if (!Torrent.canThaw(block)) {
                Torrent.thaw(block);
            } else if (WaterArmsSpear.canThaw(block)) {
                WaterArmsSpear.thaw(block);
            } else if (WaterSpoutWave.canThaw(block)) {
                WaterSpoutWave.thaw(block);
            } else if (TempBlock.isTempBlock(block)) {
                TempBlock tempBlock = TempBlock.get(block);
                BlockState originalState = tempBlock.getState();
                Material originalType = originalState.getType();
                if (isIce(tempBlock.getBlock()) && (isIce(originalType) || isAir(originalType) || isWater(originalType))) {
                    originalState.setType(Material.AIR);
                    tempBlock.revertBlock();
                } else {
                    continue;
                }
            } else if (isIce(block)) {
                block.setType(Material.AIR);
            } else {
                continue;
            }

            block.getWorld().spawnParticle(Particle.BLOCK, location.toCenterLocation(), shatterParticles, 0.25, 0.25, 0.25, 0, blockData);
            if (i % soundInterval == 0) {
                location.getWorld().playSound(getSound(), block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
            }
        }
    }

    public boolean canShatter(Block block) {
        if (block.getWorld() != this.player.getWorld()
                || block.getLocation().distanceSquared(this.player.getLocation()) > this.shatterRange * this.shatterRange
                || RegionProtection.isRegionProtected(this.player, block.getLocation(), this)
                || SurgeWall.getWallBlocks().containsKey(block)
                || SurgeWave.isBlockWave(block)) {
            return false;
        }

        if (!SurgeWave.canThaw(block)) {
            return true;
        } else if (!Torrent.canThaw(block)) {
            return true;
        } else if (WaterArmsSpear.canThaw(block)) {
            return true;
        } else if (WaterSpoutWave.canThaw(block)) {
            return true;
        } else if (TempBlock.isTempBlock(block)) {
            final TempBlock tb = TempBlock.get(block);
            return isIce(tb.getBlock());
        } else return isIce(block);
    }

    @Override
    public int getPriority() {
        return 100;
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
        return "Shatter";
    }

    @Override
    public Object createNewComboInstance(Player player) {
        return new Shatter(player);
    }

    public List<BlockFace> createWeightedFaces(Block currentBlock) {
        final List<BlockFace> faces = new ArrayList<>(Utils.getSurround());
        faces.sort(Comparator.comparingDouble(face -> location.toCenterLocation().distance(currentBlock.getRelative(face).getLocation().toCenterLocation())));
        return faces;
    }
}
