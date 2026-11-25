package com.cafeteria.promotion;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.order.Order;
import com.cafeteria.order.OrderLine;
import com.cafeteria.util.Money;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Descuento porcentual aplicado únicamente a los productos que cumplan un criterio
 * (por ejemplo, solo bebidas o solo un producto concreto).
 *
 * <p><b>Patrón Strategy (rol ConcreteStrategy):</b> corresponde a la «promoción por
 * producto» del diseño. Combinada con {@link ScheduledPromotion} permite reglas como
 * «10 % en bebidas de lunes a viernes de 3 a 6 pm».</p>
 */
public final class ProductPromotion implements Promotion {

    private final String code;
    private final String description;
    private final int percent;
    private final Predicate<Product> filter;

    /**
     * @param code        código que digita el usuario
     * @param description texto comercial
     * @param percent     porcentaje entre 1 y 100
     * @param filter      criterio que deben cumplir los productos con descuento
     * @throws IllegalArgumentException si el porcentaje es inválido
     */
    public ProductPromotion(String code, String description, int percent, Predicate<Product> filter) {
        if (percent <= 0 || percent > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 1 y 100");
        }
        this.code = code;
        this.description = description;
        this.percent = percent;
        this.filter = Objects.requireNonNull(filter, "El criterio es obligatorio");
    }

    /**
     * Crea una promoción que descuenta todos los productos de una categoría.
     *
     * @param code        código
     * @param description texto comercial
     * @param percent     porcentaje entre 1 y 100
     * @param category    categoría con descuento
     * @return la promoción creada
     */
    public static ProductPromotion forCategory(String code, String description, int percent, ProductCategory category) {
        return new ProductPromotion(code, description, percent, p -> p.getCategory() == category);
    }

    /**
     * Crea una promoción que descuenta un producto por nombre (incluye sus versiones
     * personalizadas, por ejemplo «Latte + Shot extra»).
     *
     * @param code        código
     * @param description texto comercial
     * @param percent     porcentaje entre 1 y 100
     * @param productName nombre base del producto, sin distinguir mayúsculas
     * @return la promoción creada
     */
    public static ProductPromotion forProductName(String code, String description, int percent, String productName) {
        String wanted = productName.trim().toLowerCase();
        return new ProductPromotion(code, description, percent, p -> {
            String name = p.getName().toLowerCase();
            return name.equals(wanted) || name.startsWith(wanted + " + ");
        });
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public BigDecimal discountFor(Order order) {
        BigDecimal base = Money.ZERO;
        for (OrderLine line : order.getLines()) {
            if (filter.test(line.product())) {
                base = base.add(line.total());
            }
        }
        return Money.round(base.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100)));
    }
}
