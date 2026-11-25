package com.cafeteria.order;

/**
 * Interfaz que deben implementar los interesados en los cambios de un pedido (RF11).
 *
 * <p><b>Patrón Observer (rol Observer):</b> {@link OrderService} (el sujeto) avisa a todos
 * los observadores registrados cada vez que cambia el estado de un pedido, sin depender de
 * sus clases concretas (Dependency Inversion). Las pantallas, cocina, caja, mesero y el
 * registro de ventas son observadores.</p>
 */
public interface OrderListener {

    /**
     * Se invoca cuando un pedido se crea o cambia de estado.
     *
     * @param order    pedido afectado
     * @param previous estado anterior, o {@code null} si el pedido acaba de crearse
     * @param current  nuevo estado
     */
    void onStatusChanged(Order order, OrderStatus previous, OrderStatus current);

    /**
     * Se invoca cuando cambia el contenido de un pedido sin cambiar su estado (RF09). Por
     * defecto no hace nada, de modo que los observadores existentes no necesitan cambios.
     *
     * @param order pedido ya modificado
     */
    default void onOrderModified(Order order) {
    }
}
