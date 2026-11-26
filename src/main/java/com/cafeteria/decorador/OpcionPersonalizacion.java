package com.cafeteria.decorador;

import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

public record OpcionPersonalizacion(String etiqueta, BigDecimal precio, CategoriaProducto categoria,
                                  UnaryOperator<Producto> aplicador) {

    public Producto aplicarA(Producto producto) {
        return aplicador.apply(producto);
    }
}
