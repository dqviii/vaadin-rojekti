package com.tonip.movie.domain;

public enum TargetAudience {
    KIDS("Kids"),
    TEEN("Teen"),
    ADULT("Adult"),
    FAMILY("Family"),
    GENERAL("General");

    private final String displayName;

    TargetAudience(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
