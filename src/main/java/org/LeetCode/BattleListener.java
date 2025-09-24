package org.LeetCode;

public interface BattleListener {
    void onTurn(String attacker, String defender, int damage, boolean critical, double modifier); // :contentReference[oaicite:16]{index=16}
    void onHpChanged(String pokemon, int hpActual);                                             // :contentReference[oaicite:17]{index=17}
    void onBattleEnded(String winner);                                                          // :contentReference[oaicite:18]{index=18}
}

