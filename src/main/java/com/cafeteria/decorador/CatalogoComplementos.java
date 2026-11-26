package com.cafeteria.decorador;

import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;

import java.util.List;

public final class CatalogoComplementos {

    private static final List<OpcionPersonalizacion> OPCIONES = List.of(
            new OpcionPersonalizacion(DecoradorCafeAdicional.ETIQUETA, DecoradorCafeAdicional.PRECIO,
                    CategoriaProducto.BEBIDA, DecoradorCafeAdicional::new),
            new OpcionPersonalizacion(DecoradorLecheAlternativa.ETIQUETA, DecoradorLecheAlternativa.PRECIO,
                    CategoriaProducto.BEBIDA, DecoradorLecheAlternativa::new),
            new OpcionPersonalizacion(DecoradorSirope.ETIQUETA, DecoradorSirope.PRECIO,
                    CategoriaProducto.BEBIDA, DecoradorSirope::new),
            new OpcionPersonalizacion(DecoradorCremaBatida.ETIQUETA, DecoradorCremaBatida.PRECIO,
                    CategoriaProducto.BEBIDA, DecoradorCremaBatida::new),
            new OpcionPersonalizacion(DecoradorQuesoAdicional.ETIQUETA, DecoradorQuesoAdicional.PRECIO,
                    CategoriaProducto.ALIMENTO, DecoradorQuesoAdicional::new)
    );

    private CatalogoComplementos() {
    }

    public static List<OpcionPersonalizacion> disponiblesPara(Producto producto) {
        return OPCIONES.stream().filter(o -> o.categoria() == producto.obtenerCategoria()).toList();
    }
}
