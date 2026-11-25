package com.cafeteria.sales;

import com.cafeteria.TestSupport;
import com.cafeteria.catalog.ProductFactory;
import com.cafeteria.model.Product;
import com.cafeteria.order.Order;
import com.cafeteria.order.OrderBuilder;
import com.cafeteria.order.OrderService;
import com.cafeteria.promotion.PercentagePromotion;
import com.cafeteria.util.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica el registro de ventas, el cierre de caja, los reportes y la persistencia. */
class SalesServiceTest {

    private final Product latte = ProductFactory.beverage("Latte", "x", 10000);
    private final Product cake = ProductFactory.food("Torta", "x", 20000);
    private OrderService orders;
    private SalesService sales;

    @BeforeEach
    void setUp() {
        sales = TestSupport.memorySales();
        orders = new OrderService(sales);
    }

    private Order deliver(OrderBuilder builder) {
        Order o = orders.place(builder);
        orders.advance(o.getId());
        orders.advance(o.getId());
        orders.advance(o.getId());
        return o;
    }

    @Test
    void saleKeepsThePricesCollectedAtDeliveryTime() {
        Order o = deliver(new OrderBuilder().customer("Ana").add(latte, 2).add(cake)
                .promotion(new PercentagePromotion("P10", "d", 10, Money.ZERO)));
        Sale sale = sales.all().get(0);
        assertEquals(o.getId(), sale.orderId());
        assertEquals(Money.of(40000), sale.subtotal());
        assertEquals(Money.of(4000), sale.discount());
        assertEquals(Money.of(36000), sale.total());
        assertEquals("P10", sale.promotionCode());
        assertEquals(2, sale.items().size());
        assertTrue(sale.isOpen());
    }

    @Test
    void recordingTheSameOrderTwiceDoesNotDuplicateTheSale() {
        Order o = deliver(new OrderBuilder().add(latte));
        sales.record(o);
        assertEquals(1, sales.all().size());
    }

    @Test
    void cashClosingSumsOpenSalesAndClosesThem() {
        deliver(new OrderBuilder().add(latte, 2));
        deliver(new OrderBuilder().add(cake).promotion(new PercentagePromotion("P", "d", 10, Money.ZERO)));
        CashClosing closing = sales.closeRegister("Luis");
        assertEquals(1, closing.id());
        assertEquals(2, closing.salesCount());
        assertEquals(Money.of(40000), closing.subtotal());
        assertEquals(Money.of(2000), closing.discounts());
        assertEquals(Money.of(38000), closing.total());
        assertEquals("Luis", closing.cashier());
        assertTrue(sales.openSales().isEmpty());
        assertEquals(List.of(closing), sales.closings());
        assertTrue(sales.all().stream().noneMatch(Sale::isOpen));
    }

    @Test
    void secondClosingOnlyIncludesNewSales() {
        deliver(new OrderBuilder().add(latte));
        sales.closeRegister("Luis");
        deliver(new OrderBuilder().add(cake));
        CashClosing second = sales.closeRegister("Marta");
        assertEquals(2, second.id());
        assertEquals(1, second.salesCount());
        assertEquals(Money.of(20000), second.total());
    }

    @Test
    void closingWithoutSalesOrWithoutCashierFails() {
        assertThrows(IllegalStateException.class, () -> sales.closeRegister("Luis"));
        deliver(new OrderBuilder().add(latte));
        assertThrows(IllegalArgumentException.class, () -> sales.closeRegister(" "));
        assertThrows(IllegalArgumentException.class, () -> sales.closeRegister(null));
        assertEquals(1, sales.openSales().size());
    }

    @Test
    void reportSummarizesTheSalesOfThePeriod() {
        deliver(new OrderBuilder().customer("Ana").add(latte, 3).promotion(new PercentagePromotion("P10", "d", 10, Money.ZERO)));
        deliver(new OrderBuilder().customer("Luis").add(cake).add(latte));
        LocalDate day = LocalDate.of(2026, 11, 25);
        String report = sales.salesReport(day, day);
        assertTrue(report.contains("Ventas:           2"));
        assertTrue(report.contains(Money.format(Money.of(57000))));
        assertTrue(report.contains("4x Latte"));
        assertTrue(report.contains("P10"));
        assertEquals(report, sales.dailyReport(day));
    }

    @Test
    void reportForEmptyPeriodAndInvalidRange() {
        LocalDate day = LocalDate.of(2026, 11, 25);
        assertTrue(sales.salesReport(day, day).contains("No hay ventas"));
        assertThrows(IllegalArgumentException.class, () -> sales.salesReport(day, day.minusDays(1)));
        deliver(new OrderBuilder().add(latte));
        assertTrue(sales.salesBetween(day.plusDays(1), day.plusDays(2)).isEmpty());
        assertEquals(1, sales.salesBetween(day.minusDays(1), day.plusDays(1)).size());
    }

    @Test
    void closingReceiptShowsTheTotals() {
        deliver(new OrderBuilder().add(latte));
        String text = sales.closingReport(sales.closeRegister("Luis"));
        assertTrue(text.contains("CIERRE DE CAJA #1"));
        assertTrue(text.contains("Luis"));
        assertTrue(text.contains(Money.format(Money.of(10000))));
    }

    @Test
    void orderNumbersContinueAfterExistingSales() {
        deliver(new OrderBuilder().add(latte));
        deliver(new OrderBuilder().add(latte));
        OrderService restarted = new OrderService(sales);
        assertEquals(3, restarted.place(new OrderBuilder().add(latte)).getId());
    }

    @Test
    void salesAndClosingsSurviveARestart(@TempDir Path dir) {
        SalesService first = new SalesService(FileSalesRepository.open(dir), TestSupport.clockAt(2026, 11, 25, 10, 0));
        OrderService firstOrders = new OrderService(first);
        Order o = firstOrders.place(new OrderBuilder().customer("Ana; con | símbolos\ty tab").add(latte, 2)
                .promotion(new PercentagePromotion("P10", "d", 10, Money.ZERO)));
        firstOrders.advance(o.getId());
        firstOrders.advance(o.getId());
        firstOrders.advance(o.getId());
        first.closeRegister("Luis");
        firstOrders.advance(firstOrders.place(new OrderBuilder().add(cake)).getId());

        SalesService second = new SalesService(FileSalesRepository.open(dir), TestSupport.clockAt(2026, 11, 26, 10, 0));
        assertEquals(1, second.all().size());
        Sale loaded = second.all().get(0);
        assertEquals(Money.of(18000), loaded.total());
        assertEquals("P10", loaded.promotionCode());
        assertEquals(1, loaded.closingId());
        assertEquals(1, loaded.items().size());
        assertEquals(2, loaded.items().get(0).quantity());
        assertEquals(1, second.closings().size());
        assertEquals(Money.of(18000), second.closings().get(0).total());
        assertEquals(1, second.lastOrderId());
        assertEquals(2, new OrderService(second).place(new OrderBuilder().add(latte)).getId());
    }

    @Test
    void corruptedSalesFileIsReportedInsteadOfIgnored(@TempDir Path dir) throws Exception {
        java.nio.file.Files.writeString(dir.resolve(FileSalesRepository.SALES_FILE), "esto no es una venta\n");
        assertThrows(RuntimeException.class, () -> FileSalesRepository.open(dir));
    }
}
