package com.cafeteria.decorador;

import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;

public final class DecoradorCafeAdicional extends DecoradorProducto {

    public static final String ETIQUETA = "Shot extra";

    public static final BigDecimal PRECIO = Dinero.de(2500);

    public DecoradorCafeAdicional(Producto productoBase) {
        super(productoBase, CategoriaProducto.BEBIDA, ETIQUETA, PRECIO);
    }
}
