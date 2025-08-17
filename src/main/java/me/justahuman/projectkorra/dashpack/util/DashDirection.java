package me.justahuman.projectkorra.dashpack.util;

import me.justahuman.projectkorra.dashpack.DashPack;
import me.justahuman.projectkorra.dashpack.ability.DashAbility;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public enum DashDirection {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT,
    HELD,

    ANY,
    UP,
    DOWN;

    public Vector getVector(DashAbility ability) {
        return getVector(ability.getPlayer(), ability.getInput(), ability.getPitchRestriction());
    }

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
                if (input.isForward() && !input.isBackward()) {
                    composite.add(FORWARD.getVector(player, input, pitchRestriction));
                } else if (input.isBackward() && !input.isForward()) {
                    composite.add(BACKWARD.getVector(player, input, pitchRestriction));
                }

                if (input.isLeft() && !input.isRight()) {
                    composite.add(LEFT.getVector(player, input, pitchRestriction));
                } else if (input.isRight() && !input.isLeft()) {
                    composite.add(RIGHT.getVector(player, input, pitchRestriction));
                }

                boolean empty = !input.isForward() && !input.isBackward() && !input.isLeft() && !input.isRight();
                if (empty && DashPack.instance.getConfig().getBoolean("Dash.EmptySprintForward")) {
                    composite.add(FORWARD.getVector(player, input, pitchRestriction));
                }

                yield composite.isZero() ? composite : composite.normalize();
            }
            default -> throw new IllegalArgumentException("Only input directions can be used to get a vector.");
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

    public List<DashDirection> describing(DashAbility ability) {
        return describing(ability, ability.getPlayer());
    }

    public List<DashDirection> describing(DashAbility ability, Player player) {
        List<DashDirection> directions = new ArrayList<>();
        if (this == HELD) {
            Input input = ability.getInput();
            if (input.isForward() && !input.isBackward()) {
                directions.addAll(FORWARD.describing(ability, player));
            } else if (input.isBackward() && !input.isForward()) {
                directions.addAll(BACKWARD.describing(ability, player));
            }

            if (input.isLeft() && !input.isRight()) {
                directions.add(LEFT);
            } else if (input.isRight() && !input.isLeft()) {
                directions.add(RIGHT);
            }

            boolean empty = !input.isForward() && !input.isBackward() && !input.isLeft() && !input.isRight();
            if (empty && DashPack.instance.getConfig().getBoolean("Dash.EmptySprintForward")) {
                directions.addAll(FORWARD.describing(ability, player));
            }
            return directions;
        }

        directions.add(this);
        BlockFace face = Utils.getClosestBlockFace(ability.getDirection().getVector(player, ability.getInput(), ability.getPitchRestriction()));
        switch(face) {
            case UP -> directions.add(UP);
            case DOWN -> directions.add(DOWN);
        }

        return directions;
    }
}
