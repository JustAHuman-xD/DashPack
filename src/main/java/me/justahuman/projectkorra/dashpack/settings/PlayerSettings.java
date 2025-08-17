package me.justahuman.projectkorra.dashpack.settings;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import lombok.Setter;
import me.justahuman.projectkorra.dashpack.DashPack;
import me.justahuman.projectkorra.dashpack.ability.DashAbility;
import me.justahuman.projectkorra.dashpack.util.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

@Setter
public class PlayerSettings {
    private static final NamespacedKey SETTINGS_KEY = DashPack.key("settings");

    private String element;
    private int inputTimeout = -1;

    private Boolean accountForPing;
    private int pingThreshold = -1;
    private int pingCompensation = -1;
    private int maxPingCompensation = -1;

    public String element() {
        return element;
    }

    public Element element(BendingPlayer bPlayer) {
        if (element == null) {
            return Utils.getFirstDashElement(bPlayer);
        }
        Element elem = Element.getElement(element);
        return elem != null && bPlayer.hasElement(elem) && DashAbility.get(elem) != null ? elem : Utils.getFirstDashElement(bPlayer);
    }

    public int inputTimeout() {
        return inputTimeout(PlayerSettings.def());
    }

    public int inputTimeout(PlayerSettings def) {
        return inputTimeout != -1 ? inputTimeout : def.inputTimeout;
    }

    public boolean accountForPing() {
        return accountForPing(PlayerSettings.def());
    }

    public boolean accountForPing(PlayerSettings def) {
        return accountForPing != null ? accountForPing : def.accountForPing;
    }

    public int pingThreshold() {
        return pingThreshold(PlayerSettings.def());
    }

    public int pingThreshold(PlayerSettings def) {
        return pingThreshold != -1 ? pingThreshold : def.pingThreshold;
    }

    public int pingCompensation() {
        return pingCompensation(PlayerSettings.def());
    }

    public int pingCompensation(PlayerSettings def) {
        return pingCompensation != -1 ? pingCompensation : def.pingCompensation;
    }

    public int maxPingCompensation() {
        return maxPingCompensation(PlayerSettings.def());
    }

    public int maxPingCompensation(PlayerSettings def) {
        return maxPingCompensation != -1 ? maxPingCompensation : def.maxPingCompensation;
    }

    public PlayerSettings save(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(SETTINGS_KEY, PDCType.INSTANCE, this);
        return this;
    }

    private static class PDCType implements PersistentDataType<PersistentDataContainer, PlayerSettings> {
        private static final NamespacedKey ELEMENT_KEY = DashPack.key("element");
        private static final NamespacedKey INPUT_TIMEOUT_KEY = DashPack.key("inputTimeout");
        private static final NamespacedKey ACCOUNT_FOR_PING_KEY = DashPack.key("accountForPing");
        private static final NamespacedKey PING_THRESHOLD_KEY = DashPack.key("pingThreshold");
        private static final NamespacedKey PING_COMPENSATION_KEY = DashPack.key("pingCompensation");
        private static final NamespacedKey MAX_PING_COMPENSATION_KEY = DashPack.key("maxPingCompensation");
        private static final PDCType INSTANCE = new PDCType();

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull PlayerSettings complex, @NotNull PersistentDataAdapterContext context) {
            PlayerSettings def = PlayerSettings.def();
            PersistentDataContainer pdc = context.newPersistentDataContainer();
            if (complex.element != null) {
                pdc.set(ELEMENT_KEY, PersistentDataType.STRING, complex.element);
            }
            if (complex.inputTimeout != -1 && complex.inputTimeout != def.inputTimeout) {
                pdc.set(INPUT_TIMEOUT_KEY, PersistentDataType.INTEGER, complex.inputTimeout);
            }
            if (complex.accountForPing != null && complex.accountForPing != def.accountForPing) {
                pdc.set(ACCOUNT_FOR_PING_KEY, PersistentDataType.BOOLEAN, complex.accountForPing);
            }
            if (complex.pingThreshold != -1 && complex.pingThreshold != def.pingThreshold) {
                pdc.set(PING_THRESHOLD_KEY, PersistentDataType.INTEGER, complex.pingThreshold);
            }
            if (complex.pingCompensation != -1 && complex.pingCompensation != def.pingCompensation) {
                pdc.set(PING_COMPENSATION_KEY, PersistentDataType.INTEGER, complex.pingCompensation);
            }
            if (complex.maxPingCompensation != -1 && complex.maxPingCompensation != def.maxPingCompensation) {
                pdc.set(MAX_PING_COMPENSATION_KEY, PersistentDataType.INTEGER, complex.maxPingCompensation);
            }
            return pdc;
        }

        @Override
        public @NotNull PlayerSettings fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            PlayerSettings settings = new PlayerSettings();
            settings.element = primitive.get(ELEMENT_KEY, PersistentDataType.STRING);
            settings.inputTimeout = primitive.getOrDefault(INPUT_TIMEOUT_KEY, PersistentDataType.INTEGER, -1);
            settings.accountForPing = primitive.get(ACCOUNT_FOR_PING_KEY, PersistentDataType.BOOLEAN);
            settings.pingThreshold = primitive.getOrDefault(PING_THRESHOLD_KEY, PersistentDataType.INTEGER, -1);
            settings.pingCompensation = primitive.getOrDefault(PING_COMPENSATION_KEY, PersistentDataType.INTEGER, -1);
            settings.maxPingCompensation = primitive.getOrDefault(MAX_PING_COMPENSATION_KEY, PersistentDataType.INTEGER, -1);
            return settings;
        }

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<PlayerSettings> getComplexType() {
            return PlayerSettings.class;
        }
    }

    public static PlayerSettings get(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (pdc.has(SETTINGS_KEY, PDCType.INSTANCE)) {
            return pdc.get(SETTINGS_KEY, PDCType.INSTANCE);
        }
        return new PlayerSettings().save(player);
    }

    public static PlayerSettings def() {
        PlayerSettings settings = new PlayerSettings();
        FileConfiguration config = DashPack.instance.getConfig();
        settings.inputTimeout = config.getInt("PlayerSettingDefaults.InputTimeout", 3);
        settings.accountForPing = config.getBoolean("PlayerSettingDefaults.AccountForPing", true);
        settings.pingThreshold = config.getInt("PlayerSettingDefaults.InputPingThreshold", 40);
        settings.pingCompensation = config.getInt("PlayerSettingDefaults.InputPingCompensation", 1);
        settings.maxPingCompensation = config.getInt("PlayerSettingDefaults.MaxInputPingCompensation", 4);
        return settings;
    }
}
