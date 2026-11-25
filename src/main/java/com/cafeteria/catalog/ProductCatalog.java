package com.cafeteria.catalog;

import com.cafeteria.model.Product;

import java.util.Collection;
import java.util.Optional;

/**
 * Repositorio del menú: separa las reglas del negocio de la forma en que se almacenan
 * los productos (capa de persistencia). Cubre RF01, RF02 y RF03.
 *
 * <p>Cada producto se identifica con un código único (por ejemplo {@code BEB-01}) que no
 * distingue mayúsculas de minúsculas.</p>
 */
public interface ProductCatalog {

    /**
     * Registra un producto nuevo.
     *
     * @param code    código único
     * @param product producto a guardar
     * @throws IllegalArgumentException si el código está vacío o ya existe, o el producto es nulo
     */
    void register(String code, Product product);

    /**
     * Reemplaza un producto existente.
     *
     * @param code    código del producto a modificar
     * @param product nueva versión
     * @throws IllegalArgumentException si el código no existe
     */
    void update(String code, Product product);

    /**
     * Elimina un producto del menú.
     *
     * @param code código del producto
     * @throws IllegalArgumentException si el código no existe
     */
    void remove(String code);

    /**
     * @param code código a buscar
     * @return el producto, o vacío si no existe o el código es nulo/vacío
     */
    Optional<Product> findByCode(String code);

    /** @return todas las entradas del menú en orden de registro */
    Collection<Entry> entries();

    /**
     * Par código-producto para recorrer el menú.
     *
     * @param code    código normalizado (mayúsculas)
     * @param product producto asociado
     */
    record Entry(String code, Product product) {
    }
}
