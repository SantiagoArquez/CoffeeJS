package com.cafeteria.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class OrderService {

    private final Map<Long, Order> orders = new LinkedHashMap<>();
    private final List<OrderListener> listeners = new ArrayList<>();
    private long sequence = 0;

    public void addListener(OrderListener listener) {
        listeners.add(listener);
    }

    public Order place(OrderBuilder builder) {
        Order order = builder.build(++sequence);
        orders.put(order.getId(), order);
        listeners.forEach(l -> l.onStatusChanged(order, null, order.getStatus()));
        return order;
    }

    public Optional<Order> find(long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public Collection<Order> all() {
        return List.copyOf(orders.values());
    }

    public Order advance(long id) {
        Order order = require(id);
        OrderStatus next = order.getStatus().next()
                .orElseThrow(() -> new IllegalStateException(
                        "El pedido #" + id + " ya está en estado final (" + order.getStatus().getLabel() + ")"));
        return changeStatus(order, next);
    }

    public Order cancel(long id) {
        return changeStatus(require(id), OrderStatus.CANCELLED);
    }

    private Order changeStatus(Order order, OrderStatus target) {
        OrderStatus previous = order.getStatus();
        if (!previous.canTransitionTo(target)) {
            throw new IllegalStateException("No se puede pasar de '" + previous.getLabel()
                    + "' a '" + target.getLabel() + "'");
        }
        order.setStatus(target);
        listeners.forEach(l -> l.onStatusChanged(order, previous, target));
        return order;
    }

    private Order require(long id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("No existe el pedido #" + id));
    }
}
