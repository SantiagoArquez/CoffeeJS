package com.cafeteria.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica el manejo de montos (RNF09: consistencia de precios y totales). */
class MoneyTest {

    @Test
    void roundsHalfUpToWholePesos() {
        assertEquals(Money.of(1001), Money.round(new BigDecimal("1000.5")));
        assertEquals(Money.of(1000), Money.round(new BigDecimal("1000.49")));
    }

    @Test
    void zeroAndOfHaveScaleZero() {
        assertEquals(0, Money.ZERO.scale());
        assertEquals(0, Money.of(10).scale());
    }

    @Test
    void formatsWithCurrencySymbol() {
        assertTrue(Money.format(Money.of(12500)).startsWith("$"));
        assertTrue(Money.format(Money.of(12500)).contains("12"));
    }
}
