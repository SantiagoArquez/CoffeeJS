package com.cafeteria.modelo;

import java.io.Serializable;
import java.math.BigDecimal;

public interface Producto extends Serializable {

    String obtenerNombre();

    String obtenerDescripcion();

    BigDecimal obtenerPrecio();

    CategoriaProducto obtenerCategoria();
}
