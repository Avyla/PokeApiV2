package org.LeetCode;

import javax.swing.*;
import java.util.Random;

public class BattleEngine {
    private final Pokemon p1, p2;
    private final BattleListener listener;
    private final Random rnd = new Random();
    private boolean finished = false;

    public BattleEngine(Pokemon p1, Pokemon p2, BattleListener listener) {
        this.p1 = p1; this.p2 = p2; this.listener = listener;
    }

    // Efectividad simple usando el primer tipo (opcional en requisitos)
    private double typeModifier(Pokemon atk, Pokemon def) {
        if (atk.getTypes().isEmpty() || def.getTypes().isEmpty()) return 1.0;
        String a = atk.getTypes().get(0), d = def.getTypes().get(0);
        // Agua>Fuego; Fuego>Planta; Planta>Agua (x1.3) / inversa x0.7 / resto x1.0
        if (a.equals("water") && d.equals("fire")) return 1.3;
        if (a.equals("fire")  && d.equals("grass")) return 1.3;
        if (a.equals("grass") && d.equals("water")) return 1.3;
        if (a.equals("fire")  && d.equals("water")) return 0.7;
        if (a.equals("grass") && d.equals("fire"))  return 0.7;
        if (a.equals("water") && d.equals("grass")) return 0.7;
        return 1.0;
    }

    private int computeDamage(Pokemon atk, Pokemon def) {
        double r1 = rnd.nextDouble(); // 0..1
        double r2 = rnd.nextDouble();
        double base = (atk.getAttack() * r1) - (def.getDefense() * r2); // ejemplo de fórmula simple :contentReference[oaicite:19]{index=19}
        boolean crit = rnd.nextDouble() < 0.10; // 10% crítico x1.5 :contentReference[oaicite:20]{index=20}
        double mod = typeModifier(atk, def);    // efectividad opcional :contentReference[oaicite:21]{index=21}
        double result = base * (crit ? 1.5 : 1.0) * mod;
        int dmg = Math.max(1, (int)Math.floor(result));
        // Notifica turno y calcula HP
        def.setHp(def.getHp() - dmg); // HP no negativo :contentReference[oaicite:22]{index=22}
        listener.onTurn(atk.getName(), def.getName(), dmg, crit, mod);
        listener.onHpChanged(def.getName(), def.getHp());
        return dmg;
    }

    public void playOneTurn() {
        if (finished) return;
        Pokemon first = p1, second = p2;
        if (p1.getSpeed() == p2.getSpeed()) {
            if (new Random().nextBoolean()) { first = p2; second = p1; } // empate → aleatorio :contentReference[oaicite:23]{index=23}
        } else if (p1.getSpeed() < p2.getSpeed()) {
            first = p2; second = p1; // mayor Speed inicia
        }
        computeDamage(first, second);
        if (second.getHp() <= 0) { end(first); return; }

        computeDamage(second, first);
        if (first.getHp() <= 0) { end(second); }
    }

    public void autoBattleAsync(int msBetweenTurns) {
        // No bloquear UI: usa SwingWorker para ciclo de turnos
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                while (!finished) {
                    playOneTurn();
                    try { Thread.sleep(msBetweenTurns); } catch (InterruptedException ignored) {}
                }
                return null;
            }
        };
        worker.execute();
    }

    private void end(Pokemon winner) {
        finished = true;
        listener.onBattleEnded(winner.getName());
    }

    public boolean isFinished() { return finished; }
}

