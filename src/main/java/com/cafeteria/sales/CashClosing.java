package com.cafeteria.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resultado de un cierre de caja (RF16): resume las ventas acumuladas desde el cierre
 * anterior.
 *
 * @param id         consecutivo del cierre
 * @param closedAt   momento en que se realizó
 * @param cashier    persona que realizó el cierre
 * @param salesCount cantidad de ventas incluidas
 * @param subtotal   suma de subtotales antes de descuentos
 * @param discounts  total de descuentos otorgados
 * @param total      total recaudado (subtotal menos descuentos)
 */
public record CashClosing(long id, LocalDateTime closedAt, String cashier, int salesCount,
                          BigDecimal subtotal, BigDecimal discounts, BigDecimal total) {
}
