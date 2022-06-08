package com.itsamsung.traintracks;

import android.support.annotation.NonNull;

public enum CellType {
    RAIL, EMPTY;

    public int toInt(){
        switch (this) {
            case RAIL: return 1;
            case EMPTY: return 0;
            default: return -1;
        }
    }
}
