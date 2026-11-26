package com.cafeteria.promocion;

import com.cafeteria.SoportePruebas;
import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.ConstructorPedido;
import com.cafeteria.utilidad.Dinero;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PruebaConfiguracionPromocion {

    private final Clock martes16h = SoportePruebas.relojEn(2025, 11, 25, 16, 0);
    private final Producto latte = FabricaProductos.bebida("Latte", "x", 10000);

    private Pedido pedido(int cantidadPedida) {
        return new ConstructorPedido().agregar(latte, cantidadPedida).previsualizar();
    }

    @Test
    void cadaTipoConstruyeSuEstrategia() {
        assertEquals(Dinero.de(1000), ConfiguracionPromocion.porcentaje("a", "d", 10, 0).aPromocion(martes16h).descuentoPara(pedido(1)));
        assertEquals(Dinero.de(500), ConfiguracionPromocion.montoFijo("b", "d", 500, 0).aPromocion(martes16h).descuentoPara(pedido(1)));
        assertEquals(Dinero.de(10000), ConfiguracionPromocion.compraNRecibeUno("c", "d", CategoriaProducto.BEBIDA, 2).aPromocion(martes16h).descuentoPara(pedido(3)));
        assertEquals(Dinero.de(2000), ConfiguracionPromocion.porcentajeCategoria("d", "d", 20, CategoriaProducto.BEBIDA).aPromocion(martes16h).descuentoPara(pedido(1)));
    }

    @Test
    void rechazaConfiguracionesInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionPromocion.porcentaje("", "d", 10, 0));
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionPromocion.porcentaje("con espacio", "d", 10, 0));
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionPromocion.porcentaje("a", " ", 10, 0));
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionPromocion.porcentaje("a", "d", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionPromocion.porcentaje("a", "d", 101, 0));
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionPromocion.porcentaje("a", "d", 10, -1));
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionPromocion.montoFijo("a", "d", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionPromocion.compraNRecibeUno("a", "d", CategoriaProducto.ALIMENTO, 0));
        assertThrows(IllegalArgumentException.class, () -> ConfiguracionPromocion.compraNRecibeUno("a", "d", null, 2));
        assertThrows(IllegalArgumentException.class, () ->
                ConfiguracionPromocion.porcentaje("a", "d", 10, 0).conHorario(Set.of(), LocalTime.of(8, 0), null));
    }

    @Test
    void codigoSeNormalizaAMayusculas() {
        assertEquals("ABC", ConfiguracionPromocion.porcentaje(" abc ", "d", 10, 0).codigo());
    }

    @Test
    void configuracionProgramadaSoloAplicaEnHorario() {
        ConfiguracionPromocion configuracion = ConfiguracionPromocion.porcentaje("a", "d", 10, 0)
                .conHorario(Set.of(DayOfWeek.TUESDAY), LocalTime.of(15, 0), LocalTime.of(18, 0));
        assertTrue(configuracion.estaProgramada());
        assertEquals(Dinero.de(1000), configuracion.aPromocion(martes16h).descuentoPara(pedido(1)));
        assertEquals(Dinero.CERO, configuracion.aPromocion(SoportePruebas.relojEn(2025, 11, 25, 20, 0)).descuentoPara(pedido(1)));
        assertFalse(ConfiguracionPromocion.porcentaje("a", "d", 10, 0).estaProgramada());
    }

    @Test
    void codificacionLineaConservaDatos() {
        ConfiguracionPromocion configuracion = ConfiguracionPromocion.porcentajeCategoria("HH", "Happy hour", 15, CategoriaProducto.COMBO)
                .conHorario(Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), LocalTime.of(9, 30), LocalTime.of(12, 0));
        assertEquals(configuracion, ConfiguracionPromocion.desdeLinea(configuracion.aLinea()));
        ConfiguracionPromocion simpleTexto = ConfiguracionPromocion.montoFijo("X", "Fijo", 5000, 30000);
        assertEquals(simpleTexto, ConfiguracionPromocion.desdeLinea(simpleTexto.aLinea()));
        assertThrows(IllegalStateException.class, () -> ConfiguracionPromocion.desdeLinea("basura"));
    }

    @Test
    void motorPersistenteInicializaYConservaCambios(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("promociones.tsv");
        MotorPromociones primero = MotorPromociones.cargar(archivo, martes16h);
        assertTrue(Files.exists(archivo));
        assertEquals(5, primero.todos().size());
        primero.definir(ConfiguracionPromocion.porcentaje("NAVIDAD", "Navidad", 20, 10000));
        assertTrue(primero.eliminar("cafe3x2"));

        MotorPromociones segundo = MotorPromociones.cargar(archivo, martes16h);
        assertEquals(5, segundo.todos().size());
        assertTrue(segundo.buscarPorCodigo("navidad").isPresent());
        assertTrue(segundo.buscarPorCodigo("CAFE3X2").isEmpty());
        assertEquals(Dinero.de(1000), segundo.buscarPorCodigo("HAPPYHOUR").orElseThrow().descuentoPara(pedido(1)));
    }

    @Test
    void redefinirCodigoReemplazaPromocion(@TempDir Path carpeta) {
        MotorPromociones motor = MotorPromociones.cargar(carpeta.resolve("p.tsv"), martes16h);
        motor.definir(ConfiguracionPromocion.porcentaje("BIENVENIDA10", "Nueva", 50, 0));
        assertEquals(5, motor.todos().size());
        assertEquals(Dinero.de(5000), motor.buscarPorCodigo("BIENVENIDA10").orElseThrow().descuentoPara(pedido(1)));
        assertEquals(5, MotorPromociones.cargar(carpeta.resolve("p.tsv"), martes16h).todos().size());
    }

    @Test
    void registrarPromocionDirectamenteNoPersiste(@TempDir Path carpeta) {
        Path archivo = carpeta.resolve("p.tsv");
        MotorPromociones motor = MotorPromociones.cargar(archivo, martes16h);
        motor.registrar(new PromocionPorcentaje("TEMP", "temporal", 5, Dinero.CERO));
        assertTrue(motor.buscarPorCodigo("TEMP").isPresent());
        assertTrue(MotorPromociones.cargar(archivo, martes16h).buscarPorCodigo("TEMP").isEmpty());
    }

    @Test
    void informaArchivoPromocionesCorrupto(@TempDir Path carpeta) throws Exception {
        Path archivo = carpeta.resolve("p.tsv");
        Files.writeString(archivo, "esto no es una promocion\n");
        assertThrows(RuntimeException.class, () -> MotorPromociones.cargar(archivo, martes16h));
    }

    @Test
    void actividadReflejaHorario() {
        assertTrue(MotorPromociones.conPredeterminados(martes16h).buscarPorCodigo("HAPPYHOUR").orElseThrow().estaActivaAhora());
        assertFalse(MotorPromociones.conPredeterminados(SoportePruebas.relojEn(2025, 11, 25, 9, 0)).buscarPorCodigo("HAPPYHOUR").orElseThrow().estaActivaAhora());
        assertTrue(MotorPromociones.conPredeterminados(martes16h).buscarPorCodigo("BIENVENIDA10").orElseThrow().estaActivaAhora());
    }
}
