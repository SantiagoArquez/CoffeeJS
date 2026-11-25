package com.cafeteria.ui;

import com.cafeteria.order.Order;
import com.cafeteria.order.OrderListener;
import com.cafeteria.order.OrderStatus;

public final class ConsoleNotifier implements OrderListener {

    @Override
    public void onStatusChanged(Order order, OrderStatus previous, OrderStatus current) {
        if (previous == null) {
            System.out.printf("[Notificación] Pedido #%d de %s registrado (%s).%n",
                    order.getId(), order.getCustomerName(), current.getLabel());
        } else {
            System.out.printf("[Notificación] Pedido #%d: %s -> %s.%n",
                    order.getId(), previous.getLabel(), current.getLabel());
        }
    }
}
