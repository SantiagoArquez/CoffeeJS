package com.cafeteria.pedido;

import java.util.Optional;

public enum EstadoPedido {

    CREADO("Creado"),

    EN_PREPARACION("En preparación"),

    LISTO("Listo"),

    ENTREGADO("Entregado"),

    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String obtenerEtiqueta() {
        return etiqueta;
    }

    public boolean puedeCambiarA(EstadoPedido destino) {
        return switch (this) {
            case CREADO -> destino == EN_PREPARACION || destino == CANCELADO;
            case EN_PREPARACION -> destino == LISTO || destino == CANCELADO;
            case LISTO -> destino == ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }

    public Optional<EstadoPedido> siguiente() {
        return switch (this) {
            case CREADO -> Optional.of(EN_PREPARACION);
            case EN_PREPARACION -> Optional.of(LISTO);
            case LISTO -> Optional.of(ENTREGADO);
            case ENTREGADO, CANCELADO -> Optional.empty();
        };
    }

    public boolean permiteModificacion() {
        return this == CREADO || this == EN_PREPARACION;
    }

    public String codigoArchivo() {
        return switch (this) {
            case CREADO -> "CREATED";
            case EN_PREPARACION -> "PREPARING";
            case LISTO -> "READY";
            case ENTREGADO -> "DELIVERED";
            case CANCELADO -> "CANCELLED";
        };
    }

    public static EstadoPedido desdeCodigoArchivo(String codigo) {
        return switch (codigo) {
            case "CREATED", "CREADO" -> CREADO;
            case "PREPARING", "EN_PREPARACION" -> EN_PREPARACION;
            case "READY", "LISTO" -> LISTO;
            case "DELIVERED", "ENTREGADO" -> ENTREGADO;
            case "CANCELLED", "CANCELADO" -> CANCELADO;
            default -> throw new IllegalArgumentException("Código desconocido: " + codigo);
        };
    }
}
