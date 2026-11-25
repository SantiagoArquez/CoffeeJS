package com.cafeteria.promotion;

import com.cafeteria.model.ProductCategory;
import com.cafeteria.order.Order;
import com.cafeteria.util.Money;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Administra las promociones disponibles y encuentra las que aplican a un pedido
 * (RF12 y RF13).
 *
 * <p><b>Patrón Strategy (rol Context):</b> usa las promociones solo a través de la
 * interfaz {@link Promotion}; no sabe cómo calcula cada una su descuento.</p>
 */
public final class PromotionEngine {

    private final Map<String, Promotion> promotions = new LinkedHashMap<>();

    /**
     * Registra una promoción; si ya existía una con el mismo código la reemplaza.
     *
     * @param promotion promoción a registrar
     * @return este motor, para encadenar llamadas
     */
    public PromotionEngine register(Promotion promotion) {
        promotions.put(promotion.getCode().toUpperCase(), promotion);
        return this;
    }

    /**
     * Elimina una promoción.
     *
     * @param code código de la promoción
     * @return {@code true} si existía y fue eliminada
     */
    public boolean remove(String code) {
        return code != null && promotions.remove(code.trim().toUpperCase()) != null;
    }

    /** @return todas las promociones registradas, estén vigentes o no */
    public Collection<Promotion> all() {
        return promotions.values();
    }

    /**
     * @param code código digitado por el usuario (sin distinguir mayúsculas)
     * @return la promoción, o vacío si no existe
     */
    public Optional<Promotion> findByCode(String code) {
        return code == null ? Optional.empty() : Optional.ofNullable(promotions.get(code.trim().toUpperCase()));
    }

    /**
     * Busca la promoción vigente que más descuento da al pedido.
     *
     * @param order pedido a evaluar
     * @return la mejor promoción, o vacío si ninguna aplica
     */
    public Optional<Promotion> bestFor(Order order) {
        return promotions.values().stream()
                .filter(p -> p.discountFor(order).signum() > 0)
                .max(Comparator.comparing(p -> p.discountFor(order)));
    }

    /**
     * Crea el motor con las promociones iniciales usando el reloj del sistema.
     *
     * @return motor con promociones por defecto
     */
    public static PromotionEngine withDefaults() {
        return withDefaults(Clock.systemDefaultZone());
    }

    /**
     * Crea el motor con las promociones iniciales: tres por monto o cantidad y dos por
     * día y horario, que usan el reloj indicado.
     *
     * @param clock reloj con el que se evalúan las promociones por horario
     * @return motor con promociones por defecto
     */
    public static PromotionEngine withDefaults(Clock clock) {
        Set<DayOfWeek> weekdays = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        Set<DayOfWeek> weekend = Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        return new PromotionEngine()
                .register(new PercentagePromotion("BIENVENIDA10", "10% de descuento en tu compra", 10, Money.ZERO))
                .register(new FixedAmountPromotion("AHORRA5000", "$5.000 de descuento en compras desde $30.000",
                        Money.of(5000), Money.of(30000)))
                .register(new BuyNGetOnePromotion("CAFE3X2", "Lleva 3 bebidas iguales y paga 2",
                        ProductCategory.BEVERAGE, 2))
                .register(new ScheduledPromotion(
                        ProductPromotion.forCategory("HAPPYHOUR", "10% en bebidas", 10, ProductCategory.BEVERAGE),
                        PromotionSchedule.of(weekdays, LocalTime.of(15, 0), LocalTime.of(18, 0)), clock))
                .register(new ScheduledPromotion(
                        ProductPromotion.forCategory("COMBOFINDE", "15% en combos", 15, ProductCategory.COMBO),
                        PromotionSchedule.of(weekend, null, null), clock));
    }
}
