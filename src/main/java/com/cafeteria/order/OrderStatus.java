package com.cafeteria.order;

import java.util.Optional;

public enum OrderStatus {
    CREATED("Creado"),
    PREPARING("En preparación"),
    READY("Listo"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> target == PREPARING || target == CANCELLED;
            case PREPARING -> target == READY || target == CANCELLED;
            case READY -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public Optional<OrderStatus> next() {
        return switch (this) {
            case CREATED -> Optional.of(PREPARING);
            case PREPARING -> Optional.of(READY);
            case READY -> Optional.of(DELIVERED);
            case DELIVERED, CANCELLED -> Optional.empty();
        };
    }
}
