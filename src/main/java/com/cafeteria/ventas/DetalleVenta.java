package com.cafeteria.ventas;

import java.math.BigDecimal;

public record DetalleVenta(String nombre, int cantidad, BigDecimal precioUnitario) {

    public BigDecimal total() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
