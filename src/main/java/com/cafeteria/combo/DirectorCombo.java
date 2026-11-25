package com.cafeteria.combo;

import com.cafeteria.model.Combo;
import com.cafeteria.model.Product;

import java.util.Objects;

/**
 * Coordina el orden de los pasos de construcción de combos usando un {@link ComboBuilder}.
 *
 * <p><b>Patrón Builder (rol Director):</b> conoce las "recetas" habituales de la cafetería
 * (bebida + alimento, combo completo, lista libre de productos) pero no sabe cómo se
 * construye el combo: eso lo decide el builder que recibe. Así el menú, la consola y la
 * interfaz gráfica no repiten la secuencia de pasos.</p>
 */
public final class DirectorCombo {

    private final ComboBuilder builder;

    /**
     * @param builder builder que ejecutará los pasos (obligatorio)
     */
    public DirectorCombo(ComboBuilder builder) {
        this.builder = Objects.requireNonNull(builder, "El builder es obligatorio");
    }

    /**
     * Receta de dos productos: una bebida y un alimento (por ejemplo, café + croissant).
     *
     * @param name        nombre del combo
     * @param description descripción comercial
     * @param discount    descuento porcentual
     * @param drink       bebida
     * @param food        alimento
     * @return el combo construido
     */
    public Combo drinkAndFood(String name, String description, int discount, Product drink, Product food) {
        return builder.reset().name(name).description(description).discountPercent(discount)
                .addDrink(drink).addMain(food).build();
    }

    /**
     * Receta completa: producto principal, bebida, acompañamiento y complementos opcionales.
     *
     * @param name        nombre del combo
     * @param description descripción comercial
     * @param discount    descuento porcentual
     * @param main        producto principal
     * @param drink       bebida
     * @param side        acompañamiento
     * @param extras      complementos adicionales (puede estar vacío)
     * @return el combo construido
     */
    public Combo complete(String name, String description, int discount,
                          Product main, Product drink, Product side, Product... extras) {
        builder.reset().name(name).description(description).discountPercent(discount)
                .addMain(main).addDrink(drink).addSide(side);
        for (Product extra : extras) {
            builder.addExtra(extra);
        }
        return builder.build();
    }

    /**
     * Receta libre: cualquier lista de productos sin exigir roles.
     *
     * @param name        nombre del combo
     * @param description descripción comercial
     * @param discount    descuento porcentual
     * @param items       productos incluidos (mínimo dos)
     * @return el combo construido
     */
    public Combo ofItems(String name, String description, int discount, Product... items) {
        builder.reset().name(name).description(description).discountPercent(discount);
        for (Product item : items) {
            builder.addItem(item);
        }
        return builder.build();
    }
}
