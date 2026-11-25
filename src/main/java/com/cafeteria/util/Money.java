package com.cafeteria.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utilidades para manejar valores monetarios en pesos colombianos.
 *
 * <p>Todos los montos del sistema se representan como {@link BigDecimal} sin decimales
 * (escala 0), de modo que los totales sean siempre consistentes (RNF09) y no existan
 * errores de redondeo propios de {@code double}.</p>
 */
public final class Money {

    /** Valor cero con escala 0, usado como neutro en sumas y descuentos. */
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(0);

    private Money() {
    }

    /**
     * Crea un monto a partir de un entero en pesos.
     *
     * @param amount valor en pesos
     * @return el monto con escala 0
     */
    public static BigDecimal of(long amount) {
        return BigDecimal.valueOf(amount).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Redondea un valor al peso más cercano (mitad hacia arriba).
     *
     * @param value valor a redondear
     * @return el valor con escala 0
     */
    public static BigDecimal round(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Da formato de moneda colombiana, por ejemplo {@code $12.500}.
     *
     * @param value monto a mostrar
     * @return texto listo para presentar al usuario
     */
    public static String format(BigDecimal value) {
        DecimalFormat df = new DecimalFormat("$#,##0", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));
        return df.format(value);
    }
}
