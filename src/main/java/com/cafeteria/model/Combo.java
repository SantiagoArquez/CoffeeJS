package com.cafeteria.model;

import com.cafeteria.util.Money;

import java.math.BigDecimal;
import java.util.List;

public final class Combo implements Product {

    private final String name;
    private final String description;
    private final List<Product> items;
    private final int discountPercent;

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

    public String getBaseDescription() {
        return description;
    }

    public List<Product> getItems() {
        return items;
    }

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
