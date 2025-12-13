package com.travisheads.models;

public class Rarity {

    private final String id;
    private final String displayName;
    private final double chance;
    private final String color;

    public Rarity(String id, String displayName, double chance, String color) {
        this.id = id;
        this.displayName = displayName;
        this.chance = chance;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getChance() {
        return chance;
    }

    public String getColor() {
        return color;
    }
}