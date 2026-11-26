package com.cafeteria.notificacion;

import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.EstadoPedido;
import com.cafeteria.utilidad.Dinero;

import java.time.Clock;

public final class NotificadorCaja extends NotificadorArea {

    public NotificadorCaja() {
        this(Clock.systemDefaultZone());
    }

    public NotificadorCaja(Clock reloj) {
        super("Caja", reloj);
    }

    @Override
    protected String mensajePara(Pedido pedido, EstadoPedido previo, EstadoPedido actual) {
        return switch (actual) {
            case LISTO -> "Pedido listo, total a cobrar " + Dinero.formatear(pedido.total());
            case ENTREGADO -> "Pedido entregado, venta registrada por " + Dinero.formatear(pedido.total());
            case CANCELADO -> "Pedido cancelado, no se debe cobrar";
            default -> null;
        };
    }

    @Override
    protected String mensajeParaModificacion(Pedido pedido) {
        return "El total cambió, nuevo valor " + Dinero.formatear(pedido.total());
    }
}
