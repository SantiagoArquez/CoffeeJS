package com.cafeteria.pedido;

public interface ObservadorPedido {

    void alCambiarEstado(Pedido pedido, EstadoPedido previo, EstadoPedido actual);

    default void alModificarPedido(Pedido pedido) {
    }
}
