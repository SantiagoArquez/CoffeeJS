package com.cafeteria.promocion;

import com.cafeteria.pedido.Pedido;

import java.math.BigDecimal;

public final class PromocionAplicada implements Promocion {

    private final String codigo;
    private final String descripcion;
    private final BigDecimal monto;

    public PromocionAplicada(String codigo, String descripcion, BigDecimal monto) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.monto = monto;
    }

    @Override
    public String obtenerCodigo() {
        return codigo;
    }

    @Override
    public String obtenerDescripcion() {
        return descripcion;
    }

    @Override
    public BigDecimal descuentoPara(Pedido pedido) {
        return monto;
    }
}
