package me.justahuman.pk_hackathon.ability;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import me.justahuman.pk_hackathon.PKHackathon;

import java.util.List;

public interface MyAddonAbility extends Ability, AddonAbility {
    // ** AddonAbility methods ** //

    @Override
    default void load() {}

    @Override
    default void stop() {}

    @Override
    default String getAuthor() {
        return String.join(",", PKHackathon.instance.getPluginMeta().getAuthors());
    }

    @Override
    default String getVersion() {
        return PKHackathon.instance.getPluginMeta().getVersion();
    }

    // ** MyAddonAbility methods ** //

    default String configPath() {
        String elementName = getElement().getName();
        if (getElement() instanceof Element.SubElement sub) {
            elementName = sub.getParentElement().getName();
        }
        return "Abilities." + elementName + "." + getName() + ".";
    }

    default int getInt(String path) {
        return getInt(path, 0);
    }

    default int getInt(String path, int def) {
        return ConfigManager.defaultConfig.get().getInt(configPath() + path);
    }

    default long getLong(String path) {
        return ConfigManager.defaultConfig.get().getLong(configPath() + path);
    }

    default double getDouble(String path) {
        return getDouble(path, 0.0);
    }

    default double getDouble(String path, double def) {
        return ConfigManager.defaultConfig.get().getDouble(configPath() + path, def);
    }

    default boolean getBoolean(String path) {
        return ConfigManager.defaultConfig.get().getBoolean(configPath() + path);
    }

    default List<String> getStringList(String path) {
        return ConfigManager.defaultConfig.get().getStringList(configPath() + path);
    }

    default long getBaseCooldown() {
        return getLong("Cooldown");
    }
}
