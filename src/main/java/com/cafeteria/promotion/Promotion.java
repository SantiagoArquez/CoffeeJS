package com.cafeteria.promotion;

import com.cafeteria.order.Order;

import java.math.BigDecimal;

/**
 * Regla comercial que calcula un descuento para un pedido (RF13).
 *
 * <p><b>Patrón Strategy (rol Strategy):</b> define el contrato común de todas las
 * promociones. El {@link PromotionEngine} depende de esta abstracción y no de las
 * implementaciones concretas (Dependency Inversion). Una promoción que no cumple sus
 * condiciones devuelve simplemente descuento cero.</p>
 */
public interface Promotion {

    /** @return código corto con el que el usuario selecciona la promoción */
    String getCode();

    /** @return texto comercial que describe la promoción y sus condiciones */
    String getDescription();

    /**
     * Calcula el descuento para un pedido.
     *
     * @param order pedido a evaluar
     * @return valor a descontar en pesos; cero si la promoción no aplica
     */
    BigDecimal discountFor(Order order);
}
