package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.util.Money;

import java.math.BigDecimal;

public final class WhippedCreamDecorator extends ProductDecorator {

    public static final String LABEL = "Crema batida";
    public static final BigDecimal PRICE = Money.of(1800);

    public WhippedCreamDecorator(Product wrapped) {
        super(wrapped, ProductCategory.BEVERAGE, LABEL, PRICE);
    }
}
