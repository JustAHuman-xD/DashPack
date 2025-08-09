package me.justahuman.pk_hackathon.ability;

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
        return "Abilities." + getElement().getName() + "." + getName() + ".";
    }

    default int getInt(String path) {
        return ConfigManager.defaultConfig.get().getInt(configPath() + path);
    }

    default long getLong(String path) {
        return ConfigManager.defaultConfig.get().getLong(configPath() + path);
    }

    default double getDouble(String path) {
        return ConfigManager.defaultConfig.get().getDouble(configPath() + path);
    }

    default boolean getBoolean(String path) {
        return ConfigManager.defaultConfig.get().getBoolean(configPath() + path);
    }

    default List<String> getConfiguredCombination() {
        return ConfigManager.defaultConfig.get().getStringList(configPath() + "Combination");
    }

    default long getBaseCooldown() {
        return getLong("Cooldown");
    }
}
