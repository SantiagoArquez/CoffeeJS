package com.cafeteria.model;

import com.cafeteria.util.Money;

import java.math.BigDecimal;
import java.util.List;

/**
 * Combo: conjunto de dos o más productos que se vende como una sola unidad con un
 * descuento porcentual (RF06 y RF07).
 *
 * <p><b>Patrón Builder (rol Product):</b> es el objeto complejo que construye
 * {@link com.cafeteria.combo.ComboBuilder}. El precio nunca se guarda: se calcula en
 * cada consulta sumando los productos y aplicando el descuento, así que siempre
 * refleja los precios vigentes de sus componentes.</p>
 */
public final class Combo implements Product {

    private final String name;
    private final String description;
    private final List<Product> items;
    private final int discountPercent;

    /**
     * Crea un combo validado. Normalmente se obtiene mediante
     * {@link com.cafeteria.combo.ComboPersonalizadoBuilder} o
     * {@link com.cafeteria.catalog.ProductFactory#combo}.
     *
     * @param name            nombre obligatorio
     * @param description     descripción opcional
     * @param items           al menos dos productos
     * @param discountPercent descuento entre 0 y 100
     * @throws IllegalArgumentException si no se cumplen las reglas anteriores
     */
    public Combo(String name, String description, List<Product> items, int discountPercent) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del combo es obligatorio");
        }
        if (items == null || items.size() < 2) {
            throw new IllegalArgumentException("Un combo debe tener al menos dos productos");
        }
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        this.name = name.trim();
        this.description = description == null ? "" : description;
        this.items = List.copyOf(items);
        this.discountPercent = discountPercent;
    }

    /** @return la descripción escrita por el administrador, sin la lista de productos */
    public String getBaseDescription() {
        return description;
    }

    /** @return los productos que componen el combo (lista inmutable) */
    public List<Product> getItems() {
        return items;
    }

    /** @return el descuento porcentual aplicado a la suma de los productos */
    public int getDiscountPercent() {
        return discountPercent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder(description);
        if (!description.isBlank()) {
            sb.append(" - ");
        }
        sb.append("Incluye: ");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(" + ");
            }
            sb.append(items.get(i).getName());
        }
        return sb.toString();
    }

    @Override
    public BigDecimal getPrice() {
        BigDecimal sum = items.stream().map(Product::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Money.round(sum.multiply(BigDecimal.valueOf(100 - discountPercent)).divide(BigDecimal.valueOf(100)));
    }

    @Override
    public ProductCategory getCategory() {
        return ProductCategory.COMBO;
    }
}
