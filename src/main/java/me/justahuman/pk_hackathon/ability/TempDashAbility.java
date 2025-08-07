package me.justahuman.pk_hackathon.ability;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import me.justahuman.pk_hackathon.PKHackathon;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * This ability is a temporary placeholder until I implement the per element dash abilities.
 */
@SuppressWarnings("UnstableApiUsage")
public class TempDashAbility extends CoreAbility implements ComboAbility, AddonAbility {
    @Override
    public void progress() {

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
    public boolean isIgniteAbility() {
        return false;
    }

    @Override
    public boolean isExplosiveAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return 1000;
    }

    @Override
    public String getName() {
        return "Dash";
    }

    @Override
    public String getDescription() {
        return "Temp Dash Description";
    }

    @Override
    public String getInstructions() {
        return "Temp Dash Instructions";
    }

    @Override
    public Element getElement() {
        return Element.CHI;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public ArrayList<ComboManager.AbilityInformation> getCombination() {
        return new ArrayList<>();
    }

    @Override
    public Object createNewComboInstance(Player player) {
        return new TempDashAbility();
    }

    @Override
    public void load() {
        PKHackathon.dash = this;
    }

    @Override
    public void stop() {}

    @Override
    public String getAuthor() {
        return String.join(",", PKHackathon.instance.getPluginMeta().getAuthors());
    }

    @Override
    public String getVersion() {
        return PKHackathon.instance.getPluginMeta().getVersion();
    }
}
