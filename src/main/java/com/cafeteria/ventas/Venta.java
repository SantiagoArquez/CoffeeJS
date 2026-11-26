package com.cafeteria.ventas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Venta(long id, long idPedido, String cliente, LocalDateTime fechaVenta,
                   BigDecimal subtotal, BigDecimal descuento, BigDecimal total,
                   String codigoPromocion, List<DetalleVenta> articulos, long idCierre) {

    public Venta {
        articulos = List.copyOf(articulos);
    }

    public boolean estaAbierta() {
        return idCierre == 0;
    }

    public Venta cerradoPor(long nuevoIdCierre) {
        return new Venta(id, idPedido, cliente, fechaVenta, subtotal, descuento, total, codigoPromocion, articulos, nuevoIdCierre);
    }
}
