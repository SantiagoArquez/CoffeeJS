package com.cafeteria.catalog;

import com.cafeteria.model.Combo;
import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.model.SimpleProduct;
import com.cafeteria.util.Money;

import java.util.List;

public final class ProductFactory {

    private ProductFactory() {
    }

    public static Product beverage(String name, String description, long price) {
        return new SimpleProduct(name, description, Money.of(price), ProductCategory.BEVERAGE);
    }

    public static Product food(String name, String description, long price) {
        return new SimpleProduct(name, description, Money.of(price), ProductCategory.FOOD);
    }

    public static Product simple(String name, String description, String price, ProductCategory category) {
        validateDescription(description);
        long amount;
        try {
            amount = Long.parseLong(price == null ? "" : price.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresa un precio entero en pesos, sin separadores");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        return new SimpleProduct(name, description.trim(), Money.of(amount), category);
    }

    public static Product editCombo(Combo combo, String name, String description, String discount) {
        validateDescription(description);
        int percent;
        try {
            percent = Integer.parseInt(discount == null ? "" : discount.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresa un descuento entero entre 0 y 100");
        }
        return new Combo(name, description.trim(), combo.getItems(), percent);
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
    }

    public static Product combo(String name, String description, int discountPercent, Product... items) {
        return new Combo(name, description, List.of(items), discountPercent);
    }
}
