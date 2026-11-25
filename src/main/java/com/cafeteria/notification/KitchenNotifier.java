package com.cafeteria.notification;

import com.cafeteria.order.Order;
import com.cafeteria.order.OrderStatus;

import java.time.Clock;

/**
 * Observador del área de cocina: se entera de los pedidos nuevos que debe preparar, de
 * las modificaciones al contenido y de las cancelaciones.
 */
public final class KitchenNotifier extends RoleNotifier {

    /** Crea el notificador con el reloj del sistema. */
    public KitchenNotifier() {
        this(Clock.systemDefaultZone());
    }

    /** @param clock reloj para fechar los avisos */
    public KitchenNotifier(Clock clock) {
        super("Cocina", clock);
    }

    @Override
    protected String messageFor(Order order, OrderStatus previous, OrderStatus current) {
        return switch (current) {
            case CREATED -> "Nuevo pedido de " + order.getCustomerName() + " por preparar";
            case CANCELLED -> "Pedido cancelado, suspender la preparación";
            default -> null;
        };
    }

    @Override
    protected String messageForModification(Order order) {
        return "El pedido fue modificado, revisar el nuevo contenido";
    }
}
