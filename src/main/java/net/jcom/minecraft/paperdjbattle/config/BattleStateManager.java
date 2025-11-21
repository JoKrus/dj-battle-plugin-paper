package net.jcom.minecraft.paperdjbattle.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

import static net.jcom.minecraft.paperdjbattle.config.BattleState.LOBBY;
import static net.jcom.minecraft.paperdjbattle.config.BattleState.RUNNING;

public final class BattleStateManager {
    private static final BattleStateManager instance = new BattleStateManager();
    private static final String STATE_NAME = "state";
    private static final BattleState STATE_DEFAULT = LOBBY;

    private File file;
    private YamlConfiguration yaml;

    private volatile BattleState state;

    private BattleStateManager() {

    }

    public static BattleStateManager get() {
        return instance;
    }

    public void init(Plugin plugin) {
        this.file = new File(plugin.getDataFolder(), "battleGoingOn.yml");

        if (!file.exists()) {
            yaml = new YamlConfiguration();
            state = STATE_DEFAULT;
            yaml.set(STATE_NAME, BattleState.to(state));
            saveFile();
        } else {
            yaml = YamlConfiguration.loadConfiguration(file);
            state = BattleState.from(yaml.getString(STATE_NAME, BattleState.to(STATE_DEFAULT)));
        }
    }

    public boolean isGoingOn() {
        return state == RUNNING;
    }

    public BattleState getState() {
        return state;
    }

    public void setState(BattleState value) {
        state = value;
        yaml.set(STATE_NAME, BattleState.to(value));
        saveFile();
    }

    private void saveFile() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
