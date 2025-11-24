package net.jcom.minecraft.paperdjbattle.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DataUtils {
    public static void setAndSave(File file, String pathKey, Object data) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set(pathKey, data);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double lerpWithDelay(double currentTime,
                                       double delayTime,
                                       double totalTime,
                                       double startValue,
                                       double endValue) {

        if (currentTime <= delayTime) {
            return startValue;
        }
        if (currentTime >= totalTime) {
            return endValue;
        }
        double alpha = (currentTime - delayTime) / (totalTime - delayTime);
        return startValue * (1.0 - alpha) + endValue * alpha;
    }
}
