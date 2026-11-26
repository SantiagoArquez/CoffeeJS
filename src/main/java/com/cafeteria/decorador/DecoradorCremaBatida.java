package com.cafeteria.decorador;

import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;

public final class DecoradorCremaBatida extends DecoradorProducto {

    public static final String ETIQUETA = "Crema batida";

    public static final BigDecimal PRECIO = Dinero.de(1800);

    public DecoradorCremaBatida(Producto productoBase) {
        super(productoBase, CategoriaProducto.BEBIDA, ETIQUETA, PRECIO);
    }
}
