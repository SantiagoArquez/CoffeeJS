package com.cafeteria.promocion;

import com.cafeteria.pedido.Pedido;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;

public final class PromocionPorcentaje implements Promocion {

    private final String codigo;
    private final String descripcion;
    private final int porcentaje;
    private final BigDecimal subtotalMinimo;

    public PromocionPorcentaje(String codigo, String descripcion, int porcentaje, BigDecimal subtotalMinimo) {
        if (porcentaje <= 0 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 1 y 100");
        }
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.porcentaje = porcentaje;
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
        return Dinero.redondear(subtotal.multiply(BigDecimal.valueOf(porcentaje)).divide(BigDecimal.valueOf(100)));
    }
}
