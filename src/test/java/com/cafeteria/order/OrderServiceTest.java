package com.cafeteria.order;

import com.cafeteria.TestSupport;
import com.cafeteria.catalog.ProductFactory;
import com.cafeteria.decorator.ExtraShotDecorator;
import com.cafeteria.model.Product;
import com.cafeteria.promotion.PercentagePromotion;
import com.cafeteria.util.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica pedidos, estados, modificación y el funcionamiento del Observer. */
class OrderServiceTest {

    private final Product latte = ProductFactory.beverage("Latte", "x", 8000);
    private final Product cake = ProductFactory.food("Torta", "x", 9000);
    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(TestSupport.memorySales());
    }

    private OrderBuilder basic() {
        return new OrderBuilder().customer("Ana").add(latte, 2).add(cake);
    }

    @Test
    void placeAssignsConsecutiveIdsAndStartsCreated() {
        Order a = service.place(basic());
        Order b = service.place(basic());
        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals(OrderStatus.CREATED, a.getStatus());
        assertEquals(2, service.all().size());
        assertSame(a, service.find(1).orElseThrow());
    }

    @Test
    void totalsAreCalculatedFromLinesAndPromotion() {
        Order o = service.place(basic().promotion(new PercentagePromotion("P", "d", 10, Money.ZERO)));
        assertEquals(Money.of(25000), o.subtotal());
        assertEquals(Money.of(2500), o.discount());
        assertEquals(Money.of(22500), o.total());
        assertTrue(o.receipt().contains("TOTAL"));
    }

    @Test
    void emptyOrderAndInvalidQuantityAreRejected() {
        assertThrows(IllegalStateException.class, () -> service.place(new OrderBuilder()));
        assertThrows(IllegalArgumentException.class, () -> new OrderBuilder().add(latte, 0));
        assertThrows(IllegalArgumentException.class, () -> new OrderBuilder().add(latte, -3));
    }

    @Test
    void statusFollowsTheNormalFlow() {
        Order o = service.place(basic());
        assertEquals(OrderStatus.PREPARING, service.advance(o.getId()).getStatus());
        assertEquals(OrderStatus.READY, service.advance(o.getId()).getStatus());
        assertEquals(OrderStatus.DELIVERED, service.advance(o.getId()).getStatus());
        assertThrows(IllegalStateException.class, () -> service.advance(o.getId()));
    }

    @Test
    void cancelOnlyWhileNotReady() {
        Order a = service.place(basic());
        assertEquals(OrderStatus.CANCELLED, service.cancel(a.getId()).getStatus());
        assertThrows(IllegalStateException.class, () -> service.cancel(a.getId()));
        Order b = service.place(basic());
        service.advance(b.getId());
        service.advance(b.getId());
        assertThrows(IllegalStateException.class, () -> service.cancel(b.getId()));
    }

    @Test
    void unknownOrderIsReportedClearly() {
        assertThrows(IllegalArgumentException.class, () -> service.advance(99));
        assertTrue(service.find(99).isEmpty());
    }

    @Test
    void listenersReceiveCreationAndEveryTransition() {
        List<String> events = new ArrayList<>();
        service.addListener((order, previous, current) -> events.add(previous + "->" + current));
        Order o = service.place(basic());
        service.advance(o.getId());
        service.advance(o.getId());
        assertEquals(List.of("null->CREATED", "CREATED->PREPARING", "PREPARING->READY"), events);
    }

    @Test
    void removedListenerStopsReceiving() {
        List<OrderStatus> events = new ArrayList<>();
        OrderListener listener = (order, previous, current) -> events.add(current);
        service.addListener(listener);
        service.place(basic());
        service.removeListener(listener);
        service.place(basic());
        assertEquals(1, events.size());
    }

    @Test
    void invalidTransitionDoesNotNotifyNorChangeState() {
        List<OrderStatus> events = new ArrayList<>();
        Order o = service.place(basic());
        service.addListener((order, previous, current) -> events.add(current));
        service.cancel(o.getId());
        events.clear();
        assertThrows(IllegalStateException.class, () -> service.cancel(o.getId()));
        assertTrue(events.isEmpty());
        assertEquals(OrderStatus.CANCELLED, o.getStatus());
    }

    @Test
    void modifyReplacesContentAndKeepsIdAndStatus() {
        Order o = service.place(basic());
        service.advance(o.getId());
        OrderBuilder edited = OrderBuilder.from(o).add(new ExtraShotDecorator(latte), 1).removeLine(1);
        Order modified = service.modify(o.getId(), edited);
        assertSame(o, modified);
        assertEquals(OrderStatus.PREPARING, modified.getStatus());
        assertEquals(2, modified.getLines().size());
        assertEquals(Money.of(2 * 8000 + 8000 + 2500), modified.total());
    }

    @Test
    void modifyIsNotAllowedOnceReadyOrFinished() {
        Order o = service.place(basic());
        service.advance(o.getId());
        service.advance(o.getId());
        assertThrows(IllegalStateException.class, () -> service.modify(o.getId(), OrderBuilder.from(o).add(cake)));
        Order c = service.place(basic());
        service.cancel(c.getId());
        assertThrows(IllegalStateException.class, () -> service.modify(c.getId(), OrderBuilder.from(c)));
        assertThrows(IllegalArgumentException.class, () -> service.modify(99, basic()));
    }

    @Test
    void modifyRejectsEmptyContentWithoutChangingTheOrder() {
        Order o = service.place(basic());
        assertThrows(IllegalStateException.class, () -> service.modify(o.getId(), new OrderBuilder()));
        assertEquals(2, o.getLines().size());
    }

    @Test
    void modifyNotifiesListenersAboutTheChange() {
        List<Long> modified = new ArrayList<>();
        service.addListener(new OrderListener() {
            @Override
            public void onStatusChanged(Order order, OrderStatus previous, OrderStatus current) {
            }

            @Override
            public void onOrderModified(Order order) {
                modified.add(order.getId());
            }
        });
        Order o = service.place(basic());
        service.modify(o.getId(), OrderBuilder.from(o).add(cake));
        assertEquals(List.of(o.getId()), modified);
    }

    @Test
    void statusRulesAreConsistent() {
        assertTrue(OrderStatus.CREATED.allowsModification());
        assertTrue(OrderStatus.PREPARING.allowsModification());
        assertFalse(OrderStatus.READY.allowsModification());
        assertTrue(OrderStatus.DELIVERED.next().isEmpty());
        assertFalse(OrderStatus.READY.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void deliveredOrderBecomesASaleAndCancelledDoesNot() {
        Order delivered = service.place(basic());
        service.advance(delivered.getId());
        service.advance(delivered.getId());
        service.advance(delivered.getId());
        Order cancelled = service.place(basic());
        service.cancel(cancelled.getId());
        assertEquals(1, service.sales().all().size());
        assertEquals(delivered.getId(), service.sales().all().get(0).orderId());
        assertEquals(delivered.total(), service.sales().all().get(0).total());
    }
}
