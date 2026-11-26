package com.cafeteria.decorador;

import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;

public final class DecoradorSirope extends DecoradorProducto {

    public static final String ETIQUETA = "Sirope de vainilla";

    public static final BigDecimal PRECIO = Dinero.de(1500);

    public DecoradorSirope(Producto productoBase) {
        super(productoBase, CategoriaProducto.BEBIDA, ETIQUETA, PRECIO);
    }
}
