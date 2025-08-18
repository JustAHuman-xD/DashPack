package me.justahuman.projectkorra.dashpack;

import com.google.common.base.Charsets;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.board.BendingBoardManager;
import com.projectkorra.projectkorra.configuration.Config;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import me.justahuman.projectkorra.dashpack.listener.MainListener;
import me.justahuman.projectkorra.dashpack.settings.menu.PlayerSettingsMenu;
import me.justahuman.projectkorra.dashpack.util.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class DashPack extends JavaPlugin {
    public static DashPack instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        addAddonDefaults(ConfigManager.defaultConfig, "abilities.yml");
        addAddonDefaults(ConfigManager.languageConfig, "language.yml");
        addAddonDefaults(ConfigManager.avatarStateConfig, "avatarstate.yml");
        // The first time language.yml has its defaults added, it won't reload the board manager to show the dash cooldown, this fixes that
        BendingBoardManager.reload();

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new MainListener(), this);

        CoreAbility.registerPluginAbilities(this, "me.justahuman.projectkorra.dashpack.ability");

        getServer().getScheduler().runTaskLater(this, Utils::addPriorityToComboManager, 80L);
    }

    private void addAddonDefaults(Config target, String resourcePath) {
        InputStream stream = getResource(resourcePath);
        if (stream != null) {
            target.get().addDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream, Charsets.UTF_8)));
            target.save();
            target.reload();
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            } else if (!player.hasPermission("dashpack.menu")) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }
            PlayerSettingsMenu.open(player);
        } else {
            sender.sendMessage("Usage: /dash");
        }
        return true;
    }

    @Override
    public void onDisable() {

    }

    public static NamespacedKey key(String key) {
        return new NamespacedKey(instance, key);
    }
}
