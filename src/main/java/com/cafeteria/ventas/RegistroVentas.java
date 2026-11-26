package com.cafeteria.ventas;

import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.ObservadorPedido;
import com.cafeteria.pedido.EstadoPedido;

public final class RegistroVentas implements ObservadorPedido {

    private final ServicioVentas ventas;

    public RegistroVentas(ServicioVentas ventas) {
        this.ventas = ventas;
    }

    @Override
    public void alCambiarEstado(Pedido pedido, EstadoPedido previo, EstadoPedido actual) {
        if (actual == EstadoPedido.ENTREGADO) {
            ventas.registrarVenta(pedido);
        }
    }
}
