package me.justahuman.pk_hackathon.ability.firebending.combo;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.LightningAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.lightning.Lightning;
import com.projectkorra.projectkorra.util.DamageHandler;
import lombok.Getter;
import me.justahuman.pk_hackathon.ability.PlayerLocationAbility;
import me.justahuman.pk_hackathon.ability.AddonComboAbility;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class LightningStep extends LightningAbility implements PlayerLocationAbility, AddonComboAbility {
    @Attribute(Attribute.COOLDOWN)
    private long cooldown = getBaseCooldown();

    private final Lightning lightning;
    private Location destination;
    private boolean obstructed;

    public LightningStep(Player player) {
        super(player);
        this.lightning = new Lightning(player);
        this.lightning.start();

        if (lightning.isStarted() && bPlayer.canBendIgnoreBinds(this)) {
            lightning.setCharged(true);
            player.setSneaking(false);
            start();

            if (isStarted()) {
                initialize();
            } else {
                lightning.remove();
            }
        } else if (lightning.isStarted()) {
            lightning.remove();
        }
    }

    private void initialize() {
        bPlayer.addCooldown(this);
        playLightningbendingSound(player.getLocation());

        lightning.progress();
        destination = lightning.getDestination();
        obstructed = destination == null || !isTransparent(destination.getBlock().getRelative(BlockFace.UP));
        if (obstructed && destination != null) {
            // todo: configurable radius
            for (Block block : GeneralMethods.getBlocksAroundPoint(destination, 1.25)) {
                if (isTransparent(block.getRelative(BlockFace.UP))) {
                    destination = block.getLocation();
                    obstructed = false;
                    break;
                }
            }
        }

        if (!obstructed) {
            destination = destination.clone();
            destination.setDirection(player.getLocation().getDirection());
            if (!isTransparent(destination.getBlock()) && isTransparent(destination.getBlock().getRelative(BlockFace.UP, 2))) {
                destination.add(0, 1, 0);
            } else if (isTransparent(destination.getBlock())) {
                int yDrop = 3; // TODO: Make this configurable
                while (isTransparent(destination.getBlock()) && yDrop > 0) {
                    destination.add(0, -1, 0);
                    yDrop--;
                }

                if (!isTransparent(destination.getBlock())) {
                    destination.add(0, 1, 0);
                }
            }
        }
    }

    @Override
    public void progress() {
        if (lightning.isRemoved() && lightning.getTasks().isEmpty()) {
            if (obstructed) {
                // todo: configurable damage
                DamageHandler.damageEntity(player, 4, this);
            } else {
                player.teleport(destination);
            }
            playLightningbendingSound(player.getLocation());
            remove();
            return;
        }

        if (!lightning.isRemoved()) {
            lightning.progress();
        }

        List<BukkitRunnable> tasksStep = new ArrayList<>(lightning.getTasks());
        for (int i = 0; i < 5; i++) {
            for (BukkitRunnable task : tasksStep) {
                if (!task.isCancelled()) {
                    task.run();
                }
            }

            tasksStep.removeIf(BukkitRunnable::isCancelled);
            if (tasksStep.isEmpty()) {
                break;
            }
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
        return new LightningStep(player);
    }

    @Override
    public String getName() {
        return "LightningStep";
    }
}
