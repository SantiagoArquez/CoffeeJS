package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.util.Money;

import java.math.BigDecimal;

public final class ExtraCheeseDecorator extends ProductDecorator {

    public static final String LABEL = "Queso extra";
    public static final BigDecimal PRICE = Money.of(2500);

    public ExtraCheeseDecorator(Product wrapped) {
        super(wrapped, ProductCategory.FOOD, LABEL, PRICE);
    }
}
