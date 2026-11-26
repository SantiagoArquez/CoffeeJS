package com.cafeteria.decorador;

import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;

public final class DecoradorQuesoAdicional extends DecoradorProducto {

    public static final String ETIQUETA = "Queso extra";

    public static final BigDecimal PRECIO = Dinero.de(2500);

    public DecoradorQuesoAdicional(Producto productoBase) {
        super(productoBase, CategoriaProducto.ALIMENTO, ETIQUETA, PRECIO);
    }
}
