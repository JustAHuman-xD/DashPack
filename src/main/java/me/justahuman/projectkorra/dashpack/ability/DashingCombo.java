package me.justahuman.projectkorra.dashpack.ability;

import org.bukkit.util.Vector;

public interface DashingCombo extends AddonComboAbility {
    default boolean isDashing() {
        Vector initial = getInitialVelocity();
        Vector current = getPlayer().getVelocity();
        double initialLength = initial.length();
        double currentLength = current.length();
        double lengthFraction = initialLength == 0 ? 0 : currentLength / initialLength;
        double angle = initial.angle(current);
        return lengthFraction >= 0.3 && angle < Math.toRadians(30);
    }

    Vector getInitialVelocity();
}
