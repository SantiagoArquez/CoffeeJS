package com.cafeteria.notificacion;

import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.EstadoPedido;

import java.time.Clock;

public final class NotificadorCocina extends NotificadorArea {

    public NotificadorCocina() {
        this(Clock.systemDefaultZone());
    }

    public NotificadorCocina(Clock reloj) {
        super("Cocina", reloj);
    }

    @Override
    protected String mensajePara(Pedido pedido, EstadoPedido previo, EstadoPedido actual) {
        return switch (actual) {
            case CREADO -> "Nuevo pedido de " + pedido.obtenerNombreCliente() + " por preparar";
            case CANCELADO -> "Pedido cancelado, suspender la preparación";
            default -> null;
        };
    }

    @Override
    protected String mensajeParaModificacion(Pedido pedido) {
        return "El pedido fue modificado, revisar el nuevo contenido";
    }
}
