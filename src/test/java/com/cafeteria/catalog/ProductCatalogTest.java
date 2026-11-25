package com.cafeteria.catalog;

import com.cafeteria.decorator.SyrupDecorator;
import com.cafeteria.model.Combo;
import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.model.SimpleProduct;
import com.cafeteria.util.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica la gestión de productos (RF01-RF03), sus validaciones y la persistencia del menú. */
class ProductCatalogTest {

    private final Product latte = ProductFactory.beverage("Latte", "x", 8000);
    private final Product cake = ProductFactory.food("Torta", "x", 9000);

    @Test
    void registerFindUpdateAndRemove() {
        ProductCatalog catalog = new InMemoryProductCatalog();
        catalog.register("beb-01", latte);
        assertSame(latte, catalog.findByCode("BEB-01").orElseThrow());
        catalog.update("BEB-01", cake);
        assertSame(cake, catalog.findByCode(" beb-01 ").orElseThrow());
        catalog.remove("BEB-01");
        assertTrue(catalog.findByCode("BEB-01").isEmpty());
        assertTrue(catalog.entries().isEmpty());
    }

    @Test
    void duplicatedMissingAndBlankCodesAreRejected() {
        ProductCatalog catalog = new InMemoryProductCatalog();
        catalog.register("A", latte);
        assertThrows(IllegalArgumentException.class, () -> catalog.register("a", cake));
        assertThrows(IllegalArgumentException.class, () -> catalog.update("B", cake));
        assertThrows(IllegalArgumentException.class, () -> catalog.remove("B"));
        assertThrows(IllegalArgumentException.class, () -> catalog.register(" ", cake));
        assertThrows(IllegalArgumentException.class, () -> catalog.register("C", null));
        assertTrue(catalog.findByCode(null).isEmpty());
    }

    @Test
    void factoryValidatesUserInput() {
        assertThrows(IllegalArgumentException.class, () -> ProductFactory.simple("", "d", "100", ProductCategory.FOOD));
        assertThrows(IllegalArgumentException.class, () -> ProductFactory.simple("N", "", "100", ProductCategory.FOOD));
        assertThrows(IllegalArgumentException.class, () -> ProductFactory.simple("N", "d", "abc", ProductCategory.FOOD));
        assertThrows(IllegalArgumentException.class, () -> ProductFactory.simple("N", "d", "0", ProductCategory.FOOD));
        assertThrows(IllegalArgumentException.class, () -> ProductFactory.simple("N", "d", "-5", ProductCategory.FOOD));
        assertEquals(Money.of(1500), ProductFactory.simple(" N ", " d ", " 1500 ", ProductCategory.FOOD).getPrice());
    }

    @Test
    void simpleProductRejectsInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleProduct("N", "d", Money.of(-1), ProductCategory.FOOD));
        assertThrows(IllegalArgumentException.class, () -> new SimpleProduct("N", "d", Money.of(1), ProductCategory.COMBO));
        assertThrows(NullPointerException.class, () -> new SimpleProduct("N", "d", null, ProductCategory.FOOD));
    }

    @Test
    void defaultMenuHasTheExpectedProducts() {
        ProductCatalog menu = DefaultMenu.loadDefaults();
        assertEquals(14, menu.entries().size());
        assertTrue(menu.findByCode("COM-01").orElseThrow() instanceof Combo);
    }

    @Test
    void menuIsSeededOnFirstRunAndSurvivesRestart(@TempDir Path dir) {
        Path file = dir.resolve("sub").resolve("menu.dat");
        ProductCatalog first = new FileProductCatalog(file, DefaultMenu.loadDefaults());
        assertTrue(Files.exists(file));
        first.register("BEB-99", ProductFactory.beverage("Nuevo", "n", 3000));
        first.update("ALI-01", ProductFactory.food("Croissant XL", "g", 7000));
        first.remove("BEB-01");

        ProductCatalog second = new FileProductCatalog(file, new InMemoryProductCatalog());
        assertEquals(first.entries().size(), second.entries().size());
        assertEquals("Nuevo", second.findByCode("BEB-99").orElseThrow().getName());
        assertEquals("Croissant XL", second.findByCode("ALI-01").orElseThrow().getName());
        assertTrue(second.findByCode("BEB-01").isEmpty());
        Combo combo = (Combo) second.findByCode("COM-01").orElseThrow();
        assertEquals(2, combo.getItems().size());
        assertEquals(((Combo) first.findByCode("COM-01").orElseThrow()).getPrice(), combo.getPrice());
    }

    @Test
    void catalogCanPersistComboWithDecoratedItems(@TempDir Path dir) {
        Path file = dir.resolve("menu.dat");
        ProductCatalog first = new FileProductCatalog(file, null);
        first.register("COM-9", ProductFactory.combo("Especial", "e", 5, new SyrupDecorator(latte), cake));
        ProductCatalog second = new FileProductCatalog(file, null);
        assertEquals(first.findByCode("COM-9").orElseThrow().getPrice(), second.findByCode("COM-9").orElseThrow().getPrice());
    }

    @Test
    void corruptedMenuFileIsReported(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("menu.dat");
        Files.writeString(file, "basura");
        assertThrows(RuntimeException.class, () -> new FileProductCatalog(file, null));
    }

    @Test
    void loadFallsBackToDefaultsWhenDirectoryIsUsable(@TempDir Path dir) {
        System.setProperty("coffeejs.data", dir.toString());
        try {
            ProductCatalog menu = DefaultMenu.load();
            assertEquals(14, menu.entries().size());
            assertTrue(Files.exists(dir.resolve(DefaultMenu.MENU_FILE)));
        } finally {
            System.clearProperty("coffeejs.data");
        }
    }
}
