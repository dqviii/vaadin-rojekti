package com.tonip.movie.domain;

public enum ScreenType {
    STANDARD_2D("Standard 2D"),
    IMAX("IMAX"),
    IMAX_3D("IMAX 3D"),
    DOLBY_ATMOS("Dolby Atmos"),
    FOUR_DX("4DX");

    private final String displayName;

    ScreenType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
