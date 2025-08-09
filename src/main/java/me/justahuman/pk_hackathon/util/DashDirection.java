package me.justahuman.pk_hackathon.util;

import me.justahuman.pk_hackathon.PKHackathon;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@SuppressWarnings("UnstableApiUsage")
public enum DashDirection {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT,
    HELD;

    public Vector getVector(Player player, Input input, int pitchRestriction) {
        Location location = player.getLocation();
        if (pitchRestriction != -1) {
            location.setPitch(Math.clamp(location.getPitch(), -pitchRestriction, pitchRestriction));
        }

        Vector direction = location.getDirection();
        return switch (this) {
            case FORWARD -> direction;
            case BACKWARD -> direction.multiply(-1);
            case LEFT -> direction.crossProduct(new Vector(0, -1, 0)).normalize();
            case RIGHT -> direction.crossProduct(new Vector(0, 1, 0)).normalize();
            case HELD -> {
                Vector composite = new Vector(0, 0, 0);
                if (input.isForward()) {
                    composite.add(FORWARD.getVector(player, input, pitchRestriction));
                }
                if (input.isBackward()) {
                    composite.add(BACKWARD.getVector(player, input, pitchRestriction));
                }
                if (input.isLeft()) {
                    composite.add(LEFT.getVector(player, input, pitchRestriction));
                }
                if (input.isRight()) {
                    composite.add(RIGHT.getVector(player, input, pitchRestriction));
                }
                if (composite.isZero() && PKHackathon.instance.getConfig().getBoolean("empty-sprint-dash-forward")) {
                    composite.add(FORWARD.getVector(player, input, pitchRestriction));
                }
                yield composite.isZero() ? composite : composite.normalize();
            }
        };
    }

    public static DashDirection from(Input oldInput, Input newInput, DashTap tap) {
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
}
