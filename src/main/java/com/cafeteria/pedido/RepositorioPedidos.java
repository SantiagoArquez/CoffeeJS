package com.cafeteria.pedido;

import java.util.Collection;
import java.util.List;

public interface RepositorioPedidos {

    List<Pedido> cargarTodos();

    void guardarTodos(Collection<Pedido> pedidos);

    static RepositorioPedidos ninguno() {
        return new RepositorioPedidos() {
            @Override
            public List<Pedido> cargarTodos() {
                return List.of();
            }

            @Override
            public void guardarTodos(Collection<Pedido> pedidos) {
            }
        };
    }
}
