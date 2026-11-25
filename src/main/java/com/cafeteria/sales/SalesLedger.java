package com.cafeteria.sales;

import com.cafeteria.order.Order;
import com.cafeteria.order.OrderListener;
import com.cafeteria.order.OrderStatus;

/**
 * Observador que convierte en venta cada pedido que llega al estado «Entregado» (RF15).
 *
 * <p><b>Patrón Observer (rol ConcreteObserver):</b> así el registro de ventas ocurre sin
 * que {@code OrderService} conozca el módulo de ventas ni las interfaces tengan que
 * acordarse de registrar nada. Los pedidos cancelados no generan venta.</p>
 */
public final class SalesLedger implements OrderListener {

    private final SalesService sales;

    /**
     * @param sales servicio donde se registran las ventas
     */
    public SalesLedger(SalesService sales) {
        this.sales = sales;
    }

    @Override
    public void onStatusChanged(Order order, OrderStatus previous, OrderStatus current) {
        if (current == OrderStatus.DELIVERED) {
            sales.record(order);
        }
    }
}
