package net.jcom.minecraft.paperdjbattle.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public final class BattleState {
    private static final BattleState instance = new BattleState();
    private static final String GOING_ON_NAME = "going-on";
    private static final boolean GOING_ON_DEFAULT = false;

    private File file;
    private YamlConfiguration yaml;

    private volatile boolean goingOn;

    private BattleState() {

    }

    public static BattleState get() {
        return instance;
    }

    public void init(Plugin plugin) {
        this.file = new File(plugin.getDataFolder(), "battleGoingOn.yml");

        if (!file.exists()) {
            yaml = new YamlConfiguration();
            goingOn = GOING_ON_DEFAULT;
            yaml.set(GOING_ON_NAME, goingOn);
            saveFile();
        } else {
            yaml = YamlConfiguration.loadConfiguration(file);
            goingOn = yaml.getBoolean(GOING_ON_NAME, GOING_ON_DEFAULT);
        }
    }

    public boolean isGoingOn() {
        return goingOn;
    }

    public void setGoingOn(boolean value) {
        goingOn = value;
        yaml.set(GOING_ON_NAME, value);
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
