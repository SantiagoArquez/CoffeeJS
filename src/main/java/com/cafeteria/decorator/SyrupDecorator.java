package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.util.Money;

import java.math.BigDecimal;

public final class SyrupDecorator extends ProductDecorator {

    public static final String LABEL = "Sirope de vainilla";
    public static final BigDecimal PRICE = Money.of(1500);

    public SyrupDecorator(Product wrapped) {
        super(wrapped, ProductCategory.BEVERAGE, LABEL, PRICE);
    }
}
