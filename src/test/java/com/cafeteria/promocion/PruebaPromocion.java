package com.cafeteria.promocion;

import com.cafeteria.SoportePruebas;
import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.decorador.DecoradorCafeAdicional;
import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.ConstructorPedido;
import com.cafeteria.utilidad.Dinero;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PruebaPromocion {

    private final Producto latte = FabricaProductos.bebida("Latte", "x", 10000);
    private final Producto pastel = FabricaProductos.alimento("Torta", "x", 20000);

    private final Clock martes16h = SoportePruebas.relojEn(2025, 11, 25, 16, 0);
    private final Clock martes9h = SoportePruebas.relojEn(2025, 11, 25, 9, 0);
    private final Clock sabado = SoportePruebas.relojEn(2025, 11, 29, 16, 0);

    private Pedido pedido(Object... productosYCantidades) {
        ConstructorPedido b = new ConstructorPedido();
        for (int i = 0; i < productosYCantidades.length; i += 2) {
            b.agregar((Producto) productosYCantidades[i], (Integer) productosYCantidades[i + 1]);
        }
        return b.previsualizar();
    }

    @Test
    void porcentajeAplicaSobreSubtotalYMinimo() {
        Promocion p = new PromocionPorcentaje("P", "d", 10, Dinero.de(25000));
        assertEquals(Dinero.CERO, p.descuentoPara(pedido(latte, 1)));
        assertEquals(Dinero.de(3000), p.descuentoPara(pedido(latte, 1, pastel, 1)));
    }

    @Test
    void montoFijoRequiereSubtotalMinimo() {
        Promocion p = new PromocionMontoFijo("F", "d", Dinero.de(5000), Dinero.de(30000));
        assertEquals(Dinero.CERO, p.descuentoPara(pedido(latte, 1)));
        assertEquals(Dinero.de(5000), p.descuentoPara(pedido(latte, 1, pastel, 1)));
    }

    @Test
    void compraNRecibeUnoDaUnidadesGratisPorDetalle() {
        Promocion p = new PromocionCompraNRecibeUno("X", "3x2", CategoriaProducto.BEBIDA, 2);
        assertEquals(Dinero.CERO, p.descuentoPara(pedido(latte, 2)));
        assertEquals(Dinero.de(10000), p.descuentoPara(pedido(latte, 3)));
        assertEquals(Dinero.de(20000), p.descuentoPara(pedido(latte, 6)));
        assertEquals(Dinero.CERO, p.descuentoPara(pedido(pastel, 3)));
    }

    @Test
    void rechazaParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new PromocionPorcentaje("P", "d", 0, Dinero.CERO));
        assertThrows(IllegalArgumentException.class, () -> new PromocionPorcentaje("P", "d", 101, Dinero.CERO));
        assertThrows(IllegalArgumentException.class, () -> new PromocionMontoFijo("F", "d", Dinero.CERO, Dinero.CERO));
        assertThrows(IllegalArgumentException.class, () -> new PromocionCompraNRecibeUno("B", "d", CategoriaProducto.ALIMENTO, 0));
    }

    @Test
    void promocionProductoSoloDescuentaCoincidencias() {
        Promocion porCategoria = PromocionProducto.paraCategoria("B", "d", 10, CategoriaProducto.BEBIDA);
        assertEquals(Dinero.de(1000), porCategoria.descuentoPara(pedido(latte, 1, pastel, 1)));
        Promocion porNombre = PromocionProducto.paraNombreProducto("L", "d", 50, "latte");
        assertEquals(Dinero.de(5000), porNombre.descuentoPara(pedido(latte, 1, pastel, 1)));
        assertEquals(Dinero.de(6250), porNombre.descuentoPara(pedido(new DecoradorCafeAdicional(latte), 1)));
    }

    @Test
    void horarioCompruebaDiasYHoras() {
        HorarioPromocion s = HorarioPromocion.de(Set.of(DayOfWeek.TUESDAY), LocalTime.of(15, 0), LocalTime.of(18, 0));
        assertTrue(s.estaActiva(LocalDateTime.of(2025, 11, 25, 15, 0)));
        assertTrue(s.estaActiva(LocalDateTime.of(2025, 11, 25, 17, 59)));
        assertFalse(s.estaActiva(LocalDateTime.of(2025, 11, 25, 18, 0)));
        assertFalse(s.estaActiva(LocalDateTime.of(2025, 11, 25, 14, 59)));
        assertFalse(s.estaActiva(LocalDateTime.of(2025, 11, 26, 16, 0)));
    }

    @Test
    void horarioAdmiteDiaCompletoYMedianoche() {
        assertTrue(HorarioPromocion.enDias(DayOfWeek.SATURDAY).estaActiva(LocalDateTime.of(2025, 11, 29, 3, 0)));
        assertFalse(HorarioPromocion.enDias(DayOfWeek.SATURDAY).estaActiva(LocalDateTime.of(2025, 11, 28, 12, 0)));
        HorarioPromocion noche = HorarioPromocion.de(Set.of(), LocalTime.of(22, 0), LocalTime.of(2, 0));
        assertTrue(noche.estaActiva(LocalDateTime.of(2025, 11, 25, 23, 0)));
        assertTrue(noche.estaActiva(LocalDateTime.of(2025, 11, 25, 1, 0)));
        assertFalse(noche.estaActiva(LocalDateTime.of(2025, 11, 25, 12, 0)));
        assertThrows(IllegalArgumentException.class, () -> HorarioPromocion.de(Set.of(), LocalTime.of(8, 0), null));
        assertThrows(IllegalArgumentException.class, () -> HorarioPromocion.de(Set.of(), LocalTime.of(8, 0), LocalTime.of(8, 0)));
    }

    @Test
    void promocionProgramadaSoloAplicaEnHorario() {
        Promocion base = new PromocionPorcentaje("H", "10%", 10, Dinero.CERO);
        HorarioPromocion horario = HorarioPromocion.de(Set.of(DayOfWeek.TUESDAY), LocalTime.of(15, 0), LocalTime.of(18, 0));
        Pedido o = pedido(latte, 1);
        assertEquals(Dinero.de(1000), new PromocionProgramada(base, horario, martes16h).descuentoPara(o));
        assertEquals(Dinero.CERO, new PromocionProgramada(base, horario, martes9h).descuentoPara(o));
        assertEquals(Dinero.CERO, new PromocionProgramada(base, horario, sabado).descuentoPara(o));
        assertTrue(new PromocionProgramada(base, horario, sabado).obtenerDescripcion().contains("15:00"));
    }

    @Test
    void motorEligeMejorPromocionAplicable() {
        MotorPromociones motor = MotorPromociones.conPredeterminados(martes9h);
        assertEquals("CAFE3X2", motor.mejorPara(pedido(latte, 3)).orElseThrow().obtenerCodigo());
        assertEquals("BIENVENIDA10", motor.mejorPara(pedido(pastel, 1)).orElseThrow().obtenerCodigo());
    }

    @Test
    void horaFelizSoloAplicaEnHorario() {
        Pedido bebidas = pedido(latte, 1);
        assertEquals(Dinero.de(1000), MotorPromociones.conPredeterminados(martes16h).buscarPorCodigo("happyhour").orElseThrow().descuentoPara(bebidas));
        assertEquals(Dinero.CERO, MotorPromociones.conPredeterminados(martes9h).buscarPorCodigo("happyhour").orElseThrow().descuentoPara(bebidas));
        assertEquals(Dinero.CERO, MotorPromociones.conPredeterminados(sabado).buscarPorCodigo("happyhour").orElseThrow().descuentoPara(bebidas));
    }

    @Test
    void promocionComboSoloAplicaFinDeSemana() {
        Pedido o = pedido(FabricaProductos.combo("Combo", "x", 0, latte, pastel), 1);
        assertEquals(Dinero.de(4500), MotorPromociones.conPredeterminados(sabado).buscarPorCodigo("COMBOFINDE").orElseThrow().descuentoPara(o));
        assertEquals(Dinero.CERO, MotorPromociones.conPredeterminados(martes16h).buscarPorCodigo("COMBOFINDE").orElseThrow().descuentoPara(o));
    }

    @Test
    void motorRegistraBuscaYEliminaPromociones() {
        MotorPromociones motor = new MotorPromociones();
        motor.registrar(new PromocionPorcentaje("abc", "d", 10, Dinero.CERO));
        assertTrue(motor.buscarPorCodigo(" ABC ").isPresent());
        assertTrue(motor.buscarPorCodigo(null).isEmpty());
        assertTrue(motor.eliminar("Abc"));
        assertFalse(motor.eliminar("Abc"));
        assertTrue(motor.todos().isEmpty());
        assertTrue(motor.mejorPara(pedido(latte, 1)).isEmpty());
    }
}
