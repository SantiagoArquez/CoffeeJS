package com.cafeteria.combo;

import com.cafeteria.catalog.ProductFactory;
import com.cafeteria.decorator.ExtraShotDecorator;
import com.cafeteria.model.Combo;
import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.util.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica el patrón Builder aplicado a los combos (Builder, Director y producto final). */
class ComboBuilderTest {

    private final Product burger = ProductFactory.food("Hamburguesa", "Res", 18000);
    private final Product fries = ProductFactory.food("Papas", "Fritas", 6000);
    private final Product cola = ProductFactory.beverage("Gaseosa", "Fría", 4000);

    @Test
    void buildsStepByStepAndCalculatesPrice() {
        Combo combo = new ComboPersonalizadoBuilder()
                .name("Combo Burger").description("Completo").discountPercent(10)
                .addMain(burger).addDrink(cola).addSide(fries).build();
        assertEquals(3, combo.getItems().size());
        assertEquals(Money.of(25200), combo.getPrice());
        assertEquals(ProductCategory.COMBO, combo.getCategory());
        assertEquals(10, combo.getDiscountPercent());
    }

    @Test
    void roleValidationRejectsWrongCategory() {
        ComboBuilder builder = new ComboPersonalizadoBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder.addDrink(burger));
        assertThrows(IllegalArgumentException.class, () -> builder.addMain(cola));
        assertThrows(IllegalArgumentException.class, () -> builder.addSide(cola));
    }

    @Test
    void extrasAcceptDecoratedProductsButNotCombos() {
        ComboBuilder builder = new ComboPersonalizadoBuilder();
        Product inner = ProductFactory.combo("Interno", "", 0, burger, cola);
        assertThrows(IllegalArgumentException.class, () -> builder.addExtra(inner));
        builder.addExtra(new ExtraShotDecorator(cola));
        builder.name("Con extra").addMain(burger);
        assertEquals(2, builder.build().getItems().size());
    }

    @Test
    void invalidDataIsRejected() {
        ComboBuilder builder = new ComboPersonalizadoBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder.discountPercent(101));
        assertThrows(IllegalArgumentException.class, () -> builder.discountPercent(-1));
        assertThrows(IllegalArgumentException.class, builder::build);
        builder.name("Solo uno").addMain(burger);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void resetClearsPreviousConfiguration() {
        ComboBuilder builder = new ComboPersonalizadoBuilder().name("A").addMain(burger).addDrink(cola).discountPercent(20);
        builder.reset();
        assertThrows(IllegalArgumentException.class, builder::build);
        Combo combo = builder.name("B").addMain(burger).addDrink(cola).build();
        assertEquals(0, combo.getDiscountPercent());
    }

    @Test
    void directorBuildsPredefinedRecipes() {
        DirectorCombo director = new DirectorCombo(new ComboPersonalizadoBuilder());
        Combo simple = director.drinkAndFood("Gaseosa y papas", "Rápido", 10, cola, fries);
        assertEquals(2, simple.getItems().size());
        assertEquals(cola, simple.getItems().get(0));

        Combo full = director.complete("Completo", "Todo", 15, burger, cola, fries, new ExtraShotDecorator(cola));
        assertEquals(4, full.getItems().size());

        Combo free = director.ofItems("Libre", "", 0, burger, fries, cola);
        assertEquals(3, free.getItems().size());
    }

    @Test
    void directorReusesTheSameBuilderWithoutLeakingState() {
        DirectorCombo director = new DirectorCombo(new ComboPersonalizadoBuilder());
        director.complete("Uno", "", 50, burger, cola, fries);
        Combo second = director.drinkAndFood("Dos", "", 0, cola, fries);
        assertEquals(2, second.getItems().size());
        assertEquals(0, second.getDiscountPercent());
    }

    @Test
    void productFactoryDelegatesToBuilder() {
        Product combo = ProductFactory.combo("Combo", "x", 10, burger, cola);
        assertEquals(Money.of(19800), combo.getPrice());
        Product edited = ProductFactory.editCombo((Combo) combo, "Nuevo", "y", "20");
        assertEquals(Money.of(17600), edited.getPrice());
        assertEquals("Nuevo", edited.getName());
        assertThrows(IllegalArgumentException.class, () -> ProductFactory.editCombo((Combo) combo, "N", "y", "abc"));
    }

    @Test
    void comboPriceIsRecalculatedFromItsComponents() {
        Combo combo = new ComboPersonalizadoBuilder().name("C").addDrink(cola).addMain(fries).build();
        assertEquals(Money.of(10000), combo.getPrice());
        assertTrue(combo.getDescription().contains("Gaseosa"));
    }
}
