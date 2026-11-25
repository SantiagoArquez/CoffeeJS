package com.cafeteria.catalog;

import com.cafeteria.combo.ComboPersonalizadoBuilder;
import com.cafeteria.combo.DirectorCombo;
import com.cafeteria.model.Combo;
import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.model.SimpleProduct;
import com.cafeteria.util.Money;

/**
 * Fábrica de productos que traduce los datos escritos por el usuario (texto) en objetos
 * del dominio, validándolos (RNF07). Es el punto de entrada común de la consola, la
 * interfaz gráfica y el menú por defecto.
 *
 * <p>Los combos se construyen con el patrón Builder
 * ({@link DirectorCombo} + {@link ComboPersonalizadoBuilder}).</p>
 */
public final class ProductFactory {

    private ProductFactory() {
    }

    /**
     * @param name        nombre
     * @param description descripción
     * @param price       precio en pesos
     * @return una bebida simple
     */
    public static Product beverage(String name, String description, long price) {
        return new SimpleProduct(name, description, Money.of(price), ProductCategory.BEVERAGE);
    }

    /**
     * @param name        nombre
     * @param description descripción
     * @param price       precio en pesos
     * @return un alimento simple
     */
    public static Product food(String name, String description, long price) {
        return new SimpleProduct(name, description, Money.of(price), ProductCategory.FOOD);
    }

    /**
     * Crea un producto simple a partir de texto escrito por el usuario.
     *
     * @param name        nombre obligatorio
     * @param description descripción obligatoria
     * @param price       precio entero en pesos, mayor que cero, sin separadores
     * @param category    categoría
     * @return el producto creado
     * @throws IllegalArgumentException si la descripción está vacía o el precio no es válido
     */
    public static Product simple(String name, String description, String price, ProductCategory category) {
        validateDescription(description);
        long amount;
        try {
            amount = Long.parseLong(price == null ? "" : price.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresa un precio entero en pesos, sin separadores");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        return new SimpleProduct(name, description.trim(), Money.of(amount), category);
    }

    /**
     * Reconstruye un combo existente con nuevo nombre, descripción y descuento,
     * conservando sus productos.
     *
     * @param combo       combo original
     * @param name        nuevo nombre
     * @param description nueva descripción obligatoria
     * @param discount    nuevo descuento como texto entero entre 0 y 100
     * @return el combo actualizado
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public static Product editCombo(Combo combo, String name, String description, String discount) {
        validateDescription(description);
        int percent;
        try {
            percent = Integer.parseInt(discount == null ? "" : discount.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresa un descuento entero entre 0 y 100");
        }
        return new DirectorCombo(new ComboPersonalizadoBuilder())
                .ofItems(name, description.trim(), percent, combo.getItems().toArray(Product[]::new));
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
    }

    /**
     * Crea un combo con la receta libre del {@link DirectorCombo}.
     *
     * @param name            nombre
     * @param description     descripción
     * @param discountPercent descuento entre 0 y 100
     * @param items           al menos dos productos
     * @return el combo construido
     */
    public static Product combo(String name, String description, int discountPercent, Product... items) {
        return new DirectorCombo(new ComboPersonalizadoBuilder()).ofItems(name, description, discountPercent, items);
    }
}
