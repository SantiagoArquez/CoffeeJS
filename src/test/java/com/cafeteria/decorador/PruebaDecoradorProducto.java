package com.cafeteria.decorador;

import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.modelo.Producto;
import com.cafeteria.utilidad.Dinero;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PruebaDecoradorProducto {

    private final Producto latte = FabricaProductos.bebida("Latte", "Espresso con leche", 8000);
    private final Producto sandwich = FabricaProductos.alimento("Sándwich", "Pollo", 14000);

    @Test
    void decoradorAgregaPrecioYEtiqueta() {
        Producto cargaCafe = new DecoradorCafeAdicional(latte);
        assertEquals(Dinero.de(8000).add(DecoradorCafeAdicional.PRECIO), cargaCafe.obtenerPrecio());
        assertTrue(cargaCafe.obtenerNombre().contains(DecoradorCafeAdicional.ETIQUETA));
        assertTrue(cargaCafe.obtenerDescripcion().contains(DecoradorCafeAdicional.ETIQUETA));
    }

    @Test
    void decoradoresSePuedenApilarEnCualquierOrden() {
        Producto a = new DecoradorSirope(new DecoradorCafeAdicional(latte));
        Producto b = new DecoradorCafeAdicional(new DecoradorSirope(latte));
        assertEquals(Dinero.de(8000).add(DecoradorCafeAdicional.PRECIO).add(DecoradorSirope.PRECIO), a.obtenerPrecio());
        assertEquals(a.obtenerPrecio(), b.obtenerPrecio());
    }

    @Test
    void productoOriginalNoSeModifica() {
        new DecoradorCremaBatida(latte);
        assertEquals(Dinero.de(8000), latte.obtenerPrecio());
        assertEquals("Latte", latte.obtenerNombre());
    }

    @Test
    void productoDecoradoConservaCategoria() {
        assertEquals(latte.obtenerCategoria(), new DecoradorSirope(latte).obtenerCategoria());
    }

    @Test
    void extraBebidaNoAplicaAAlimento() {
        assertThrows(IllegalArgumentException.class, () -> new DecoradorSirope(sandwich));
    }

    @Test
    void extraAlimentoNoAplicaABebida() {
        assertThrows(IllegalArgumentException.class, () -> new DecoradorQuesoAdicional(latte));
        assertEquals(Dinero.de(14000).add(DecoradorQuesoAdicional.PRECIO), new DecoradorQuesoAdicional(sandwich).obtenerPrecio());
    }

    @Test
    void rechazaProductoNulo() {
        assertThrows(NullPointerException.class, () -> new DecoradorSirope(null));
    }

    @Test
    void catalogoOfreceOpcionesCompatibles() {
        assertTrue(CatalogoComplementos.disponiblesPara(latte).stream().noneMatch(o -> o.etiqueta().equals(DecoradorQuesoAdicional.ETIQUETA)));
        assertEquals(1, CatalogoComplementos.disponiblesPara(sandwich).size());
        Producto combo = FabricaProductos.combo("Combo", "x", 10, latte, sandwich);
        assertTrue(CatalogoComplementos.disponiblesPara(combo).isEmpty());
    }

    @Test
    void opcionAplicaDecorador() {
        OpcionPersonalizacion opcion = CatalogoComplementos.disponiblesPara(latte).get(0);
        assertEquals(latte.obtenerPrecio().add(opcion.precio()), opcion.aplicarA(latte).obtenerPrecio());
    }
}
