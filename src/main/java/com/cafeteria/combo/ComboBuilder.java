package com.cafeteria.combo;

import com.cafeteria.model.Combo;
import com.cafeteria.model.Product;

/**
 * Define los pasos para construir un {@link Combo} (RF06).
 *
 * <p><b>Patrón Builder (rol Builder):</b> separa el proceso de construcción del combo de
 * su representación final. En vez de un constructor con muchos parámetros, el combo se
 * arma paso a paso: nombre, descripción, descuento, producto principal, bebida,
 * acompañamiento y complementos. Todos los pasos devuelven el propio builder para poder
 * encadenarlos.</p>
 *
 * <p>Otras formas de construir combos (por ejemplo, combos sugeridos automáticamente)
 * solo requieren una nueva implementación de esta interfaz (Open/Closed).</p>
 */
public interface ComboBuilder {

    /**
     * Descarta todo lo configurado y deja el builder listo para armar otro combo.
     *
     * @return este builder
     */
    ComboBuilder reset();

    /**
     * @param name nombre del combo (obligatorio al construir)
     * @return este builder
     */
    ComboBuilder name(String name);

    /**
     * @param description descripción comercial opcional
     * @return este builder
     */
    ComboBuilder description(String description);

    /**
     * @param percent descuento entre 0 y 100 sobre la suma de los productos
     * @return este builder
     * @throws IllegalArgumentException si el valor está fuera de rango
     */
    ComboBuilder discountPercent(int percent);

    /**
     * Agrega el producto principal del combo (por ejemplo, una hamburguesa o un sándwich).
     *
     * @param product producto de categoría alimento
     * @return este builder
     * @throws IllegalArgumentException si no es un alimento
     */
    ComboBuilder addMain(Product product);

    /**
     * Agrega la bebida del combo.
     *
     * @param product producto de categoría bebida
     * @return este builder
     * @throws IllegalArgumentException si no es una bebida
     */
    ComboBuilder addDrink(Product product);

    /**
     * Agrega un acompañamiento (papas, postre, pan, etc.).
     *
     * @param product producto de categoría alimento
     * @return este builder
     * @throws IllegalArgumentException si no es un alimento
     */
    ComboBuilder addSide(Product product);

    /**
     * Agrega un complemento adicional (puede ser un producto ya personalizado con decoradores).
     *
     * @param product alimento o bebida
     * @return este builder
     * @throws IllegalArgumentException si es un combo
     */
    ComboBuilder addExtra(Product product);

    /**
     * Agrega cualquier producto sin exigir un rol concreto.
     *
     * @param product producto a incluir
     * @return este builder
     */
    ComboBuilder addItem(Product product);

    /**
     * Construye el combo con lo configurado hasta el momento.
     *
     * @return el combo validado, con su precio calculado automáticamente (RF07)
     * @throws IllegalArgumentException si falta el nombre o hay menos de dos productos
     */
    Combo build();
}
