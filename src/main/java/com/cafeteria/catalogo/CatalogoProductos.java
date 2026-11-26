package com.cafeteria.catalogo;

import com.cafeteria.modelo.Producto;

import java.util.Collection;
import java.util.Optional;

public interface CatalogoProductos {

    void registrar(String codigo, Producto producto);

    void actualizar(String codigo, Producto producto);

    void eliminar(String codigo);

    Optional<Producto> buscarPorCodigo(String codigo);

    Collection<Entrada> entradas();

    record Entrada(String codigo, Producto producto) {
    }
}
