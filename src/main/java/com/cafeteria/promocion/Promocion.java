package com.cafeteria.promocion;

import com.cafeteria.pedido.Pedido;

import java.math.BigDecimal;

public interface Promocion {

    String obtenerCodigo();

    String obtenerDescripcion();

    BigDecimal descuentoPara(Pedido pedido);

    default boolean estaActivaAhora() {
        return true;
    }
}
