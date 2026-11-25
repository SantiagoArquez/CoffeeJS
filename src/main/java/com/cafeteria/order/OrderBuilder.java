package com.cafeteria.order;

import com.cafeteria.model.Product;
import com.cafeteria.promotion.Promotion;

import java.util.ArrayList;
import java.util.List;

/**
 * Arma un pedido paso a paso: cliente, productos con cantidades y promoción (RF08).
 *
 * <p>Permite previsualizar el pedido ({@link #preview()}) antes de enviarlo a
 * {@link OrderService#place(OrderBuilder)}, y partir de un pedido existente
 * ({@link #from(Order)}) para modificarlo con {@link OrderService#modify(long, OrderBuilder)}.</p>
 */
public final class OrderBuilder {

    private String customerName = "Cliente";
    private final List<OrderLine> lines = new ArrayList<>();
    private Promotion promotion;

    /**
     * Crea un builder con el contenido de un pedido existente, listo para editarlo.
     *
     * @param order pedido a copiar
     * @return un builder nuevo con el mismo cliente, líneas y promoción
     */
    public static OrderBuilder from(Order order) {
        OrderBuilder builder = new OrderBuilder().customer(order.getCustomerName());
        order.getLines().forEach(l -> builder.add(l.product(), l.quantity()));
        builder.promotion(order.getPromotion().orElse(null));
        return builder;
    }

    /**
     * @param name nombre del cliente; si está vacío se conserva el actual («Cliente» por defecto)
     * @return este builder
     */
    public OrderBuilder customer(String name) {
        if (name != null && !name.isBlank()) {
            this.customerName = name.trim();
        }
        return this;
    }

    /**
     * @param product  producto a agregar
     * @param quantity unidades, mayor que cero
     * @return este builder
     * @throws IllegalArgumentException si la cantidad no es válida
     */
    public OrderBuilder add(Product product, int quantity) {
        lines.add(new OrderLine(product, quantity));
        return this;
    }

    /**
     * @param product producto a agregar (1 unidad)
     * @return este builder
     */
    public OrderBuilder add(Product product) {
        return add(product, 1);
    }

    /**
     * Quita una línea del pedido.
     *
     * @param index posición de la línea, empezando en 0
     * @return este builder
     * @throws IndexOutOfBoundsException si la posición no existe
     */
    public OrderBuilder removeLine(int index) {
        lines.remove(index);
        return this;
    }

    /**
     * @param promotion promoción a aplicar, o {@code null} para ninguna
     * @return este builder
     */
    public OrderBuilder promotion(Promotion promotion) {
        this.promotion = promotion;
        return this;
    }

    /** @return {@code true} si todavía no se agregó ningún producto */
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /**
     * Construye un borrador (sin número) para mostrar el recibo antes de confirmar.
     *
     * @return pedido con id 0 que no se registra en ningún servicio
     * @throws IllegalStateException si no hay productos
     */
    public Order preview() {
        return build(0);
    }

    /** Construye el pedido definitivo; solo lo invoca {@link OrderService}. */
    Order build(long id) {
        if (lines.isEmpty()) {
            throw new IllegalStateException("El pedido debe tener al menos un producto");
        }
        return new Order(id, customerName, lines, promotion);
    }
}
