package com.cafeteria.ui;

import com.cafeteria.catalog.ProductCatalog;
import com.cafeteria.decorator.ExtraShotDecorator;
import com.cafeteria.decorator.SyrupDecorator;
import com.cafeteria.model.Product;
import com.cafeteria.order.Order;
import com.cafeteria.order.OrderBuilder;
import com.cafeteria.order.OrderService;
import com.cafeteria.promotion.PromotionEngine;

public final class Demo {

    private Demo() {
    }

    public static void run(ProductCatalog catalog, PromotionEngine promotions, OrderService orders) {
        System.out.println("=== DEMO ===\n");

        Product latte = catalog.findByCode("BEB-04").orElseThrow();
        Product americano = catalog.findByCode("BEB-02").orElseThrow();
        Product tostada = catalog.findByCode("ALI-05").orElseThrow();
        Product combo = catalog.findByCode("COM-01").orElseThrow();

        Product latteEspecial = new SyrupDecorator(new ExtraShotDecorator(latte));
        System.out.println("Decorator -> " + latteEspecial.getName() + ": " + latteEspecial.getPrice());

        System.out.println("Composite -> " + combo.getName() + ": " + combo.getPrice() + " (" + combo.getDescription() + ")");

        OrderBuilder builder = new OrderBuilder().customer("Ana")
                .add(latteEspecial)
                .add(americano, 3)
                .add(tostada)
                .add(combo);
        promotions.bestFor(builder.preview()).ifPresent(builder::promotion);
        Order order = orders.place(builder);
        System.out.println("\n" + order.receipt());

        orders.advance(order.getId());
        orders.advance(order.getId());
        orders.advance(order.getId());
        System.out.println("\n" + order.receipt());
    }
}
