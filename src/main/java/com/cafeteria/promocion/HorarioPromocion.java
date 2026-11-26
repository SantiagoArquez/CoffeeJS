package com.cafeteria.promocion;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class HorarioPromocion {

    private final Set<DayOfWeek> dias;
    private final LocalTime desde;
    private final LocalTime hasta;

    private HorarioPromocion(Set<DayOfWeek> dias, LocalTime desde, LocalTime hasta) {
        if ((desde == null) != (hasta == null)) {
            throw new IllegalArgumentException("Debe indicarse la hora de inicio y la de fin, o ninguna");
        }
        if (desde != null && desde.equals(hasta)) {
            throw new IllegalArgumentException("La hora de inicio y la de fin no pueden ser iguales");
        }
        this.dias = dias.isEmpty() ? EnumSet.noneOf(DayOfWeek.class) : EnumSet.copyOf(dias);
        this.desde = desde;
        this.hasta = hasta;
    }

    public static HorarioPromocion de(Set<DayOfWeek> dias, LocalTime desde, LocalTime hasta) {
        return new HorarioPromocion(dias, desde, hasta);
    }

    public static HorarioPromocion enDias(DayOfWeek... dias) {
        Set<DayOfWeek> conjunto = EnumSet.noneOf(DayOfWeek.class);
        conjunto.addAll(java.util.Arrays.asList(dias));
        return new HorarioPromocion(conjunto, null, null);
    }

    public boolean estaActiva(LocalDateTime momento) {
        if (!dias.isEmpty() && !dias.contains(momento.getDayOfWeek())) {
            return false;
        }
        if (desde == null) {
            return true;
        }
        LocalTime hora = momento.toLocalTime();
        return desde.isBefore(hasta)
                ? !hora.isBefore(desde) && hora.isBefore(hasta)
                : !hora.isBefore(desde) || hora.isBefore(hasta);
    }

    public String describir() {
        String textoDias = dias.isEmpty() ? "todos los días"
                : dias.stream().sorted(Comparator.naturalOrder())
                .map(d -> d.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-CO")))
                .collect(Collectors.joining(", "));
        return desde == null ? textoDias : textoDias + " de " + desde + " a " + hasta;
    }
}
