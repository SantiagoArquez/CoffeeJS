package com.cafeteria.utilidad;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class Dinero {

    public static final BigDecimal CERO = BigDecimal.ZERO.setScale(0);

    private Dinero() {
    }

    public static BigDecimal de(long monto) {
        return BigDecimal.valueOf(monto).setScale(0, RoundingMode.HALF_UP);
    }

    public static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(0, RoundingMode.HALF_UP);
    }

    public static String formatear(BigDecimal valor) {
        DecimalFormat formatoFecha = new DecimalFormat("$#,##0", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));
        return formatoFecha.format(valor);
    }
}
