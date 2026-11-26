package com.cafeteria.decorador;

import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;

public final class DecoradorLecheAlternativa extends DecoradorProducto {

    public static final String ETIQUETA = "Leche de almendras";

    public static final BigDecimal PRECIO = Dinero.de(2000);

    public DecoradorLecheAlternativa(Producto productoBase) {
        super(productoBase, CategoriaProducto.BEBIDA, ETIQUETA, PRECIO);
    }
}
