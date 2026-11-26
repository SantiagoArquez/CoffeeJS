package com.cafeteria.model;

import java.math.BigDecimal;

public interface Product {

    String getName();

    String getDescription();

    BigDecimal getPrice();

    ProductCategory getCategory();
}
