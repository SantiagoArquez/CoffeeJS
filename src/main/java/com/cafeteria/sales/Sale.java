package com.cafeteria.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Venta registrada cuando un pedido es entregado (RF15). Es una fotografía inmutable de
 * los valores del pedido en ese momento: aunque el menú cambie después, la venta conserva
 * los precios con que se cobró.
 *
 * @param id            identificador propio de la venta
 * @param orderId       número del pedido que la originó
 * @param customer      nombre del cliente
 * @param soldAt        fecha y hora de la venta
 * @param subtotal      suma de las líneas antes de promociones
 * @param discount      descuento aplicado
 * @param total         valor cobrado
 * @param promotionCode código de la promoción aplicada, o cadena vacía si no hubo
 * @param items         productos vendidos
 * @param closingId     número del cierre de caja que la incluyó, o 0 si aún está abierta
 */
public record Sale(long id, long orderId, String customer, LocalDateTime soldAt,
                   BigDecimal subtotal, BigDecimal discount, BigDecimal total,
                   String promotionCode, List<SaleItem> items, long closingId) {

    /** Copia defensiva de la lista de productos. */
    public Sale {
        items = List.copyOf(items);
    }

    /** @return {@code true} si la venta todavía no pertenece a ningún cierre de caja */
    public boolean isOpen() {
        return closingId == 0;
    }

    /**
     * @param newClosingId número del cierre que incluye la venta
     * @return una copia de la venta marcada como cerrada
     */
    public Sale closedBy(long newClosingId) {
        return new Sale(id, orderId, customer, soldAt, subtotal, discount, total, promotionCode, items, newClosingId);
    }
}
