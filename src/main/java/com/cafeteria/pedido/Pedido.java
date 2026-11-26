package com.cafeteria.pedido;

import com.cafeteria.promocion.Promocion;
import com.cafeteria.utilidad.Dinero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class Pedido {

    private final long id;
    private String nombreCliente;
    private List<DetallePedido> detalles;
    private Promocion promocion;
    private final LocalDateTime fechaCreacion;
    private EstadoPedido estado;
    private BigDecimal descuento;

    Pedido(long id, String nombreCliente, List<DetallePedido> detalles, Promocion promocion) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.detalles = List.copyOf(detalles);
        this.promocion = promocion;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoPedido.CREADO;
        this.descuento = calcularDescuento();
    }

    Pedido(long id, String nombreCliente, List<DetallePedido> detalles, Promocion promocion,
          LocalDateTime fechaCreacion, EstadoPedido estado, BigDecimal descuento) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.detalles = List.copyOf(detalles);
        this.promocion = promocion;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.descuento = descuento;
    }

    private BigDecimal calcularDescuento() {
        return promocion == null ? Dinero.CERO : promocion.descuentoPara(this).min(subtotal());
    }

    public LocalDateTime obtenerFechaCreacion() {
        return fechaCreacion;
    }

    public long obtenerId() {
        return id;
    }

    public String obtenerNombreCliente() {
        return nombreCliente;
    }

    public List<DetallePedido> obtenerDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public EstadoPedido obtenerEstado() {
        return estado;
    }

    void establecerEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    void reemplazarContenido(Pedido reemplazo) {
        this.nombreCliente = reemplazo.nombreCliente;
        this.detalles = reemplazo.detalles;
        this.promocion = reemplazo.promocion;
        this.descuento = reemplazo.descuento;
    }

    public Optional<Promocion> obtenerPromocion() {
        return Optional.ofNullable(promocion);
    }

    public BigDecimal subtotal() {
        return detalles.stream().map(DetallePedido::total).reduce(Dinero.CERO, BigDecimal::add);
    }

    public BigDecimal descuento() {
        return descuento;
    }

    public BigDecimal total() {
        return subtotal().subtract(descuento());
    }

    public String recibo() {
        StringBuilder textoResultado = new StringBuilder();
        textoResultado.append("========================================\n");
        textoResultado.append(id == 0
                ? String.format("  BORRADOR DE PEDIDO - %s%n", nombreCliente)
                : String.format("  PEDIDO #%d - %s%n", id, nombreCliente));
        textoResultado.append("  ").append(fechaCreacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append('\n');
        textoResultado.append("========================================\n");
        for (DetallePedido linea : detalles) {
            textoResultado.append(String.format("%dx %s%n", linea.cantidad(), linea.producto().obtenerNombre()));
            textoResultado.append(String.format("      %s c/u  ->  %s%n", Dinero.formatear(linea.producto().obtenerPrecio()),
                    Dinero.formatear(linea.total())));
        }
        textoResultado.append("----------------------------------------\n");
        textoResultado.append(String.format("Subtotal:   %s%n", Dinero.formatear(subtotal())));
        if (promocion != null) {
            textoResultado.append(String.format("Promo %s: -%s%n", promocion.obtenerCodigo(), Dinero.formatear(descuento())));
        }
        textoResultado.append(String.format("TOTAL:      %s%n", Dinero.formatear(total())));
        textoResultado.append(String.format("Estado:     %s%n", estado.obtenerEtiqueta()));
        textoResultado.append("========================================");
        return textoResultado.toString();
    }
}
