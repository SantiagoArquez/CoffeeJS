package com.cafeteria.decorator;

import com.cafeteria.catalog.ProductFactory;
import com.cafeteria.model.Product;
import com.cafeteria.util.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica el patrón Decorator: precio, descripción, apilamiento y restricciones. */
class ProductDecoratorTest {

    private final Product latte = ProductFactory.beverage("Latte", "Espresso con leche", 8000);
    private final Product sandwich = ProductFactory.food("Sándwich", "Pollo", 14000);

    @Test
    void decoratorAddsItsPriceAndLabel() {
        Product shot = new ExtraShotDecorator(latte);
        assertEquals(Money.of(8000).add(ExtraShotDecorator.PRICE), shot.getPrice());
        assertTrue(shot.getName().contains(ExtraShotDecorator.LABEL));
        assertTrue(shot.getDescription().contains(ExtraShotDecorator.LABEL));
    }

    @Test
    void decoratorsCanBeStackedInAnyOrder() {
        Product a = new SyrupDecorator(new ExtraShotDecorator(latte));
        Product b = new ExtraShotDecorator(new SyrupDecorator(latte));
        assertEquals(Money.of(8000).add(ExtraShotDecorator.PRICE).add(SyrupDecorator.PRICE), a.getPrice());
        assertEquals(a.getPrice(), b.getPrice());
    }

    @Test
    void originalProductIsNotModified() {
        new WhippedCreamDecorator(latte);
        assertEquals(Money.of(8000), latte.getPrice());
        assertEquals("Latte", latte.getName());
    }

    @Test
    void decoratedProductKeepsCategory() {
        assertEquals(latte.getCategory(), new SyrupDecorator(latte).getCategory());
    }

    @Test
    void beverageExtraCannotBeAppliedToFood() {
        assertThrows(IllegalArgumentException.class, () -> new SyrupDecorator(sandwich));
    }

    @Test
    void foodExtraCannotBeAppliedToBeverage() {
        assertThrows(IllegalArgumentException.class, () -> new ExtraCheeseDecorator(latte));
        assertEquals(Money.of(14000).add(ExtraCheeseDecorator.PRICE), new ExtraCheeseDecorator(sandwich).getPrice());
    }

    @Test
    void nullProductIsRejected() {
        assertThrows(NullPointerException.class, () -> new SyrupDecorator(null));
    }

    @Test
    void customizationCatalogOffersOnlyCompatibleOptions() {
        assertTrue(CustomizationCatalog.availableFor(latte).stream().noneMatch(o -> o.label().equals(ExtraCheeseDecorator.LABEL)));
        assertEquals(1, CustomizationCatalog.availableFor(sandwich).size());
        Product combo = ProductFactory.combo("Combo", "x", 10, latte, sandwich);
        assertTrue(CustomizationCatalog.availableFor(combo).isEmpty());
    }

    @Test
    void customizationOptionAppliesDecorator() {
        CustomizationOption option = CustomizationCatalog.availableFor(latte).get(0);
        assertEquals(latte.getPrice().add(option.price()), option.applyTo(latte).getPrice());
    }
}
