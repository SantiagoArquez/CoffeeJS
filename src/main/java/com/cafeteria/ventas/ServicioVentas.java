package com.cafeteria.ventas;

import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.DetallePedido;
import com.cafeteria.utilidad.DirectorioDatos;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ServicioVentas {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RepositorioVentas repositorio;
    private final Clock reloj;

    public ServicioVentas(RepositorioVentas repositorio, Clock reloj) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public static ServicioVentas crearPredeterminado() {
        return new ServicioVentas(RepositorioVentasArchivo.abrir(DirectorioDatos.resolver()), Clock.systemDefaultZone());
    }

    public Venta registrarVenta(Pedido pedido) {
        for (Venta existente : repositorio.buscarTodos()) {
            if (existente.idPedido() == pedido.obtenerId()) {
                return existente;
            }
        }
        List<DetalleVenta> articulos = new ArrayList<>();
        for (DetallePedido linea : pedido.obtenerDetalles()) {
            articulos.add(new DetalleVenta(linea.producto().obtenerNombre(), linea.cantidad(), linea.producto().obtenerPrecio()));
        }
        Venta venta = new Venta(repositorio.siguienteIdVenta(), pedido.obtenerId(), pedido.obtenerNombreCliente(),
                LocalDateTime.now(reloj), pedido.subtotal(), pedido.descuento(), pedido.total(),
                pedido.obtenerPromocion().map(p -> p.obtenerCodigo()).orElse(""), articulos, 0);
        repositorio.guardar(venta);
        return venta;
    }

    public List<Venta> todos() {
        return repositorio.buscarTodos();
    }

    public List<Venta> ventasAbiertas() {
        return repositorio.buscarTodos().stream().filter(Venta::estaAbierta).toList();
    }

    public List<CierreCaja> cierres() {
        return repositorio.buscarTodosCierres();
    }

    public long ultimoIdPedido() {
        return repositorio.buscarTodos().stream().mapToLong(Venta::idPedido).max().orElse(0);
    }

    public CierreCaja cerrarCaja(String cajero) {
        if (cajero == null || cajero.isBlank()) {
            throw new IllegalArgumentException("El nombre del cajero es obligatorio");
        }
        List<Venta> abrir = ventasAbiertas();
        if (abrir.isEmpty()) {
            throw new IllegalStateException("No hay ventas abiertas para cerrar la caja");
        }
        BigDecimal subtotal = sumar(abrir, Venta::subtotal);
        BigDecimal descuentos = sumar(abrir, Venta::descuento);
        BigDecimal total = sumar(abrir, Venta::total);
        CierreCaja cierre = new CierreCaja(repositorio.siguienteIdCierre(), LocalDateTime.now(reloj),
                cajero.trim(), abrir.size(), subtotal, descuentos, total);
        repositorio.guardarCierre(cierre, abrir.stream().map(Venta::id).toList());
        return cierre;
    }

    public List<Venta> ventasEntre(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        return repositorio.buscarTodos().stream()
                .filter(s -> !s.fechaVenta().toLocalDate().isBefore(desde) && !s.fechaVenta().toLocalDate().isAfter(hasta))
                .toList();
    }

    public String informeVentas(LocalDate desde, LocalDate hasta) {
        List<Venta> ventas = ventasEntre(desde, hasta);
        StringBuilder textoResultado = new StringBuilder();
        textoResultado.append("========================================\n");
        textoResultado.append(String.format("  REPORTE DE VENTAS %s - %s%n", desde.format(FECHA), hasta.format(FECHA)));
        textoResultado.append("========================================\n");
        if (ventas.isEmpty()) {
            textoResultado.append("No hay ventas registradas en el período.\n");
            return textoResultado.toString();
        }
        BigDecimal total = sumar(ventas, Venta::total);
        textoResultado.append(String.format("Ventas:           %d%n", ventas.size()));
        textoResultado.append(String.format("Subtotal:         %s%n", Dinero.formatear(sumar(ventas, Venta::subtotal))));
        textoResultado.append(String.format("Descuentos:       %s%n", Dinero.formatear(sumar(ventas, Venta::descuento))));
        textoResultado.append(String.format("Total vendido:    %s%n", Dinero.formatear(total)));
        textoResultado.append(String.format("Ticket promedio:  %s%n", Dinero.formatear(
                total.divide(BigDecimal.valueOf(ventas.size()), 0, java.math.RoundingMode.HALF_UP))));

        Map<String, Integer> unidades = new LinkedHashMap<>();
        Map<String, Integer> promos = new LinkedHashMap<>();
        for (Venta venta : ventas) {
            venta.articulos().forEach(i -> unidades.merge(i.nombre(), i.cantidad(), Integer::sum));
            if (!venta.codigoPromocion().isEmpty()) {
                promos.merge(venta.codigoPromocion(), 1, Integer::sum);
            }
        }
        textoResultado.append("\nProductos más vendidos:\n");
        unidades.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(5)
                .forEach(e -> textoResultado.append(String.format("  %dx %s%n", e.getValue(), e.getKey())));
        if (!promos.isEmpty()) {
            textoResultado.append("\nPromociones usadas:\n");
            promos.forEach((codigo, n) -> textoResultado.append(String.format("  %s: %d vez/veces%n", codigo, n)));
        }
        textoResultado.append("\nDetalle:\n");
        ventas.stream().sorted(Comparator.comparing(Venta::fechaVenta)).forEach(s -> textoResultado.append(String.format(
                "  #%d  %s  %-15s %s%n", s.idPedido(), s.fechaVenta().format(FECHA_HORA), s.cliente(), Dinero.formatear(s.total()))));
        return textoResultado.toString();
    }

    public String informeDiario(LocalDate dia) {
        return informeVentas(dia, dia);
    }

    public String informeCierre(CierreCaja cierre) {
        return "========================================\n"
                + String.format("  CIERRE DE CAJA #%d%n", cierre.id())
                + String.format("  %s - Cajero: %s%n", cierre.fechaCierre().format(FECHA_HORA), cierre.cajero())
                + "========================================\n"
                + String.format("Ventas incluidas: %d%n", cierre.cantidadVentas())
                + String.format("Subtotal:         %s%n", Dinero.formatear(cierre.subtotal()))
                + String.format("Descuentos:       %s%n", Dinero.formatear(cierre.descuentos()))
                + String.format("TOTAL RECAUDADO:  %s%n", Dinero.formatear(cierre.total()))
                + "========================================";
    }

    private static BigDecimal sumar(List<Venta> ventas, java.util.function.Function<Venta, BigDecimal> campo) {
        return ventas.stream().map(campo).reduce(Dinero.CERO, BigDecimal::add);
    }
}
