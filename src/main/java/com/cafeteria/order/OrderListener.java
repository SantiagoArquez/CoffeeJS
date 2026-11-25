package com.cafeteria.order;

public interface OrderListener {

    void onStatusChanged(Order order, OrderStatus previous, OrderStatus current);
}
