package com.cafeteria;

import com.cafeteria.sales.InMemorySalesRepository;
import com.cafeteria.sales.SalesService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Utilidades compartidas por las pruebas: relojes fijos y servicios en memoria. */
public final class TestSupport {

    private TestSupport() {
    }

    /** Reloj fijo en la fecha y hora indicadas. */
    public static Clock clockAt(int year, int month, int day, int hour, int minute) {
        ZoneId zone = ZoneId.of("America/Bogota");
        Instant instant = LocalDateTime.of(year, month, day, hour, minute).atZone(zone).toInstant();
        return Clock.fixed(instant, zone);
    }

    /** Servicio de ventas en memoria con reloj fijo (miércoles 25/11/2026, 10:00). */
    public static SalesService memorySales() {
        return new SalesService(new InMemorySalesRepository(), clockAt(2026, 11, 25, 10, 0));
    }
}
