package com.cafeteria.decorator;

import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;

import java.util.List;

/**
 * Catálogo de complementos disponibles. Para ofrecer un complemento nuevo basta con
 * crear su decorador y registrarlo aquí, sin tocar los productos existentes
 * (principio Open/Closed).
 */
public final class CustomizationCatalog {

    private static final List<CustomizationOption> OPTIONS = List.of(
            new CustomizationOption(ExtraShotDecorator.LABEL, ExtraShotDecorator.PRICE,
                    ProductCategory.BEVERAGE, ExtraShotDecorator::new),
            new CustomizationOption(AlternativeMilkDecorator.LABEL, AlternativeMilkDecorator.PRICE,
                    ProductCategory.BEVERAGE, AlternativeMilkDecorator::new),
            new CustomizationOption(SyrupDecorator.LABEL, SyrupDecorator.PRICE,
                    ProductCategory.BEVERAGE, SyrupDecorator::new),
            new CustomizationOption(WhippedCreamDecorator.LABEL, WhippedCreamDecorator.PRICE,
                    ProductCategory.BEVERAGE, WhippedCreamDecorator::new),
            new CustomizationOption(ExtraCheeseDecorator.LABEL, ExtraCheeseDecorator.PRICE,
                    ProductCategory.FOOD, ExtraCheeseDecorator::new)
    );

    private CustomizationCatalog() {
    }

    /**
     * Lista los complementos que se pueden aplicar a un producto según su categoría.
     *
     * @param product producto que se quiere personalizar
     * @return complementos compatibles (vacía, por ejemplo, para combos)
     */
    public static List<CustomizationOption> availableFor(Product product) {
        return OPTIONS.stream().filter(o -> o.category() == product.getCategory()).toList();
    }
}
