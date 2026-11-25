package com.cafeteria.notification;

import com.cafeteria.order.Order;
import com.cafeteria.order.OrderStatus;
import com.cafeteria.util.Money;

import java.time.Clock;

/**
 * Observador del área de caja: se entera cuando un pedido está listo para cobrar, cuando
 * se entrega (y por tanto se registra la venta) y cuando se cancela.
 */
public final class CashierNotifier extends RoleNotifier {

    /** Crea el notificador con el reloj del sistema. */
    public CashierNotifier() {
        this(Clock.systemDefaultZone());
    }

    /** @param clock reloj para fechar los avisos */
    public CashierNotifier(Clock clock) {
        super("Caja", clock);
    }

    @Override
    protected String messageFor(Order order, OrderStatus previous, OrderStatus current) {
        return switch (current) {
            case READY -> "Pedido listo, total a cobrar " + Money.format(order.total());
            case DELIVERED -> "Pedido entregado, venta registrada por " + Money.format(order.total());
            case CANCELLED -> "Pedido cancelado, no se debe cobrar";
            default -> null;
        };
    }

    @Override
    protected String messageForModification(Order order) {
        return "El total cambió, nuevo valor " + Money.format(order.total());
    }
}
