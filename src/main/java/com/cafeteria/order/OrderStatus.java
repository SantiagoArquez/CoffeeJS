package com.cafeteria.order;

import java.util.Optional;

/**
 * Estados por los que pasa un pedido y las transiciones permitidas entre ellos (RF10).
 *
 * <p>Flujo normal: Creado → En preparación → Listo → Entregado. Un pedido puede
 * cancelarse mientras esté creado o en preparación; «Entregado» y «Cancelado» son
 * estados finales.</p>
 */
public enum OrderStatus {
    /** Pedido registrado, a la espera de que cocina lo tome. */
    CREATED("Creado"),
    /** Cocina está preparándolo. */
    PREPARING("En preparación"),
    /** Preparación terminada, listo para entregar. */
    READY("Listo"),
    /** Entregado al cliente; origina la venta. */
    DELIVERED("Entregado"),
    /** Cancelado antes de estar listo. */
    CANCELLED("Cancelado");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    /** @return nombre en español para mostrar en las interfaces */
    public String getLabel() {
        return label;
    }

    /**
     * @param target estado al que se quiere pasar
     * @return {@code true} si la transición desde este estado está permitida
     */
    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> target == PREPARING || target == CANCELLED;
            case PREPARING -> target == READY || target == CANCELLED;
            case READY -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    /** @return el siguiente estado del flujo normal, o vacío si este es un estado final */
    public Optional<OrderStatus> next() {
        return switch (this) {
            case CREATED -> Optional.of(PREPARING);
            case PREPARING -> Optional.of(READY);
            case READY -> Optional.of(DELIVERED);
            case DELIVERED, CANCELLED -> Optional.empty();
        };
    }

    /** @return {@code true} mientras el contenido del pedido todavía se puede modificar (RF09) */
    public boolean allowsModification() {
        return this == CREATED || this == PREPARING;
    }
}
