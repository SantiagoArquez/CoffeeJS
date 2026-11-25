package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

public record CustomizationOption(String label, BigDecimal price, ProductCategory category,
                                  UnaryOperator<Product> applier) {

    public Product applyTo(Product product) {
        return applier.apply(product);
    }
}
