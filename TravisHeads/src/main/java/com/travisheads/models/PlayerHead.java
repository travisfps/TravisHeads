package com.travisheads.models;

public class PlayerHead {

    private final String ownerName;
    private final String rarityId;
    private final long timestamp;

    public PlayerHead(String ownerName, String rarityId, long timestamp) {
        this.ownerName = ownerName;
        this.rarityId = rarityId;
        this.timestamp = timestamp;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getRarityId() {
        return rarityId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}