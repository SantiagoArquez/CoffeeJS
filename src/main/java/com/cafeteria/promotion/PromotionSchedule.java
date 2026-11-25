package com.cafeteria.promotion;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Condición temporal de una promoción: días de la semana y franja horaria en que está
 * vigente (RF12).
 *
 * <p>Si el conjunto de días está vacío aplica todos los días; si no se indica franja
 * horaria aplica todo el día. La franja puede cruzar la medianoche (por ejemplo
 * 22:00 a 02:00): en ese caso el día que se valida es el del instante consultado.</p>
 */
public final class PromotionSchedule {

    private final Set<DayOfWeek> days;
    private final LocalTime from;
    private final LocalTime to;

    private PromotionSchedule(Set<DayOfWeek> days, LocalTime from, LocalTime to) {
        if ((from == null) != (to == null)) {
            throw new IllegalArgumentException("Debe indicarse la hora de inicio y la de fin, o ninguna");
        }
        if (from != null && from.equals(to)) {
            throw new IllegalArgumentException("La hora de inicio y la de fin no pueden ser iguales");
        }
        this.days = days.isEmpty() ? EnumSet.noneOf(DayOfWeek.class) : EnumSet.copyOf(days);
        this.from = from;
        this.to = to;
    }

    /**
     * @param days días en que aplica (vacío = todos)
     * @param from hora de inicio inclusive, o {@code null} para todo el día
     * @param to   hora de fin exclusive, o {@code null} para todo el día
     * @return el horario creado
     * @throws IllegalArgumentException si solo se indica una de las dos horas o son iguales
     */
    public static PromotionSchedule of(Set<DayOfWeek> days, LocalTime from, LocalTime to) {
        return new PromotionSchedule(days, from, to);
    }

    /**
     * @param days días en que aplica durante todo el día
     * @return un horario solo por días
     */
    public static PromotionSchedule onDays(DayOfWeek... days) {
        Set<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
        set.addAll(java.util.Arrays.asList(days));
        return new PromotionSchedule(set, null, null);
    }

    /**
     * @param moment instante a evaluar
     * @return {@code true} si la promoción está vigente en ese día y hora
     */
    public boolean isActive(LocalDateTime moment) {
        if (!days.isEmpty() && !days.contains(moment.getDayOfWeek())) {
            return false;
        }
        if (from == null) {
            return true;
        }
        LocalTime time = moment.toLocalTime();
        return from.isBefore(to)
                ? !time.isBefore(from) && time.isBefore(to)
                : !time.isBefore(from) || time.isBefore(to);
    }

    /** @return texto legible, por ejemplo «lun, mar, mié de 15:00 a 18:00» */
    public String describe() {
        String dayText = days.isEmpty() ? "todos los días"
                : days.stream().sorted(Comparator.naturalOrder())
                .map(d -> d.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-CO")))
                .collect(Collectors.joining(", "));
        return from == null ? dayText : dayText + " de " + from + " a " + to;
    }
}
