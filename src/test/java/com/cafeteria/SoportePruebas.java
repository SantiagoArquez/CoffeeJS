package com.cafeteria;

import com.cafeteria.ventas.RepositorioVentasMemoria;
import com.cafeteria.ventas.ServicioVentas;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class SoportePruebas {

    private SoportePruebas() {
    }

    public static Clock relojEn(int anio, int mes, int dia, int horaActual, int minuto) {
        ZoneId zona = ZoneId.of("America/Bogota");
        Instant instant = LocalDateTime.of(anio, mes, dia, horaActual, minuto).atZone(zona).toInstant();
        return Clock.fixed(instant, zona);
    }

    public static ServicioVentas ventasMemoria() {
        return new ServicioVentas(new RepositorioVentasMemoria(), relojEn(2026, 11, 25, 10, 0));
    }

    public static final class RelojMutable extends Clock {
        private Instant ahora;
        private final ZoneId zona = ZoneId.of("America/Bogota");

        public RelojMutable(int anio, int mes, int dia, int horaActual, int minuto) {
            this.ahora = LocalDateTime.of(anio, mes, dia, horaActual, minuto).atZone(zona).toInstant();
        }

        public void establecerHora(int horaActual, int minuto) {
            ahora = ahora.atZone(zona).withHour(horaActual).withMinute(minuto).toInstant();
        }

        @Override public ZoneId getZone() { return zona; }
        @Override public Clock withZone(ZoneId z) { return this; }
        @Override public Instant instant() { return ahora; }
    }
}
