package com.example.auction.enums;

public enum LotStatus {
    STARTED("Started"),
    STOPPED("Stopped"),
    CREATED("Created");

    private final String lotStatus;

    LotStatus(String lotStatus) {
        this.lotStatus = lotStatus;
    }

    public String getLotStatus() {
        return lotStatus;
    }

}

