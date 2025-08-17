package me.justahuman.projectkorra.dashpack.ability.earthbending.combo;

import com.destroystokyo.paper.ParticleBuilder;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import lombok.Getter;
import me.justahuman.projectkorra.dashpack.ability.PlayerLocationAbility;
import me.justahuman.projectkorra.dashpack.ability.AddonComboAbility;
import me.justahuman.projectkorra.dashpack.ability.earthbending.EarthDash;
import me.justahuman.projectkorra.dashpack.util.DashDirection;
import org.bukkit.Input;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@Getter
@SuppressWarnings("UnstableApiUsage")
public class EarthLaunch extends EarthAbility implements PlayerLocationAbility, AddonComboAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();
    @Attribute(Attribute.SELF_PUSH)
    private double pushFactor = getDouble("PushFactor");
    private boolean additive = getBoolean("Additive");

    private Block sourceBlock;
    private Vector launchVector;

    public EarthLaunch(Player player, Input input, DashDirection direction) {
        super(player);

        this.sourceBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        this.launchVector = direction.getVector(player, input, 0);
        if (bPlayer.canBendIgnoreBinds(this) && !this.launchVector.isZero() && isEarthbendable(player, getName(), sourceBlock)) {
            this.start();
        }
    }

    @Override
    public void progress() {
        new ParticleBuilder(Particle.DUST_PILLAR)
                .location(sourceBlock.getLocation().add(0.5, 1, 0.5))
                .count(getInt("Particles")).extra(0.5)
                .offset(0.5, 0.5, 0.5)
                .data(sourceBlock.getBlockData())
                .spawn();
        playEarthbendingSound(getLocation());

        this.launchVector = this.launchVector.multiply(pushFactor).add(new Vector(0, pushFactor, 0));
        GeneralMethods.setVelocity(this, player, additive ? player.getVelocity().add(launchVector) : launchVector);

        this.bPlayer.addCooldown(this);
        remove();
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
        EarthDash dash = (EarthDash) CoreAbility.getAbility(EarthDash.class);
        return dash == null ? null : new EarthLaunch(player, dash.getInput(), dash.getDirection());
    }

    @Override
    public String getName() {
        return "EarthLaunch";
    }
}
