package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.util.Money;

import java.math.BigDecimal;

public final class AlternativeMilkDecorator extends ProductDecorator {

    public static final String LABEL = "Leche de almendras";
    public static final BigDecimal PRICE = Money.of(2000);

    public AlternativeMilkDecorator(Product wrapped) {
        super(wrapped, ProductCategory.BEVERAGE, LABEL, PRICE);
    }
}
