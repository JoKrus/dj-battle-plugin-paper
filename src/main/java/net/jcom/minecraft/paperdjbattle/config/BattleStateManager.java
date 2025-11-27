package net.jcom.minecraft.paperdjbattle.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

import static net.jcom.minecraft.paperdjbattle.config.BattleState.LOBBY;
import static net.jcom.minecraft.paperdjbattle.config.BattleState.RUNNING;

public final class BattleStateManager {
    private static final BattleStateManager instance = new BattleStateManager();
    private static final String FILE_NAME = "battleGoingOn.yml";
    private static final String STATE_NAME = "state";
    private static final BattleState STATE_DEFAULT = LOBBY;
    private static final String DURATION_NAME = "going-on-since";
    private static final int DURATION_DEFAULT = 0;

    private File file;
    private YamlConfiguration yaml;

    private volatile BattleState state;
    private volatile int duration;

    private BattleStateManager() {

    }

    public static BattleStateManager get() {
        return instance;
    }

    public void init(Plugin plugin) {
        this.file = new File(plugin.getDataFolder(), FILE_NAME);

        if (!file.exists()) {
            yaml = new YamlConfiguration();
            state = STATE_DEFAULT;
            duration = DURATION_DEFAULT;
            yaml.set(STATE_NAME, BattleState.to(state));
            yaml.set(DURATION_NAME, duration);
            saveFile();
        } else {
            yaml = YamlConfiguration.loadConfiguration(file);
            state = BattleState.from(yaml.getString(STATE_NAME, BattleState.to(STATE_DEFAULT)));
            duration = yaml.getInt(DURATION_NAME, DURATION_DEFAULT);
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
        if (state != RUNNING) {
            duration = 0;
            yaml.set(DURATION_NAME, duration);
        }
        yaml.set(STATE_NAME, BattleState.to(value));
        saveFile();
    }

    public int incrementDuration() {
        setDuration(duration + 1);
        return duration;
    }

    public void setDuration(int seconds) {
        duration = seconds;
        //save to file every 20 seconds
        if (duration % 20 == 0) {
            yaml.set(DURATION_NAME, duration);
            saveFile();
        }
    }

    public int getDuration() {
        return duration;
    }

    private void saveFile() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
