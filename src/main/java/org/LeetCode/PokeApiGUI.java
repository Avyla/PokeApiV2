package org.LeetCode;


import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.net.URL;

public class PokeApiGUI {
    // ==== Componentes del .form (mantén exactamente estos nombres) ====
    private JPanel mainPanel;
    private JLabel Name1;
    private JLabel Imagen1;
    private JProgressBar Life1;
    private JButton Search1;

    private JLabel Name2;
    private JLabel Imagen2;
    private JProgressBar Life2;
    private JButton Search2;

    private JButton Attack1; // Fight!
    private JButton Attack2; // Random P1
    private JButton Attack3; // Random P2
    private JTextArea Consola;
    private JButton Reset;

    // ==== Estado ====
    private final PokeApiClient api = new PokeApiClient();
    private Pokemon player1;
    private Pokemon player2;
    private BattleEngine engine;

    public PokeApiGUI() {
        // Ajustes iniciales de UI
        initUiHints();

        // ==== Cargar P1 por nombre ====
        Search1.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(mainPanel, "Nombre del Pokémon Jugador 1:");
            if (name == null || name.isBlank()) return;
            lockWhileLoading(true);
            api.fetchByName(name.trim())
                    .whenComplete((pk, err) -> SwingUtilities.invokeLater(() -> {
                        lockWhileLoading(false);
                        if (err != null) {
                            log("Error: " + err.getMessage());
                            return;
                        }
                        player1 = pk;
                        paintPokemon(player1, Name1, Imagen1, Life1);
                        updateFightEnabled();
                    }));
        });

        // ==== Cargar P2 por nombre ====
        Search2.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(mainPanel, "Nombre del Pokémon Jugador 2:");
            if (name == null || name.isBlank()) return;
            lockWhileLoading(true);
            api.fetchByName(name.trim())
                    .whenComplete((pk, err) -> SwingUtilities.invokeLater(() -> {
                        lockWhileLoading(false);
                        if (err != null) {
                            log("Error: " + err.getMessage());
                            return;
                        }
                        player2 = pk;
                        paintPokemon(player2, Name2, Imagen2, Life2);
                        updateFightEnabled();
                    }));
        });

        // ==== Random P1 ====
        Attack2.addActionListener(e -> {
            lockWhileLoading(true);
            api.fetchRandom()
                    .whenComplete((pk, err) -> SwingUtilities.invokeLater(() -> {
                        lockWhileLoading(false);
                        if (err != null) { log("Error: " + err.getMessage()); return; }
                        player1 = pk;
                        paintPokemon(player1, Name1, Imagen1, Life1);
                        updateFightEnabled();
                    }));
        });

        // ==== Random P2 ====
        Attack3.addActionListener(e -> {
            lockWhileLoading(true);
            api.fetchRandom()
                    .whenComplete((pk, err) -> SwingUtilities.invokeLater(() -> {
                        lockWhileLoading(false);
                        if (err != null) { log("Error: " + err.getMessage()); return; }
                        player2 = pk;
                        paintPokemon(player2, Name2, Imagen2, Life2);
                        updateFightEnabled();
                    }));
        });

        // ==== Fight! (batalla automática por turnos, no bloquea UI) ====
        Attack1.addActionListener(e -> startBattle());

        // ==== Reset ====
        Reset.addActionListener(e -> resetGame());
    }

    private void initUiHints() {
        // Etiquetas y estados iniciales
        Name1.setText("Jugador 1");
        Name2.setText("Jugador 2");

        Search1.setText("Search P1");
        Search2.setText("Search P2");

        Attack1.setText("Fight!");
        Attack2.setText("Random P1");
        Attack3.setText("Random P2");
        Reset.setText("Reset");

        // Barras de vida configuradas
        setupLifeBar(Life1);
        setupLifeBar(Life2);

        // Auto-scroll del log
        Consola.setEditable(false);
        Consola.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        if (Consola.getParent() instanceof JViewport) {
            DefaultCaret caret = (DefaultCaret) Consola.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }

        updateFightEnabled();
    }

    private void setupLifeBar(JProgressBar bar) {
        bar.setMinimum(0);
        bar.setMaximum(100);
        bar.setValue(0);
        bar.setStringPainted(true);
    }

    private void startBattle() {
        if (player1 == null || player2 == null) {
            log("Ambos jugadores deben tener un Pokémon cargado.");
            return;
        }
        Attack1.setEnabled(false);
        Attack2.setEnabled(false);
        Attack3.setEnabled(false);
        Search1.setEnabled(false);
        Search2.setEnabled(false);

        engine = new BattleEngine(player1, player2, new BattleListener() {
            @Override
            public void onTurn(String attacker, String defender, int damage, boolean critical, double modifier) {
                SwingUtilities.invokeLater(() -> {
                    String modText = modifier > 1.0 ? " (¡SÚPER EFECTIVO!)"
                            : modifier < 1.0 ? " (poco efectivo)"
                            : "";
                    String critText = critical ? " (CRÍTICO)" : "";
                    log(attacker + " atacó a " + defender + " por " + damage + " HP" + critText + modText);
                });
            }
            @Override
            public void onHpChanged(String pokemon, int hpActual) {
                SwingUtilities.invokeLater(() -> {
                    if (player1 != null && pokemon.equalsIgnoreCase(player1.getName())) {
                        Life1.setValue(hpActual);
                        Life1.setString(hpActual + " / " + player1.getHpMax());
                    }
                    if (player2 != null && pokemon.equalsIgnoreCase(player2.getName())) {
                        Life2.setValue(hpActual);
                        Life2.setString(hpActual + " / " + player2.getHpMax());
                    }
                });
            }
            @Override
            public void onBattleEnded(String winner) {
                SwingUtilities.invokeLater(() -> {
                    log("¡Ganador: " + winner + "!");
                    // Rehabilitar controles excepto Fight! (para evitar re-lanzar misma batalla)
                    Attack2.setEnabled(true);
                    Attack3.setEnabled(true);
                    Search1.setEnabled(true);
                    Search2.setEnabled(true);
                });
            }
        });

        // Turnos automáticos cada 700 ms
        engine.autoBattleAsync(700);
    }

    private void paintPokemon(Pokemon p, JLabel nameLbl, JLabel imgLbl, JProgressBar bar) {
        nameLbl.setText(String.format(
                "%s  | Tipo: %s  | HP:%d  ATK:%d  DEF:%d  SPD:%d",
                capitalize(p.getName()),
                p.getTypes().isEmpty() ? "-" : p.getTypes().get(0),
                p.getHpMax(), p.getAttack(), p.getDefense(), p.getSpeed()
        ));
        try {
            ImageIcon ii = new ImageIcon(new URL(p.getSpriteUrl()));
            Image scaled = ii.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            imgLbl.setIcon(new ImageIcon(scaled));
        } catch (Exception ex) {
            imgLbl.setIcon(null);
        }
        bar.setMaximum(p.getHpMax());
        bar.setValue(p.getHp());
        bar.setString(p.getHp() + " / " + p.getHpMax());
    }

    private void updateFightEnabled() {
        boolean ready = (player1 != null && player2 != null);
        Attack1.setEnabled(ready);
    }

    private void lockWhileLoading(boolean loading) {
        Search1.setEnabled(!loading);
        Search2.setEnabled(!loading);
        Attack2.setEnabled(!loading);
        Attack3.setEnabled(!loading);
        Reset.setEnabled(!loading);
        // Fight! depende de tener ambos cargados
        if (!loading) updateFightEnabled();
    }

    private void resetGame() {
        player1 = null;
        player2 = null;
        engine = null;

        Name1.setText("Jugador 1");
        Name2.setText("Jugador 2");
        Imagen1.setIcon(null);
        Imagen2.setIcon(null);

        Life1.setMaximum(100); Life1.setValue(0); Life1.setString("0 / 0");
        Life2.setMaximum(100); Life2.setValue(0); Life2.setString("0 / 0");

        Consola.setText("");

        // Restaurar botones
        Attack1.setEnabled(false); // hasta que haya 2 Pokémon
        Attack2.setEnabled(true);
        Attack3.setEnabled(true);
        Search1.setEnabled(true);
        Search2.setEnabled(true);
    }

    private void log(String msg) {
        Consola.append(msg + "\n");
        // auto-scroll (por si el JTextArea no está en JScrollPane, forzamos caret al final)
        Consola.setCaretPosition(Consola.getDocument().getLength());
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ==== Main de arranque rápido (opcional) ====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pokémon Battle Arena");
            frame.setContentPane(new PokeApiGUI().mainPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setSize(700, 520);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // Si usas el GUI Designer de IntelliJ y necesitas custom components:
    private void createUIComponents() {
        // Deja vacío si todos los componentes ya están en el .form
    }
}

