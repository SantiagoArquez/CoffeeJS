package com.cafeteria.notification;

import com.cafeteria.order.Order;
import com.cafeteria.order.OrderStatus;

import java.time.Clock;

/**
 * Observador del área de meseros: se entera cuando un pedido está listo para llevar a la
 * mesa y cuando se cancela.
 */
public final class WaiterNotifier extends RoleNotifier {

    /** Crea el notificador con el reloj del sistema. */
    public WaiterNotifier() {
        this(Clock.systemDefaultZone());
    }

    /** @param clock reloj para fechar los avisos */
    public WaiterNotifier(Clock clock) {
        super("Mesero", clock);
    }

    @Override
    protected String messageFor(Order order, OrderStatus previous, OrderStatus current) {
        return switch (current) {
            case READY -> "Pedido de " + order.getCustomerName() + " listo para llevar a la mesa";
            case CANCELLED -> "Pedido cancelado";
            default -> null;
        };
    }
}
