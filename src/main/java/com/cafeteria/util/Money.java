package com.cafeteria.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class Money {

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(0);

    private Money() {
    }

    public static BigDecimal of(long amount) {
        return BigDecimal.valueOf(amount).setScale(0, RoundingMode.HALF_UP);
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    public static String format(BigDecimal value) {
        DecimalFormat df = new DecimalFormat("$#,##0", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));
        return df.format(value);
    }
}
