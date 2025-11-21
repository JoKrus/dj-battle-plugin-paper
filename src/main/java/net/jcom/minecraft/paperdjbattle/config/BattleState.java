package net.jcom.minecraft.paperdjbattle.config;

public enum BattleState {
    RUNNING,
    COUNTDOWN,
    LOBBY;

    public static String to(BattleState value) {
        return value.name();
    }

    public static BattleState from(String value) {
        return BattleState.valueOf(value);
    }
}
