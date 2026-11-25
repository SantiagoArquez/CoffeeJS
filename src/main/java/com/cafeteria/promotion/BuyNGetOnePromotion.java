package com.cafeteria.promotion;

import com.cafeteria.model.ProductCategory;
import com.cafeteria.order.Order;
import com.cafeteria.order.OrderLine;
import com.cafeteria.util.Money;

import java.math.BigDecimal;

public final class BuyNGetOnePromotion implements Promotion {

    private final String code;
    private final String description;
    private final ProductCategory category;
    private final int buy;

    public BuyNGetOnePromotion(String code, String description, ProductCategory category, int buy) {
        if (buy < 1) {
            throw new IllegalArgumentException("Debe comprarse al menos 1 unidad");
        }
        this.code = code;
        this.description = description;
        this.category = category;
        this.buy = buy;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public BigDecimal discountFor(Order order) {
        BigDecimal discount = Money.ZERO;
        for (OrderLine line : order.getLines()) {
            if (line.product().getCategory() != category) {
                continue;
            }
            int freeUnits = line.quantity() / (buy + 1);
            discount = discount.add(line.product().getPrice().multiply(BigDecimal.valueOf(freeUnits)));
        }
        return discount;
    }
}
