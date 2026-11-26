package com.cafeteria.notificacion;

import java.time.LocalDateTime;

public record Notificacion(long idPedido, String destinatario, String mensaje, LocalDateTime fechaCreacion) {

    @Override
    public String toString() {
        return "[" + destinatario + "] Pedido #" + idPedido + ": " + mensaje;
    }
}
