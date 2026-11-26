package com.cafeteria.catalogo;

import com.cafeteria.decorador.DecoradorSirope;
import com.cafeteria.modelo.Combo;
import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.modelo.ProductoSimple;
import com.cafeteria.utilidad.Dinero;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PruebaCatalogoProductos {

    private final Producto latte = FabricaProductos.bebida("Latte", "x", 8000);
    private final Producto pastel = FabricaProductos.alimento("Torta", "x", 9000);

    @Test
    void registrarBuscarActualizarYEliminar() {
        CatalogoProductos catalogo = new CatalogoProductosMemoria();
        catalogo.registrar("beb-01", latte);
        assertSame(latte, catalogo.buscarPorCodigo("BEB-01").orElseThrow());
        catalogo.actualizar("BEB-01", pastel);
        assertSame(pastel, catalogo.buscarPorCodigo(" beb-01 ").orElseThrow());
        catalogo.eliminar("BEB-01");
        assertTrue(catalogo.buscarPorCodigo("BEB-01").isEmpty());
        assertTrue(catalogo.entradas().isEmpty());
    }

    @Test
    void rechazaCodigosDuplicadosAusentesYVacios() {
        CatalogoProductos catalogo = new CatalogoProductosMemoria();
        catalogo.registrar("A", latte);
        assertThrows(IllegalArgumentException.class, () -> catalogo.registrar("a", pastel));
        assertThrows(IllegalArgumentException.class, () -> catalogo.actualizar("B", pastel));
        assertThrows(IllegalArgumentException.class, () -> catalogo.eliminar("B"));
        assertThrows(IllegalArgumentException.class, () -> catalogo.registrar(" ", pastel));
        assertThrows(IllegalArgumentException.class, () -> catalogo.registrar("C", null));
        assertTrue(catalogo.buscarPorCodigo(null).isEmpty());
    }

    @Test
    void fabricaValidaDatosUsuario() {
        assertThrows(IllegalArgumentException.class, () -> FabricaProductos.sencillo("", "d", "100", CategoriaProducto.ALIMENTO));
        assertThrows(IllegalArgumentException.class, () -> FabricaProductos.sencillo("N", "", "100", CategoriaProducto.ALIMENTO));
        assertThrows(IllegalArgumentException.class, () -> FabricaProductos.sencillo("N", "d", "abc", CategoriaProducto.ALIMENTO));
        assertThrows(IllegalArgumentException.class, () -> FabricaProductos.sencillo("N", "d", "0", CategoriaProducto.ALIMENTO));
        assertThrows(IllegalArgumentException.class, () -> FabricaProductos.sencillo("N", "d", "-5", CategoriaProducto.ALIMENTO));
        assertEquals(Dinero.de(1500), FabricaProductos.sencillo(" N ", " d ", " 1500 ", CategoriaProducto.ALIMENTO).obtenerPrecio());
    }

    @Test
    void productoSimpleRechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new ProductoSimple("N", "d", Dinero.de(-1), CategoriaProducto.ALIMENTO));
        assertThrows(IllegalArgumentException.class, () -> new ProductoSimple("N", "d", Dinero.de(1), CategoriaProducto.COMBO));
        assertThrows(NullPointerException.class, () -> new ProductoSimple("N", "d", null, CategoriaProducto.ALIMENTO));
    }

    @Test
    void menuPredeterminadoTieneProductosEsperados() {
        CatalogoProductos menu = MenuPredeterminado.cargarPredeterminados();
        assertEquals(14, menu.entradas().size());
        assertTrue(menu.buscarPorCodigo("COM-01").orElseThrow() instanceof Combo);
    }

    @Test
    void menuSeInicializaYPersisteTrasReinicio(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("sub").resolve("menu.dat");
        CatalogoProductos primero = new CatalogoProductosArchivo(archivo, MenuPredeterminado.cargarPredeterminados());
        assertTrue(Files.exists(archivo));
        primero.registrar("BEB-99", FabricaProductos.bebida("Nuevo", "n", 3000));
        primero.actualizar("ALI-01", FabricaProductos.alimento("Croissant XL", "g", 7000));
        primero.eliminar("BEB-01");

        CatalogoProductos segundo = new CatalogoProductosArchivo(archivo, new CatalogoProductosMemoria());
        assertEquals(primero.entradas().size(), segundo.entradas().size());
        assertEquals("Nuevo", segundo.buscarPorCodigo("BEB-99").orElseThrow().obtenerNombre());
        assertEquals("Croissant XL", segundo.buscarPorCodigo("ALI-01").orElseThrow().obtenerNombre());
        assertTrue(segundo.buscarPorCodigo("BEB-01").isEmpty());
        Combo combo = (Combo) segundo.buscarPorCodigo("COM-01").orElseThrow();
        assertEquals(2, combo.obtenerArticulos().size());
        assertEquals(((Combo) primero.buscarPorCodigo("COM-01").orElseThrow()).obtenerPrecio(), combo.obtenerPrecio());
    }

    @Test
    void catalogoPersisteComboConProductosDecorados(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("menu.dat");
        CatalogoProductos primero = new CatalogoProductosArchivo(archivo, null);
        primero.registrar("COM-9", FabricaProductos.combo("Especial", "e", 5, new DecoradorSirope(latte), pastel));
        CatalogoProductos segundo = new CatalogoProductosArchivo(archivo, null);
        assertEquals(primero.buscarPorCodigo("COM-9").orElseThrow().obtenerPrecio(), segundo.buscarPorCodigo("COM-9").orElseThrow().obtenerPrecio());
    }

    @Test
    void informaArchivoMenuCorrupto(@TempDir Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("menu.dat");
        Files.writeString(archivo, "basura");
        assertThrows(RuntimeException.class, () -> new CatalogoProductosArchivo(archivo, null));
    }

    @Test
    void cargaPredeterminadosSiDirectorioEsUtilizable(@TempDir Path carpeta) {
        System.setProperty("coffeejs.data", carpeta.toString());
        try {
            CatalogoProductos menu = MenuPredeterminado.cargar();
            assertEquals(14, menu.entradas().size());
            assertTrue(Files.exists(carpeta.resolve(MenuPredeterminado.ARCHIVO_MENU)));
        } finally {
            System.clearProperty("coffeejs.data");
        }
    }
}
