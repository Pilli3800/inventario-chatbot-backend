package com.pilli3800.inventario.util;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RucValidator {

    public static boolean isValid(String ruc) {
        if (ruc == null || !ruc.matches("\\d{11}")) {
            return false;
        }

        int[] factors = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int sum = 0;

        for (int i = 0; i < factors.length; i++) {
            sum += Character.getNumericValue(ruc.charAt(i)) * factors[i];
        }

        int mod = 11 - (sum % 11);
        int checkDigit = switch (mod) {
            case 10 -> 0;
            case 11 -> 1;
            default -> mod;
        };

        return checkDigit == Character.getNumericValue(ruc.charAt(10));
    }
}
