package net.jcom.minecraft.paperdjbattle.config;

import org.apache.commons.lang3.tuple.Pair;

public class Defaults {
    private static final int DEFAULT_BATTLE_DURATION = 3600;

    public static final Pair<String, Object> GRACE_PERIOD = Pair.of("timers.grace-period", 30);
    public static final Pair<String, Object> BATTLE_START = Pair.of("timers.battle-start", 10);
    public static final Pair<String, Object> BATTLE_DURATION = Pair.of("timers.battle-duration", DEFAULT_BATTLE_DURATION);
    public static final Pair<String, Object> BATTLE_LOCATION = Pair.of("locations.battle-location", "10000 257 10000");
    public static final Pair<String, Object> BATTLE_LOCATION_SPREAD_RADIUS = Pair.of("locations.battle-location-radius", 15);
    public static final Pair<String, Object> LOBBY_LOCATION = Pair.of("locations.lobby-location", "0 257 0");
    public static final Pair<String, Object> WORLD_BORDER_INIT_WIDTH = Pair.of("world-border.init-width", 2000);
    public static final Pair<String, Object> WORLD_BORDER_END_WIDTH = Pair.of("world-border.end-width", 100);
    public static final Pair<String, Object> WORLD_BORDER_LOBBY_WIDTH = Pair.of("world-border.lobby-width", 300);
    public static final Pair<String, Object> HORIZONTAL_BORDER_START = Pair.of("horizontal-border.start-move", DEFAULT_BATTLE_DURATION / 2);
    public static final Pair<String, Object> HORIZONTAL_BORDER_RENDER_SIZE = Pair.of("horizontal-border.render-size", 5);
    public static final Pair<String, Object> HORIZONTAL_BORDER_RENDER_DISTANCE = Pair.of("horizontal-border.render-distance", 15);
    public static final Pair<String, Object> TEAM_SIZE = Pair.of("teams.size", 2);
}
