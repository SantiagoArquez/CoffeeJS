package com.cafeteria.notificacion;

import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.EstadoPedido;

import java.time.Clock;

public final class NotificadorMesero extends NotificadorArea {

    public NotificadorMesero() {
        this(Clock.systemDefaultZone());
    }

    public NotificadorMesero(Clock reloj) {
        super("Mesero", reloj);
    }

    @Override
    protected String mensajePara(Pedido pedido, EstadoPedido previo, EstadoPedido actual) {
        return switch (actual) {
            case LISTO -> "Pedido de " + pedido.obtenerNombreCliente() + " listo para llevar a la mesa";
            case CANCELADO -> "Pedido cancelado";
            default -> null;
        };
    }
}
