package me.justahuman.projectkorra.dashpack.settings.menu;

import com.projectkorra.projectkorra.util.ChatUtil;
import me.justahuman.projectkorra.dashpack.DashPack;
import me.justahuman.projectkorra.dashpack.settings.PlayerSettings;
import me.justahuman.projectkorra.dashpack.settings.menu.item.BooleanOptionItem;
import me.justahuman.projectkorra.dashpack.settings.menu.item.ElementOptionItem;
import me.justahuman.projectkorra.dashpack.settings.menu.item.TicksOptionItem;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.window.Window;

public class PlayerSettingsMenu {
    public static void open(Player player) {
        FileConfiguration config = DashPack.instance.getConfig();
        Gui gui = Gui.normal()
                .setStructure(config.getStringList("SettingsMenu.Layout").toArray(String[]::new))
                .addIngredient('E', new ElementOptionItem())
                .addIngredient('I', new TicksOptionItem("InputTimeout", PlayerSettings::inputTimeout, PlayerSettings::setInputTimeout))
                .addIngredient('A', new BooleanOptionItem("AccountForPing", PlayerSettings::accountForPing, PlayerSettings::setAccountForPing))
                .addIngredient('T', new TicksOptionItem("InputPingThreshold", PlayerSettings::pingThreshold, PlayerSettings::setPingThreshold))
                .addIngredient('C', new TicksOptionItem("InputPingCompensation", PlayerSettings::pingCompensation, PlayerSettings::setPingCompensation))
                .addIngredient('M', new TicksOptionItem("MaxInputPingCompensation", PlayerSettings::maxPingCompensation, PlayerSettings::setMaxPingCompensation))
                .build();

        Window.single()
                .setTitle(ChatUtil.color(config.getString("SettingsMenu.Title")))
                .setGui(gui)
                .open(player);
    }
}
