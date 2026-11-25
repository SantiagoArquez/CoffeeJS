package com.cafeteria.sales;

import java.math.BigDecimal;

/**
 * Línea de una venta: cuántas unidades de un producto se vendieron y a qué precio.
 *
 * @param name      nombre del producto tal como aparecía en el pedido (incluye complementos)
 * @param quantity  unidades vendidas
 * @param unitPrice precio unitario al momento de la venta
 */
public record SaleItem(String name, int quantity, BigDecimal unitPrice) {

    /** @return valor de la línea: precio unitario por cantidad */
    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
