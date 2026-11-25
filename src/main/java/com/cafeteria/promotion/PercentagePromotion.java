package com.cafeteria.promotion;

import com.cafeteria.order.Order;
import com.cafeteria.util.Money;

import java.math.BigDecimal;

/**
 * Descuento porcentual sobre el subtotal del pedido, con un monto mínimo opcional.
 *
 * <p><b>Patrón Strategy (rol ConcreteStrategy):</b> encapsula en una clase independiente el algoritmo
 * de cálculo de un tipo de descuento; se puede añadir otra promoción sin modificar las existentes.</p>
 */
public final class PercentagePromotion implements Promotion {

    private final String code;
    private final String description;
    private final int percent;
    private final BigDecimal minSubtotal;

    /**
     * @param code        código que digita el usuario
     * @param description texto comercial
     * @param percent     porcentaje entre 1 y 100
     * @param minSubtotal subtotal mínimo para que aplique ({@code Money.ZERO} si no hay mínimo)
     * @throws IllegalArgumentException si el porcentaje es inválido
     */
    public PercentagePromotion(String code, String description, int percent, BigDecimal minSubtotal) {
        if (percent <= 0 || percent > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 1 y 100");
        }
        this.code = code;
        this.description = description;
        this.percent = percent;
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
        return Money.round(subtotal.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100)));
    }
}
