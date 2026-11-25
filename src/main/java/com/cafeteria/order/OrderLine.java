package com.cafeteria.order;

import com.cafeteria.model.Product;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderLine(Product product, int quantity) {

    public OrderLine {
        Objects.requireNonNull(product, "El producto es obligatorio");
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }

    public BigDecimal total() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
