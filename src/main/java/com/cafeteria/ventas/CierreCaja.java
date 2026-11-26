package com.cafeteria.ventas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CierreCaja(long id, LocalDateTime fechaCierre, String cajero, int cantidadVentas,
                          BigDecimal subtotal, BigDecimal descuentos, BigDecimal total) {
}
