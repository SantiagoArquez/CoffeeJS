package com.cafeteria.combo;

import com.cafeteria.model.Combo;
import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementación concreta de {@link ComboBuilder}: arma combos personalizados con los
 * productos que elija el administrador.
 *
 * <p><b>Patrón Builder (rol ConcreteBuilder).</b> Valida el rol de cada producto al
 * agregarlo (por ejemplo, una bebida no puede entrar como producto principal) y deja que
 * {@link Combo} valide el conjunto final. Un mismo builder puede reutilizarse llamando a
 * {@link #reset()}. No es seguro para uso concurrente.</p>
 */
public final class ComboPersonalizadoBuilder implements ComboBuilder {

    private String name;
    private String description = "";
    private int discountPercent;
    private final List<Product> items = new ArrayList<>();

    @Override
    public ComboBuilder reset() {
        name = null;
        description = "";
        discountPercent = 0;
        items.clear();
        return this;
    }

    @Override
    public ComboBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ComboBuilder description(String description) {
        this.description = description == null ? "" : description;
        return this;
    }

    @Override
    public ComboBuilder discountPercent(int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        this.discountPercent = percent;
        return this;
    }

    @Override
    public ComboBuilder addMain(Product product) {
        return addWithCategory(product, ProductCategory.FOOD, "El producto principal");
    }

    @Override
    public ComboBuilder addDrink(Product product) {
        return addWithCategory(product, ProductCategory.BEVERAGE, "La bebida");
    }

    @Override
    public ComboBuilder addSide(Product product) {
        return addWithCategory(product, ProductCategory.FOOD, "El acompañamiento");
    }

    @Override
    public ComboBuilder addExtra(Product product) {
        Objects.requireNonNull(product, "El producto es obligatorio");
        if (product.getCategory() == ProductCategory.COMBO) {
            throw new IllegalArgumentException("Un complemento no puede ser un combo");
        }
        items.add(product);
        return this;
    }

    @Override
    public ComboBuilder addItem(Product product) {
        items.add(Objects.requireNonNull(product, "El producto es obligatorio"));
        return this;
    }

    @Override
    public Combo build() {
        return new Combo(name, description, items, discountPercent);
    }

    private ComboBuilder addWithCategory(Product product, ProductCategory expected, String role) {
        Objects.requireNonNull(product, "El producto es obligatorio");
        if (product.getCategory() != expected) {
            throw new IllegalArgumentException(role + " debe ser de categoría " + expected.getLabel()
                    + " (recibido: " + product.getCategory().getLabel() + ")");
        }
        items.add(product);
        return this;
    }
}
