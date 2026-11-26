package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.util.Money;

import java.math.BigDecimal;

public final class ExtraShotDecorator extends ProductDecorator {

    public static final String LABEL = "Shot extra";
    public static final BigDecimal PRICE = Money.of(2500);

    public ExtraShotDecorator(Product wrapped) {
        super(wrapped, ProductCategory.BEVERAGE, LABEL, PRICE);
    }
}
