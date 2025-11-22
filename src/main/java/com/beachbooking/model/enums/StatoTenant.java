package com.beachbooking.model.enums;

import java.util.Arrays;

/**
 * Stati possibili di un tenant.
 */
public enum StatoTenant {
    TRIAL,
    ACTIVE,
    SUSPENDED,
    EXPIRED;

    public static boolean isActiveLike(StatoTenant stato) {
        return stato == ACTIVE || stato == TRIAL;
    }
}