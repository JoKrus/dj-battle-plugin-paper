package net.jcom.minecraft.paperdjbattle.event.data;

import net.jcom.minecraft.paperdjbattle.commands.BattleCommand;
import net.jcom.minecraft.paperdjbattle.config.Defaults;
import net.jcom.minecraft.paperdjbattle.config.DefaultsManager;
import org.bukkit.Location;

public record BattleData(String name, String category, Location battleCenter, int durationSec, int worldSizeStart,
                         int worldSizeEnd,
                         int maxTeamSize) {
    public static BattleData getBattleData(String name, String category) {
        var strLoc = DefaultsManager.<String>getValue(Defaults.BATTLE_LOCATION);
        var loc = BattleCommand.getLocation(strLoc);

        return new BattleData(name,
                category,
                loc,
                DefaultsManager.getValue(Defaults.BATTLE_DURATION),
                DefaultsManager.getValue(Defaults.WORLD_BORDER_INIT_WIDTH),
                DefaultsManager.getValue(Defaults.WORLD_BORDER_END_WIDTH),
                DefaultsManager.getValue(Defaults.TEAM_SIZE)
        );
    }
}
