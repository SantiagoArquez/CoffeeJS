package com.cafeteria.combo;

import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.decorador.DecoradorCafeAdicional;
import com.cafeteria.modelo.Combo;
import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.utilidad.Dinero;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PruebaConstructorCombo {

    private final Producto hamburguesa = FabricaProductos.alimento("Hamburguesa", "Res", 18000);
    private final Producto papas = FabricaProductos.alimento("Papas", "Fritas", 6000);
    private final Producto cola = FabricaProductos.bebida("Gaseosa", "Fría", 4000);

    @Test
    void construyePasoAPasoYCalculaPrecio() {
        Combo combo = new ConstructorComboPersonalizado()
                .nombre("Combo Burger").descripcion("Completo").porcentajeDescuento(10)
                .agregarPrincipal(hamburguesa).agregarBebida(cola).agregarAcompanamiento(papas).construir();
        assertEquals(3, combo.obtenerArticulos().size());
        assertEquals(Dinero.de(25200), combo.obtenerPrecio());
        assertEquals(CategoriaProducto.COMBO, combo.obtenerCategoria());
        assertEquals(10, combo.obtenerPorcentajeDescuento());
    }

    @Test
    void validacionRechazaCategoriaIncorrecta() {
        ConstructorCombo constructor = new ConstructorComboPersonalizado();
        assertThrows(IllegalArgumentException.class, () -> constructor.agregarBebida(hamburguesa));
        assertThrows(IllegalArgumentException.class, () -> constructor.agregarPrincipal(cola));
        assertThrows(IllegalArgumentException.class, () -> constructor.agregarAcompanamiento(cola));
    }

    @Test
    void extrasAceptanDecoradosPeroNoCombos() {
        ConstructorCombo constructor = new ConstructorComboPersonalizado();
        Producto interno = FabricaProductos.combo("Interno", "", 0, hamburguesa, cola);
        assertThrows(IllegalArgumentException.class, () -> constructor.agregarExtra(interno));
        constructor.agregarExtra(new DecoradorCafeAdicional(cola));
        constructor.nombre("Con extra").agregarPrincipal(hamburguesa);
        assertEquals(2, constructor.construir().obtenerArticulos().size());
    }

    @Test
    void rechazaDatosInvalidos() {
        ConstructorCombo constructor = new ConstructorComboPersonalizado();
        assertThrows(IllegalArgumentException.class, () -> constructor.porcentajeDescuento(101));
        assertThrows(IllegalArgumentException.class, () -> constructor.porcentajeDescuento(-1));
        assertThrows(IllegalArgumentException.class, constructor::construir);
        constructor.nombre("Solo uno").agregarPrincipal(hamburguesa);
        assertThrows(IllegalArgumentException.class, constructor::construir);
    }

    @Test
    void reiniciarLimpiaConfiguracionPrevia() {
        ConstructorCombo constructor = new ConstructorComboPersonalizado().nombre("A").agregarPrincipal(hamburguesa).agregarBebida(cola).porcentajeDescuento(20);
        constructor.reiniciar();
        assertThrows(IllegalArgumentException.class, constructor::construir);
        Combo combo = constructor.nombre("B").agregarPrincipal(hamburguesa).agregarBebida(cola).construir();
        assertEquals(0, combo.obtenerPorcentajeDescuento());
    }

    @Test
    void directorConstruyeRecetasPredeterminadas() {
        DirectorCombo director = new DirectorCombo(new ConstructorComboPersonalizado());
        Combo sencillo = director.bebidaYAlimento("Gaseosa y papas", "Rápido", 10, cola, papas);
        assertEquals(2, sencillo.obtenerArticulos().size());
        assertEquals(cola, sencillo.obtenerArticulos().get(0));

        Combo completo = director.completo("Completo", "Todo", 15, hamburguesa, cola, papas, new DecoradorCafeAdicional(cola));
        assertEquals(4, completo.obtenerArticulos().size());

        Combo gratis = director.deArticulos("Libre", "", 0, hamburguesa, papas, cola);
        assertEquals(3, gratis.obtenerArticulos().size());
    }

    @Test
    void directorReutilizaConstructorSinConservarEstado() {
        DirectorCombo director = new DirectorCombo(new ConstructorComboPersonalizado());
        director.completo("Uno", "", 50, hamburguesa, cola, papas);
        Combo segundo = director.bebidaYAlimento("Dos", "", 0, cola, papas);
        assertEquals(2, segundo.obtenerArticulos().size());
        assertEquals(0, segundo.obtenerPorcentajeDescuento());
    }

    @Test
    void fabricaDelegaAlConstructor() {
        Producto combo = FabricaProductos.combo("Combo", "x", 10, hamburguesa, cola);
        assertEquals(Dinero.de(19800), combo.obtenerPrecio());
        Producto editado = FabricaProductos.editarCombo((Combo) combo, "Nuevo", "y", "20");
        assertEquals(Dinero.de(17600), editado.obtenerPrecio());
        assertEquals("Nuevo", editado.obtenerNombre());
        assertThrows(IllegalArgumentException.class, () -> FabricaProductos.editarCombo((Combo) combo, "N", "y", "abc"));
    }

    @Test
    void recalculaPrecioComboPorComponentes() {
        Combo combo = new ConstructorComboPersonalizado().nombre("C").agregarBebida(cola).agregarPrincipal(papas).construir();
        assertEquals(Dinero.de(10000), combo.obtenerPrecio());
        assertTrue(combo.obtenerDescripcion().contains("Gaseosa"));
    }
}
