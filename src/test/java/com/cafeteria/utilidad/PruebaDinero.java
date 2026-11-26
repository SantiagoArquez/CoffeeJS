package com.cafeteria.utilidad;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PruebaDinero {

    @Test
    void redondeaAPesosEnteros() {
        assertEquals(Dinero.de(1001), Dinero.redondear(new BigDecimal("1000.5")));
        assertEquals(Dinero.de(1000), Dinero.redondear(new BigDecimal("1000.49")));
    }

    @Test
    void ceroYDeTienenEscalaCero() {
        assertEquals(0, Dinero.CERO.scale());
        assertEquals(0, Dinero.de(10).scale());
    }

    @Test
    void formateaConSimboloMoneda() {
        assertTrue(Dinero.formatear(Dinero.de(12500)).startsWith("$"));
        assertTrue(Dinero.formatear(Dinero.de(12500)).contains("12"));
    }
}
