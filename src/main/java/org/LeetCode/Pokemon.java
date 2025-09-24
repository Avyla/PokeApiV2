package org.LeetCode;

import java.util.List;

public class Pokemon {
    private final String name;
    private final List<String> types; // tipo[0] es el principal
    private final int hpMax, attack, defense, speed;
    private final String spriteUrl;
    private int hp; // HP actual

    public Pokemon(String name, List<String> types, int hp, int attack, int defense, int speed, String spriteUrl) {
        this.name = name;
        this.types = types;
        this.hpMax = hp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.spriteUrl = spriteUrl;
        this.hp = hp;
    }

    public String getName() { return name; }
    public List<String> getTypes() { return types; }
    public int getHpMax() { return hpMax; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public String getSpriteUrl() { return spriteUrl; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = Math.max(0, Math.min(hpMax, hp)); } // HP no negativo (ni > max) :contentReference[oaicite:13]{index=13}
}

