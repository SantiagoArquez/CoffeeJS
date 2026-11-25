package com.cafeteria.model;

import com.cafeteria.util.Money;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Producto básico del menú, sin personalización.
 *
 * <p><b>Patrón Decorator (rol ConcreteComponent):</b> es el objeto que los
 * decoradores envuelven para añadirle complementos. Es inmutable y valida sus datos
 * al construirse (RNF07): nombre obligatorio, precio no negativo y categoría
 * distinta de {@link ProductCategory#COMBO}.</p>
 */
public final class SimpleProduct implements Product {

    private final String name;
    private final String description;
    private final BigDecimal price;
    private final ProductCategory category;

    /**
     * @param name        nombre obligatorio (se recortan espacios)
     * @param description descripción; si es {@code null} se usa cadena vacía
     * @param price       precio obligatorio, no negativo (se redondea al peso)
     * @param category    categoría obligatoria, no puede ser COMBO
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public SimpleProduct(String name, String description, BigDecimal price, ProductCategory category) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.name = name.trim();
        this.description = description == null ? "" : description;
        this.price = Money.round(Objects.requireNonNull(price, "El precio es obligatorio"));
        this.category = Objects.requireNonNull(category, "La categoría es obligatoria");
        if (this.price.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (category == ProductCategory.COMBO) {
            throw new IllegalArgumentException("Un producto simple no puede ser de categoría COMBO");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public ProductCategory getCategory() {
        return category;
    }
}
