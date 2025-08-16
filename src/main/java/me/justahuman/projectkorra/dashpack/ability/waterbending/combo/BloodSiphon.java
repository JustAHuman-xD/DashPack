package me.justahuman.projectkorra.dashpack.ability.waterbending.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.BloodAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.region.RegionProtection;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.AddonComboAbility;
import me.justahuman.projectkorra.dashpack.ability.PlayerLocationAbility;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Getter
public class BloodSiphon extends BloodAbility implements PlayerLocationAbility, AddonComboAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.DURATION)
    private long dashTime = getLong("DashTime");
    private long hitInterval = getLong("HitInterval");
    private double hitRadius = getDouble("HitRadius");
    @Attribute(Attribute.DAMAGE)
    private double damage = getDouble("Damage");
    private double healingEfficiency = getDouble("HealingEfficiency");
    private int maxHits = getInt("MaxHits", -1);

    private long time;
    private Set<UUID> hit = new HashSet<>();

    public BloodSiphon(Player player) {
        super(player);
        if (bPlayer.canBendIgnoreBinds(this) && !CoreAbility.hasAbility(player, BloodSiphon.class)) {
            start();
        }
    }

    @Override
    public void progress() {
        long time = System.currentTimeMillis();
        if (time - getStartTime() >= dashTime) {
            remove();
            return;
        } else if (time - this.time < this.hitInterval) {
            return;
        }
        this.time = time;

        Predicate<Entity> predicate = GeneralMethods.getEntityFilter().and(entity -> entity != player && !hit.contains(entity.getUniqueId()) && entity instanceof LivingEntity && !RegionProtection.isRegionProtected(this, entity.getLocation()));
        for (Entity entity : player.getWorld().getNearbyEntities(player.getBoundingBox().expand(hitRadius), predicate)) {
            LivingEntity target = (LivingEntity) entity;
            double damage = Math.min(this.damage, target.getHealth());
            target.damage(damage, player);
            player.heal(damage * healingEfficiency, EntityRegainHealthEvent.RegainReason.MAGIC);
            hit.add(target.getUniqueId());
            bPlayer.addCooldown(this);

            if (maxHits != -1 && hit.size() >= maxHits) {
                remove();
                return;
            }
        }
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
        return new BloodSiphon(player);
    }

    @Override
    public String getName() {
        return "BloodSiphon";
    }
}