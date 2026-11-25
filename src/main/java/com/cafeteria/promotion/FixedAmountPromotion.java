package com.cafeteria.promotion;

import com.cafeteria.order.Order;
import com.cafeteria.util.Money;

import java.math.BigDecimal;

/**
 * Descuento de valor fijo cuando el subtotal alcanza un mínimo.
 *
 * <p><b>Patrón Strategy (rol ConcreteStrategy):</b> encapsula en una clase independiente el algoritmo
 * de cálculo de un tipo de descuento; se puede añadir otra promoción sin modificar las existentes.</p>
 */
public final class FixedAmountPromotion implements Promotion {

    private final String code;
    private final String description;
    private final BigDecimal amount;
    private final BigDecimal minSubtotal;

    /**
     * @param code        código que digita el usuario
     * @param description texto comercial
     * @param amount      valor a descontar, positivo
     * @param minSubtotal subtotal mínimo para que aplique
     * @throws IllegalArgumentException si el monto no es positivo
     */
    public FixedAmountPromotion(String code, String description, BigDecimal amount, BigDecimal minSubtotal) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("El descuento debe ser positivo");
        }
        this.code = code;
        this.description = description;
        this.amount = amount;
        this.minSubtotal = minSubtotal;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public BigDecimal discountFor(Order order) {
        BigDecimal subtotal = order.subtotal();
        if (subtotal.compareTo(minSubtotal) < 0) {
            return Money.ZERO;
        }
        return amount.min(subtotal);
    }
}
