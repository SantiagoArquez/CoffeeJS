package com.cafeteria.interfaz;

import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.ObservadorPedido;
import com.cafeteria.pedido.EstadoPedido;

public final class NotificadorConsola implements ObservadorPedido {

    @Override
    public void alCambiarEstado(Pedido pedido, EstadoPedido previo, EstadoPedido actual) {
        if (previo == null) {
            System.out.printf("[Notificación] Pedido #%d de %s registrado (%s).%n",
                    pedido.obtenerId(), pedido.obtenerNombreCliente(), actual.obtenerEtiqueta());
        } else {
            System.out.printf("[Notificación] Pedido #%d: %s -> %s.%n",
                    pedido.obtenerId(), previo.obtenerEtiqueta(), actual.obtenerEtiqueta());
        }
    }
}
