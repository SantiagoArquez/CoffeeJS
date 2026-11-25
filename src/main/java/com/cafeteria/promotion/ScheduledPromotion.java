package com.cafeteria.promotion;

import com.cafeteria.order.Order;
import com.cafeteria.util.Money;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Promoción que solo aplica en ciertos días y horarios (RF12 y RF13).
 *
 * <p><b>Patrón Strategy:</b> es una estrategia que compone a otra. Evalúa su
 * {@link PromotionSchedule} contra la hora del {@link Clock} y, si está vigente, delega el
 * cálculo en la promoción envuelta; si no, el descuento es cero. Equivale a las
 * estrategias «promoción por horario» y «promoción por día» del diseño, sin duplicar
 * código para cada combinación.</p>
 *
 * <p>El reloj se inyecta para poder probar la promoción con fechas fijas.</p>
 */
public final class ScheduledPromotion implements Promotion {

    private final Promotion delegate;
    private final PromotionSchedule schedule;
    private final Clock clock;

    /**
     * @param delegate promoción que calcula el descuento cuando está vigente
     * @param schedule días y horas de vigencia
     * @param clock    reloj con el que se consulta la hora actual
     */
    public ScheduledPromotion(Promotion delegate, PromotionSchedule schedule, Clock clock) {
        this.delegate = Objects.requireNonNull(delegate, "La promoción es obligatoria");
        this.schedule = Objects.requireNonNull(schedule, "El horario es obligatorio");
        this.clock = Objects.requireNonNull(clock, "El reloj es obligatorio");
    }

    /** @return {@code true} si la promoción está vigente en este momento */
    public boolean isActiveNow() {
        return schedule.isActive(LocalDateTime.now(clock));
    }

    @Override
    public String getCode() {
        return delegate.getCode();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription() + " (" + schedule.describe() + ")";
    }

    @Override
    public BigDecimal discountFor(Order order) {
        return isActiveNow() ? delegate.discountFor(order) : Money.ZERO;
    }
}
