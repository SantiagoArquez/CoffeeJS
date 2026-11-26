package com.cafeteria.order;

import com.cafeteria.model.Product;
import com.cafeteria.promotion.Promotion;

import java.util.ArrayList;
import java.util.List;

public final class OrderBuilder {

    private String customerName = "Cliente";
    private final List<OrderLine> lines = new ArrayList<>();
    private Promotion promotion;

    public OrderBuilder customer(String name) {
        if (name != null && !name.isBlank()) {
            this.customerName = name.trim();
        }
        return this;
    }

    public OrderBuilder add(Product product, int quantity) {
        lines.add(new OrderLine(product, quantity));
        return this;
    }

    public OrderBuilder add(Product product) {
        return add(product, 1);
    }

    public OrderBuilder promotion(Promotion promotion) {
        this.promotion = promotion;
        return this;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public Order preview() {
        return build(0);
    }

    Order build(long id) {
        if (lines.isEmpty()) {
            throw new IllegalStateException("El pedido debe tener al menos un producto");
        }
        return new Order(id, customerName, lines, promotion);
    }
}
