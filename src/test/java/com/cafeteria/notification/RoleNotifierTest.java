package com.cafeteria.notification;

import com.cafeteria.TestSupport;
import com.cafeteria.catalog.ProductFactory;
import com.cafeteria.order.Order;
import com.cafeteria.order.OrderBuilder;
import com.cafeteria.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica que cada área (observador) reciba solo los avisos que le corresponden (RF11). */
class RoleNotifierTest {

    private OrderService service;
    private Order order;

    @BeforeEach
    void setUp() {
        service = new OrderService(TestSupport.memorySales());
        order = service.place(new OrderBuilder().customer("Ana").add(ProductFactory.beverage("Latte", "x", 8000)));
    }

    @Test
    void kitchenIsToldAboutNewOrders() {
        assertEquals(1, service.kitchen().inbox().size());
        assertTrue(service.kitchen().inbox().get(0).message().contains("Ana"));
        assertTrue(service.cashier().inbox().isEmpty());
        assertTrue(service.waiter().inbox().isEmpty());
    }

    @Test
    void cashierAndWaiterAreToldWhenKitchenMarksOrderReady() {
        service.advance(order.getId());
        assertTrue(service.cashier().inbox().isEmpty());
        service.advance(order.getId());
        assertEquals(1, service.cashier().inbox().size());
        assertEquals(1, service.waiter().inbox().size());
        assertEquals(1, service.kitchen().inbox().size());
        assertEquals("Mesero", service.waiter().inbox().get(0).recipient());
        assertEquals(order.getId(), service.waiter().inbox().get(0).orderId());
    }

    @Test
    void deliveryIsOnlyRelevantToTheCashier() {
        service.advance(order.getId());
        service.advance(order.getId());
        service.advance(order.getId());
        assertEquals(2, service.cashier().inbox().size());
        assertEquals(1, service.waiter().inbox().size());
    }

    @Test
    void cancellationReachesEveryArea() {
        service.cancel(order.getId());
        assertEquals(2, service.kitchen().inbox().size());
        assertEquals(1, service.cashier().inbox().size());
        assertEquals(1, service.waiter().inbox().size());
    }

    @Test
    void modificationReachesKitchenAndCashierOnly() {
        service.modify(order.getId(), OrderBuilder.from(order).add(ProductFactory.food("Torta", "x", 9000)));
        assertEquals(2, service.kitchen().inbox().size());
        assertEquals(1, service.cashier().inbox().size());
        assertTrue(service.waiter().inbox().isEmpty());
    }

    @Test
    void sinksReceiveNotificationsAsTheyHappen() {
        List<Notification> received = new ArrayList<>();
        service.waiter().addSink(received::add);
        service.advance(order.getId());
        service.advance(order.getId());
        assertEquals(1, received.size());
        assertTrue(received.get(0).toString().startsWith("[Mesero]"));
    }

    @Test
    void clearEmptiesTheInbox() {
        service.kitchen().clear();
        assertTrue(service.kitchen().inbox().isEmpty());
    }
}
