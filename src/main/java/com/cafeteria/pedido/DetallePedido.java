package com.cafeteria.pedido;

import com.cafeteria.modelo.Producto;

import java.math.BigDecimal;
import java.util.Objects;

public record DetallePedido(Producto producto, int cantidad) {

    public DetallePedido {
        Objects.requireNonNull(producto, "El producto es obligatorio");
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }

    public BigDecimal total() {
        return producto.obtenerPrecio().multiply(BigDecimal.valueOf(cantidad));
    }
}
