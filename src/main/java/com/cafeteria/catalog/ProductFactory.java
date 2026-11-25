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

    public static Product combo(String name, String description, int discountPercent, Product... items) {
        return new Combo(name, description, List.of(items), discountPercent);
    }
}
