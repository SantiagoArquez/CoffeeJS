package com.cafeteria.order;

import com.cafeteria.promotion.Promotion;
import com.cafeteria.util.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Pedido de un cliente: productos con cantidades, promoción aplicada y estado de atención
 * (RF08, RF10 y RF14).
 *
 * <p>Es el <b>sujeto</b> de los cambios que se notifican a los observadores
 * ({@link OrderListener}); el cambio de estado y la modificación del contenido se hacen
 * siempre a través de {@link OrderService}, que es quien avisa. Los totales se calculan en
 * cada consulta a partir de las líneas, de modo que nunca quedan desactualizados (RNF09).</p>
 */
public final class Order {

    private final long id;
    private String customerName;
    private List<OrderLine> lines;
    private Promotion promotion;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private OrderStatus status = OrderStatus.CREATED;

    /** Constructor interno; los pedidos se crean con {@link OrderBuilder}. */
    Order(long id, String customerName, List<OrderLine> lines, Promotion promotion) {
        this.id = id;
        this.customerName = customerName;
        this.lines = List.copyOf(lines);
        this.promotion = promotion;
    }

    /** @return número del pedido (0 si es solo un borrador) */
    public long getId() {
        return id;
    }

    /** @return nombre del cliente */
    public String getCustomerName() {
        return customerName;
    }

    /** @return líneas del pedido (vista de solo lectura) */
    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /** @return estado actual de atención */
    public OrderStatus getStatus() {
        return status;
    }

    /** Cambia el estado; solo lo invoca {@link OrderService}, que valida la transición. */
    void setStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * Reemplaza cliente, líneas y promoción por los de otro pedido ya validado; conserva
     * el número, la fecha de creación y el estado. Solo lo invoca {@link OrderService}.
     */
    void replaceContent(Order replacement) {
        this.customerName = replacement.customerName;
        this.lines = replacement.lines;
        this.promotion = replacement.promotion;
    }

    /** @return la promoción aplicada, o vacío si no tiene */
    public Optional<Promotion> getPromotion() {
        return Optional.ofNullable(promotion);
    }

    /** @return suma de las líneas antes de la promoción */
    public BigDecimal subtotal() {
        return lines.stream().map(OrderLine::total).reduce(Money.ZERO, BigDecimal::add);
    }

    /** @return descuento de la promoción, nunca mayor que el subtotal */
    public BigDecimal discount() {
        if (promotion == null) {
            return Money.ZERO;
        }
        return promotion.discountFor(this).min(subtotal());
    }

    /** @return valor final a cobrar: subtotal menos descuento (RF14) */
    public BigDecimal total() {
        return subtotal().subtract(discount());
    }

    /** @return recibo en texto con líneas, subtotal, promoción, total y estado */
    public String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append(id == 0
                ? String.format("  BORRADOR DE PEDIDO - %s%n", customerName)
                : String.format("  PEDIDO #%d - %s%n", id, customerName));
        sb.append("  ").append(createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append('\n');
        sb.append("========================================\n");
        for (OrderLine line : lines) {
            sb.append(String.format("%dx %s%n", line.quantity(), line.product().getName()));
            sb.append(String.format("      %s c/u  ->  %s%n", Money.format(line.product().getPrice()),
                    Money.format(line.total())));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("Subtotal:   %s%n", Money.format(subtotal())));
        if (promotion != null) {
            sb.append(String.format("Promo %s: -%s%n", promotion.getCode(), Money.format(discount())));
        }
        sb.append(String.format("TOTAL:      %s%n", Money.format(total())));
        sb.append(String.format("Estado:     %s%n", status.getLabel()));
        sb.append("========================================");
        return sb.toString();
    }
}
