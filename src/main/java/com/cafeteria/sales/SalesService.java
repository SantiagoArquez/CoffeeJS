package com.cafeteria.sales;

import com.cafeteria.order.Order;
import com.cafeteria.order.OrderLine;
import com.cafeteria.util.DataDirectory;
import com.cafeteria.util.Money;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Servicio de control de ventas: registra las ventas, realiza el cierre de caja y genera
 * reportes (RF15, RF16 y RF17).
 *
 * <p>No conoce la forma de almacenamiento: usa un {@link SalesRepository}. Se alimenta
 * automáticamente cuando un pedido pasa a «Entregado» gracias a {@link SalesLedger}.</p>
 */
public final class SalesService {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SalesRepository repository;
    private final Clock clock;

    /**
     * @param repository donde se guardan ventas y cierres
     * @param clock      reloj para fechar las ventas y los cierres
     */
    public SalesService(SalesRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Crea el servicio que usa la aplicación: guarda en la carpeta de
     * {@link DataDirectory}. Si el disco no está disponible avisa por la salida de error y
     * trabaja solo en memoria.
     *
     * @return servicio de ventas listo para usar
     */
    public static SalesService createDefault() {
        try {
            return new SalesService(FileSalesRepository.open(DataDirectory.resolve()), Clock.systemDefaultZone());
        } catch (RuntimeException e) {
            System.err.println("Aviso: no se pudo usar el almacenamiento de ventas (" + e.getMessage()
                    + "). Las ventas se guardarán solo en memoria.");
            return new SalesService(new InMemorySalesRepository(), Clock.systemDefaultZone());
        }
    }

    /**
     * Registra la venta correspondiente a un pedido entregado. Si el pedido ya tenía una
     * venta registrada no hace nada y devuelve la existente.
     *
     * @param order pedido entregado
     * @return la venta registrada
     */
    public Sale record(Order order) {
        for (Sale existing : repository.findAll()) {
            if (existing.orderId() == order.getId()) {
                return existing;
            }
        }
        List<SaleItem> items = new ArrayList<>();
        for (OrderLine line : order.getLines()) {
            items.add(new SaleItem(line.product().getName(), line.quantity(), line.product().getPrice()));
        }
        Sale sale = new Sale(repository.nextSaleId(), order.getId(), order.getCustomerName(),
                LocalDateTime.now(clock), order.subtotal(), order.discount(), order.total(),
                order.getPromotion().map(p -> p.getCode()).orElse(""), items, 0);
        repository.save(sale);
        return sale;
    }

    /** @return todas las ventas registradas */
    public List<Sale> all() {
        return repository.findAll();
    }

    /** @return las ventas que aún no pertenecen a ningún cierre de caja */
    public List<Sale> openSales() {
        return repository.findAll().stream().filter(Sale::isOpen).toList();
    }

    /** @return todos los cierres de caja realizados */
    public List<CashClosing> closings() {
        return repository.findAllClosings();
    }

    /** @return el mayor número de pedido que ya generó una venta (0 si no hay ventas) */
    public long lastOrderId() {
        return repository.findAll().stream().mapToLong(Sale::orderId).max().orElse(0);
    }

    /**
     * Realiza el cierre de caja: agrupa todas las ventas abiertas, calcula los totales y las
     * marca como cerradas.
     *
     * @param cashier nombre de quien realiza el cierre
     * @return el cierre generado
     * @throws IllegalArgumentException si no se indica el cajero
     * @throws IllegalStateException    si no hay ventas abiertas
     */
    public CashClosing closeRegister(String cashier) {
        if (cashier == null || cashier.isBlank()) {
            throw new IllegalArgumentException("El nombre del cajero es obligatorio");
        }
        List<Sale> open = openSales();
        if (open.isEmpty()) {
            throw new IllegalStateException("No hay ventas abiertas para cerrar la caja");
        }
        BigDecimal subtotal = sum(open, Sale::subtotal);
        BigDecimal discounts = sum(open, Sale::discount);
        BigDecimal total = sum(open, Sale::total);
        CashClosing closing = new CashClosing(repository.nextClosingId(), LocalDateTime.now(clock),
                cashier.trim(), open.size(), subtotal, discounts, total);
        repository.saveClosing(closing, open.stream().map(Sale::id).toList());
        return closing;
    }

    /**
     * Ventas realizadas entre dos fechas.
     *
     * @param from primer día incluido
     * @param to   último día incluido
     * @return ventas del rango
     * @throws IllegalArgumentException si {@code to} es anterior a {@code from}
     */
    public List<Sale> salesBetween(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        return repository.findAll().stream()
                .filter(s -> !s.soldAt().toLocalDate().isBefore(from) && !s.soldAt().toLocalDate().isAfter(to))
                .toList();
    }

    /**
     * Genera el reporte de ventas de un rango de fechas (RF17): totales, ticket promedio,
     * productos más vendidos y uso de promociones.
     *
     * @param from primer día incluido
     * @param to   último día incluido
     * @return reporte en texto listo para mostrar
     */
    public String salesReport(LocalDate from, LocalDate to) {
        List<Sale> sales = salesBetween(from, to);
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append(String.format("  REPORTE DE VENTAS %s - %s%n", from.format(DATE), to.format(DATE)));
        sb.append("========================================\n");
        if (sales.isEmpty()) {
            sb.append("No hay ventas registradas en el período.\n");
            return sb.toString();
        }
        BigDecimal total = sum(sales, Sale::total);
        sb.append(String.format("Ventas:           %d%n", sales.size()));
        sb.append(String.format("Subtotal:         %s%n", Money.format(sum(sales, Sale::subtotal))));
        sb.append(String.format("Descuentos:       %s%n", Money.format(sum(sales, Sale::discount))));
        sb.append(String.format("Total vendido:    %s%n", Money.format(total)));
        sb.append(String.format("Ticket promedio:  %s%n", Money.format(
                total.divide(BigDecimal.valueOf(sales.size()), 0, java.math.RoundingMode.HALF_UP))));

        Map<String, Integer> units = new LinkedHashMap<>();
        Map<String, Integer> promos = new LinkedHashMap<>();
        for (Sale sale : sales) {
            sale.items().forEach(i -> units.merge(i.name(), i.quantity(), Integer::sum));
            if (!sale.promotionCode().isEmpty()) {
                promos.merge(sale.promotionCode(), 1, Integer::sum);
            }
        }
        sb.append("\nProductos más vendidos:\n");
        units.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(5)
                .forEach(e -> sb.append(String.format("  %dx %s%n", e.getValue(), e.getKey())));
        if (!promos.isEmpty()) {
            sb.append("\nPromociones usadas:\n");
            promos.forEach((code, n) -> sb.append(String.format("  %s: %d vez/veces%n", code, n)));
        }
        sb.append("\nDetalle:\n");
        sales.stream().sorted(Comparator.comparing(Sale::soldAt)).forEach(s -> sb.append(String.format(
                "  #%d  %s  %-15s %s%n", s.orderId(), s.soldAt().format(DATE_TIME), s.customer(), Money.format(s.total()))));
        return sb.toString();
    }

    /**
     * Reporte del día indicado.
     *
     * @param day día a consultar
     * @return reporte en texto
     */
    public String dailyReport(LocalDate day) {
        return salesReport(day, day);
    }

    /**
     * Comprobante de un cierre de caja.
     *
     * @param closing cierre a describir
     * @return texto listo para imprimir o mostrar
     */
    public String closingReport(CashClosing closing) {
        return "========================================\n"
                + String.format("  CIERRE DE CAJA #%d%n", closing.id())
                + String.format("  %s - Cajero: %s%n", closing.closedAt().format(DATE_TIME), closing.cashier())
                + "========================================\n"
                + String.format("Ventas incluidas: %d%n", closing.salesCount())
                + String.format("Subtotal:         %s%n", Money.format(closing.subtotal()))
                + String.format("Descuentos:       %s%n", Money.format(closing.discounts()))
                + String.format("TOTAL RECAUDADO:  %s%n", Money.format(closing.total()))
                + "========================================";
    }

    private static BigDecimal sum(List<Sale> sales, java.util.function.Function<Sale, BigDecimal> field) {
        return sales.stream().map(field).reduce(Money.ZERO, BigDecimal::add);
    }
}
