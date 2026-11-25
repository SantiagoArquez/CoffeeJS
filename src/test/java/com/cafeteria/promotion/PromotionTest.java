package com.cafeteria.promotion;

import com.cafeteria.TestSupport;
import com.cafeteria.catalog.ProductFactory;
import com.cafeteria.decorator.ExtraShotDecorator;
import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.order.Order;
import com.cafeteria.order.OrderBuilder;
import com.cafeteria.util.Money;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica el patrón Strategy: cada promoción calcula su descuento de forma independiente. */
class PromotionTest {

    private final Product latte = ProductFactory.beverage("Latte", "x", 10000);
    private final Product cake = ProductFactory.food("Torta", "x", 20000);
    // 25/11/2025 es martes; 29/11/2025 es sábado.
    private final Clock tuesday4pm = TestSupport.clockAt(2025, 11, 25, 16, 0);
    private final Clock tuesday9am = TestSupport.clockAt(2025, 11, 25, 9, 0);
    private final Clock saturday = TestSupport.clockAt(2025, 11, 29, 16, 0);

    private Order order(Object... productsAndQty) {
        OrderBuilder b = new OrderBuilder();
        for (int i = 0; i < productsAndQty.length; i += 2) {
            b.add((Product) productsAndQty[i], (Integer) productsAndQty[i + 1]);
        }
        return b.preview();
    }

    @Test
    void percentageAppliesOverSubtotalAndMinimum() {
        Promotion p = new PercentagePromotion("P", "d", 10, Money.of(25000));
        assertEquals(Money.ZERO, p.discountFor(order(latte, 1)));
        assertEquals(Money.of(3000), p.discountFor(order(latte, 1, cake, 1)));
    }

    @Test
    void fixedAmountNeedsMinimumSubtotal() {
        Promotion p = new FixedAmountPromotion("F", "d", Money.of(5000), Money.of(30000));
        assertEquals(Money.ZERO, p.discountFor(order(latte, 1)));
        assertEquals(Money.of(5000), p.discountFor(order(latte, 1, cake, 1)));
    }

    @Test
    void buyNGetOneGivesFreeUnitsPerLine() {
        Promotion p = new BuyNGetOnePromotion("X", "3x2", ProductCategory.BEVERAGE, 2);
        assertEquals(Money.ZERO, p.discountFor(order(latte, 2)));
        assertEquals(Money.of(10000), p.discountFor(order(latte, 3)));
        assertEquals(Money.of(20000), p.discountFor(order(latte, 6)));
        assertEquals(Money.ZERO, p.discountFor(order(cake, 3)));
    }

    @Test
    void invalidParametersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PercentagePromotion("P", "d", 0, Money.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new PercentagePromotion("P", "d", 101, Money.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new FixedAmountPromotion("F", "d", Money.ZERO, Money.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new BuyNGetOnePromotion("B", "d", ProductCategory.FOOD, 0));
    }

    @Test
    void productPromotionOnlyDiscountsMatchingProducts() {
        Promotion byCategory = ProductPromotion.forCategory("B", "d", 10, ProductCategory.BEVERAGE);
        assertEquals(Money.of(1000), byCategory.discountFor(order(latte, 1, cake, 1)));
        Promotion byName = ProductPromotion.forProductName("L", "d", 50, "latte");
        assertEquals(Money.of(5000), byName.discountFor(order(latte, 1, cake, 1)));
        assertEquals(Money.of(6250), byName.discountFor(order(new ExtraShotDecorator(latte), 1)));
    }

    @Test
    void scheduleChecksDaysAndTimeWindow() {
        PromotionSchedule s = PromotionSchedule.of(Set.of(DayOfWeek.TUESDAY), LocalTime.of(15, 0), LocalTime.of(18, 0));
        assertTrue(s.isActive(LocalDateTime.of(2025, 11, 25, 15, 0)));
        assertTrue(s.isActive(LocalDateTime.of(2025, 11, 25, 17, 59)));
        assertFalse(s.isActive(LocalDateTime.of(2025, 11, 25, 18, 0)));
        assertFalse(s.isActive(LocalDateTime.of(2025, 11, 25, 14, 59)));
        assertFalse(s.isActive(LocalDateTime.of(2025, 11, 26, 16, 0)));
    }

    @Test
    void scheduleSupportsWholeDayAndEveryDayAndMidnightWrap() {
        assertTrue(PromotionSchedule.onDays(DayOfWeek.SATURDAY).isActive(LocalDateTime.of(2025, 11, 29, 3, 0)));
        assertFalse(PromotionSchedule.onDays(DayOfWeek.SATURDAY).isActive(LocalDateTime.of(2025, 11, 28, 12, 0)));
        PromotionSchedule night = PromotionSchedule.of(Set.of(), LocalTime.of(22, 0), LocalTime.of(2, 0));
        assertTrue(night.isActive(LocalDateTime.of(2025, 11, 25, 23, 0)));
        assertTrue(night.isActive(LocalDateTime.of(2025, 11, 25, 1, 0)));
        assertFalse(night.isActive(LocalDateTime.of(2025, 11, 25, 12, 0)));
        assertThrows(IllegalArgumentException.class, () -> PromotionSchedule.of(Set.of(), LocalTime.of(8, 0), null));
        assertThrows(IllegalArgumentException.class, () -> PromotionSchedule.of(Set.of(), LocalTime.of(8, 0), LocalTime.of(8, 0)));
    }

    @Test
    void scheduledPromotionAppliesOnlyInsideItsSchedule() {
        Promotion base = new PercentagePromotion("H", "10%", 10, Money.ZERO);
        PromotionSchedule schedule = PromotionSchedule.of(Set.of(DayOfWeek.TUESDAY), LocalTime.of(15, 0), LocalTime.of(18, 0));
        Order o = order(latte, 1);
        assertEquals(Money.of(1000), new ScheduledPromotion(base, schedule, tuesday4pm).discountFor(o));
        assertEquals(Money.ZERO, new ScheduledPromotion(base, schedule, tuesday9am).discountFor(o));
        assertEquals(Money.ZERO, new ScheduledPromotion(base, schedule, saturday).discountFor(o));
        assertTrue(new ScheduledPromotion(base, schedule, saturday).getDescription().contains("15:00"));
    }

    @Test
    void engineChoosesTheBestApplicablePromotion() {
        PromotionEngine engine = PromotionEngine.withDefaults(tuesday9am);
        assertEquals("CAFE3X2", engine.bestFor(order(latte, 3)).orElseThrow().getCode());
        assertEquals("BIENVENIDA10", engine.bestFor(order(cake, 1)).orElseThrow().getCode());
    }

    @Test
    void happyHourOnlyAppliesInsideItsWindow() {
        Order drinks = order(latte, 1);
        assertEquals(Money.of(1000), PromotionEngine.withDefaults(tuesday4pm).findByCode("happyhour").orElseThrow().discountFor(drinks));
        assertEquals(Money.ZERO, PromotionEngine.withDefaults(tuesday9am).findByCode("happyhour").orElseThrow().discountFor(drinks));
        assertEquals(Money.ZERO, PromotionEngine.withDefaults(saturday).findByCode("happyhour").orElseThrow().discountFor(drinks));
    }

    @Test
    void weekendComboPromotionOnlyAppliesOnWeekend() {
        Order o = order(ProductFactory.combo("Combo", "x", 0, latte, cake), 1);
        assertEquals(Money.of(4500), PromotionEngine.withDefaults(saturday).findByCode("COMBOFINDE").orElseThrow().discountFor(o));
        assertEquals(Money.ZERO, PromotionEngine.withDefaults(tuesday4pm).findByCode("COMBOFINDE").orElseThrow().discountFor(o));
    }

    @Test
    void engineRegistersFindsAndRemovesPromotions() {
        PromotionEngine engine = new PromotionEngine();
        engine.register(new PercentagePromotion("abc", "d", 10, Money.ZERO));
        assertTrue(engine.findByCode(" ABC ").isPresent());
        assertTrue(engine.findByCode(null).isEmpty());
        assertTrue(engine.remove("Abc"));
        assertFalse(engine.remove("Abc"));
        assertTrue(engine.all().isEmpty());
        assertTrue(engine.bestFor(order(latte, 1)).isEmpty());
    }
}
