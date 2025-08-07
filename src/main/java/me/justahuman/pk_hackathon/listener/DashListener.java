package me.justahuman.pk_hackathon.listener;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.event.AbilityLoadEvent;
import me.justahuman.pk_hackathon.PKHackathon;
import me.justahuman.pk_hackathon.ability.TempDashAbility;
import org.bukkit.Input;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class DashListener implements Listener {
    // TODO: Adjust timeout based on players ping
    private static final int DASH_TAP_TIMEOUT = 3;

    private final Map<UUID, DashData> dashing = new HashMap<>();

    @EventHandler
    public void onAbilityLoad(AbilityLoadEvent<?> event) {
        if (event.getLoadable() instanceof TempDashAbility dash) {
            // TODO: Make a PR that makes using CoreAbility.registerPluginAbilities() respect if an ability is an AddonAbility (it does not currently t-t)
            dash.load();
        }
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (PKHackathon.dash == null || bPlayer.isOnCooldown(PKHackathon.dash)) {
            return;
        }

        int time = player.getTicksLived();
        Input oldInput = player.getCurrentInput();
        Input newInput = event.getInput();

        DashData dashData = dashing.remove(player.getUniqueId());
        if (dashData != null && time - dashData.tapTime > DASH_TAP_TIMEOUT) {
            dashData = null;
        }

        DashDirection direction = getDirection(oldInput, newInput, dashData != null ? dashData.tap : null);
        if (direction == null) {
            return;
        }

        if (dashData == null || direction != dashData.direction) {
            dashing.put(player.getUniqueId(), new DashData(direction, DashTap.FIRST, time));
        } else if (dashData.tap == DashTap.FIRST) {
            dashing.put(player.getUniqueId(), new DashData(direction, DashTap.SECOND, time));
        } else {
            // TODO: Move this all to the dash ability itself and restrict the y velocity based on the element.
            //  Airbenders should be able to dash up but waterbenders shouldn't.
            //  Also dashing left while looking directly up/down currently dashes up/down instead of left/right.
            Vector playerDirection = player.getLocation().getDirection();
            Vector dashVelocity = playerDirection.clone();
            switch (direction) {
                case FORWARD -> {}
                case BACKWARD -> dashVelocity.multiply(-1);
                case LEFT -> dashVelocity.rotateAroundY(Math.toRadians(90));
                case RIGHT -> dashVelocity.rotateAroundY(Math.toRadians(-90));
                case HELD -> {
                    dashVelocity = new Vector();
                    if (newInput.isForward()) {
                        dashVelocity.add(playerDirection.clone());
                    }
                    if (newInput.isBackward()) {
                        dashVelocity.add(playerDirection.clone().multiply(-1));
                    }
                    if (newInput.isLeft()) {
                        dashVelocity.add(playerDirection.clone().rotateAroundY(Math.toRadians(90)));
                    }
                    if (newInput.isRight()) {
                        dashVelocity.add(playerDirection.clone().rotateAroundY(Math.toRadians(-90)));
                    }
                    dashVelocity.normalize();
                }
            }
            dashVelocity.multiply(3);
            Vector newVelocity = player.getVelocity().add(dashVelocity);
            player.setVelocity(newVelocity);
            player.setSprinting(true);
            // player.sendMessage("Dashed " + direction.name().toLowerCase());
            bPlayer.addCooldown(PKHackathon.dash);
        }
    }

    private DashDirection getDirection(Input oldInput, Input newInput, DashTap tap) {
        boolean enabling = tap == null || tap == DashTap.SECOND;
        DashDirection direction = null;
        if (oldInput.isForward() != newInput.isForward() && (enabling == newInput.isForward())) {
            direction = DashDirection.FORWARD;
        } else if (oldInput.isBackward() != newInput.isBackward() && (enabling == newInput.isBackward())) {
            direction = DashDirection.BACKWARD;
        } else if (oldInput.isLeft() != newInput.isLeft() && (enabling == newInput.isLeft())) {
            direction = DashDirection.LEFT;
        } else if (oldInput.isRight() != newInput.isRight() && (enabling == newInput.isRight())) {
            direction = DashDirection.RIGHT;
        } else if (oldInput.isSprint() != newInput.isSprint() && (enabling == newInput.isSprint())) {
            direction = DashDirection.HELD;
        }
        return direction;
    }

    private record DashData(DashDirection direction, DashTap tap, int tapTime) { }

    private enum DashTap {
        FIRST,
        SECOND
    }

    private enum DashDirection {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        HELD
    }
}
