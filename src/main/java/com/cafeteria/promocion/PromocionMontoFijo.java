package com.cafeteria.promocion;

import com.cafeteria.pedido.Pedido;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;

public final class PromocionMontoFijo implements Promocion {

    private final String codigo;
    private final String descripcion;
    private final BigDecimal monto;
    private final BigDecimal subtotalMinimo;

    public PromocionMontoFijo(String codigo, String descripcion, BigDecimal monto, BigDecimal subtotalMinimo) {
        if (monto.signum() <= 0) {
            throw new IllegalArgumentException("El descuento debe ser positivo");
        }
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.monto = monto;
        this.subtotalMinimo = subtotalMinimo;
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
        BigDecimal subtotal = pedido.subtotal();
        if (subtotal.compareTo(subtotalMinimo) < 0) {
            return Dinero.CERO;
        }
        return monto.min(subtotal);
    }
}
