package net.jcom.minecraft.paperdjbattle.config;

public enum BattleState {
    RUNNING,
    COUNTDOWN,
    COUNTDOWN_AFTER_TP,
    COOLDOWN_AFTER_WIN,
    LOBBY;

    public static String to(BattleState value) {
        return value.name();
    }

    public static BattleState from(String value) {
        return BattleState.valueOf(value);
    }
}
