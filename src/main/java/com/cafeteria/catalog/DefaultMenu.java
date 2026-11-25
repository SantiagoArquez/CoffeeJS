package com.cafeteria.catalog;

import com.cafeteria.model.Product;

public final class DefaultMenu {

    private DefaultMenu() {
    }

    public static ProductCatalog load() {
        ProductCatalog catalog = new InMemoryProductCatalog();

        Product espresso = ProductFactory.beverage("Espresso", "Café concentrado", 4500);
        Product americano = ProductFactory.beverage("Americano", "Espresso con agua caliente", 5000);
        Product cappuccino = ProductFactory.beverage("Cappuccino", "Espresso con leche espumada", 7500);
        Product latte = ProductFactory.beverage("Latte", "Espresso con abundante leche", 8000);
        Product chocolate = ProductFactory.beverage("Chocolate caliente", "Chocolate espeso de la casa", 7000);
        Product chai = ProductFactory.beverage("Té chai", "Té especiado con leche", 6500);

        Product croissant = ProductFactory.food("Croissant", "Mantequilla, horneado del día", 6000);
        Product sandwich = ProductFactory.food("Sándwich de pollo", "Pollo desmechado, queso y vegetales", 14000);
        Product panDeBono = ProductFactory.food("Pan de bono", "Tradicional, recién horneado", 3500);
        Product torta = ProductFactory.food("Torta de zanahoria", "Con crema de queso", 9000);
        Product tostada = ProductFactory.food("Tostada con aguacate", "Pan artesanal y aguacate", 12000);

        catalog.register("BEB-01", espresso);
        catalog.register("BEB-02", americano);
        catalog.register("BEB-03", cappuccino);
        catalog.register("BEB-04", latte);
        catalog.register("BEB-05", chocolate);
        catalog.register("BEB-06", chai);

        catalog.register("ALI-01", croissant);
        catalog.register("ALI-02", sandwich);
        catalog.register("ALI-03", panDeBono);
        catalog.register("ALI-04", torta);
        catalog.register("ALI-05", tostada);

        catalog.register("COM-01", ProductFactory.combo("Combo Desayuno", "Para empezar el día", 10, cappuccino, croissant));
        catalog.register("COM-02", ProductFactory.combo("Combo Almuerzo ligero", "Rápido y completo", 15, sandwich, americano));
        catalog.register("COM-03", ProductFactory.combo("Combo Dulce", "Para la tarde", 10, latte, torta));
        return catalog;
    }
}
