package com.cafeteria.promotion;

import com.cafeteria.model.ProductCategory;
import com.cafeteria.order.Order;
import com.cafeteria.util.Money;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class PromotionEngine {

    private final Map<String, Promotion> promotions = new LinkedHashMap<>();

    public PromotionEngine register(Promotion promotion) {
        promotions.put(promotion.getCode().toUpperCase(), promotion);
        return this;
    }

    public Collection<Promotion> all() {
        return promotions.values();
    }

    public Optional<Promotion> findByCode(String code) {
        return code == null ? Optional.empty() : Optional.ofNullable(promotions.get(code.trim().toUpperCase()));
    }

    public Optional<Promotion> bestFor(Order order) {
        return promotions.values().stream()
                .filter(p -> p.discountFor(order).signum() > 0)
                .max(Comparator.comparing(p -> p.discountFor(order)));
    }

    public static PromotionEngine withDefaults() {
        return new PromotionEngine()
                .register(new PercentagePromotion("BIENVENIDA10", "10% de descuento en tu compra", 10, Money.ZERO))
                .register(new FixedAmountPromotion("AHORRA5000", "$5.000 de descuento en compras desde $30.000",
                        Money.of(5000), Money.of(30000)))
                .register(new BuyNGetOnePromotion("CAFE3X2", "Lleva 3 bebidas iguales y paga 2",
                        ProductCategory.BEVERAGE, 2));
    }
}
