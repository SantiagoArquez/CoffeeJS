package com.cafeteria.catalogo;

import com.cafeteria.modelo.Producto;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;

public final class CatalogoProductosMemoria implements CatalogoProductos {

    private final Map<String, Producto> productos = new LinkedHashMap<>();

    @Override
    public void registrar(String codigo, Producto producto) {
        String clave = normalizar(codigo);
        validarProducto(producto);
        if (productos.containsKey(clave)) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + clave);
        }
        productos.put(clave, producto);
    }

    @Override
    public void actualizar(String codigo, Producto producto) {
        String clave = normalizar(codigo);
        validarProducto(producto);
        exigirExistente(clave);
        productos.put(clave, producto);
    }

    @Override
    public void eliminar(String codigo) {
        String clave = normalizar(codigo);
        exigirExistente(clave);
        productos.remove(clave);
    }

    private String normalizar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
    }

    private void exigirExistente(String clave) {
        if (!productos.containsKey(clave)) {
            throw new IllegalArgumentException("No existe el producto " + clave);
        }
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(productos.get(normalizar(codigo)));
    }

    @Override
    public Collection<Entrada> entradas() {
        return productos.entrySet().stream().map(e -> new Entrada(e.getKey(), e.getValue())).toList();
    }
}
