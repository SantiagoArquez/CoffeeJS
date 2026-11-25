package com.cafeteria.order;

import com.cafeteria.promotion.Promotion;
import com.cafeteria.util.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class Order {

    private final long id;
    private final String customerName;
    private final List<OrderLine> lines;
    private final Promotion promotion;
    private final LocalDateTime createdAt = LocalDateTime.now();
    private OrderStatus status = OrderStatus.CREATED;

    Order(long id, String customerName, List<OrderLine> lines, Promotion promotion) {
        this.id = id;
        this.customerName = customerName;
        this.lines = List.copyOf(lines);
        this.promotion = promotion;
    }

    public long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public OrderStatus getStatus() {
        return status;
    }

    void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Optional<Promotion> getPromotion() {
        return Optional.ofNullable(promotion);
    }

    public BigDecimal subtotal() {
        return lines.stream().map(OrderLine::total).reduce(Money.ZERO, BigDecimal::add);
    }

    public BigDecimal discount() {
        if (promotion == null) {
            return Money.ZERO;
        }
        return promotion.discountFor(this).min(subtotal());
    }

    public BigDecimal total() {
        return subtotal().subtract(discount());
    }

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
