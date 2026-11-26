package com.cafeteria.promotion;

import com.cafeteria.order.Order;

import java.math.BigDecimal;

public interface Promotion {

    String getCode();

    String getDescription();

    BigDecimal discountFor(Order order);
}
