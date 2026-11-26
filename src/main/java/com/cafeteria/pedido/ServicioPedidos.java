package com.cafeteria.pedido;

import com.cafeteria.notificacion.NotificadorCaja;
import com.cafeteria.notificacion.NotificadorCocina;
import com.cafeteria.notificacion.NotificadorMesero;
import com.cafeteria.ventas.RegistroVentas;
import com.cafeteria.ventas.ServicioVentas;
import com.cafeteria.utilidad.DirectorioDatos;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ServicioPedidos {

    private final Map<Long, Pedido> pedidos = new LinkedHashMap<>();
    private final List<ObservadorPedido> observadores = new ArrayList<>();
    private final ServicioVentas ventas;
    private final RegistroVentas registroVentas;
    private final RepositorioPedidos repositorio;
    private final NotificadorCocina cocina = new NotificadorCocina();
    private final NotificadorCaja cajero = new NotificadorCaja();
    private final NotificadorMesero mesero = new NotificadorMesero();
    private long secuencia;

    public ServicioPedidos() {
        this(ServicioVentas.crearPredeterminado(), repositorioPredeterminado());
    }

    public ServicioPedidos(ServicioVentas ventas) {
        this(ventas, RepositorioPedidos.ninguno());
    }

    public ServicioPedidos(ServicioVentas ventas, RepositorioPedidos repositorio) {
        this.ventas = Objects.requireNonNull(ventas);
        this.registroVentas = new RegistroVentas(ventas);
        this.repositorio = Objects.requireNonNull(repositorio);
        this.secuencia = ventas.ultimoIdPedido();
        for (Pedido guardado : repositorio.cargarTodos()) {
            pedidos.put(guardado.obtenerId(), guardado);
            secuencia = Math.max(secuencia, guardado.obtenerId());
        }
        observadores.add(cocina);
        observadores.add(cajero);
        observadores.add(mesero);
        for (Pedido guardado : pedidos.values()) {
            if (guardado.obtenerEstado() == EstadoPedido.ENTREGADO) ventas.registrarVenta(guardado);
        }

    }

    private static RepositorioPedidos repositorioPredeterminado() {
        return new RepositorioPedidosArchivo(DirectorioDatos.resolver());
    }

    private void persistir() {
        try {
            repositorio.guardarTodos(pedidos.values());
        } catch (UncheckedIOException e) {
            throw new IllegalStateException("No se pudieron guardar los pedidos. No se confirmó el cambio.", e);
        }
    }

    public void agregarObservador(ObservadorPedido observador) {
        observadores.add(observador);
    }

    public void eliminarObservador(ObservadorPedido observador) {
        observadores.remove(observador);
    }

    public NotificadorCocina cocina() {
        return cocina;
    }

    public NotificadorCaja cajero() {
        return cajero;
    }

    public NotificadorMesero mesero() {
        return mesero;
    }

    public ServicioVentas ventas() {
        return ventas;
    }

    public Pedido registrarPedido(ConstructorPedido constructor) {
        Pedido pedido = constructor.construir(secuencia + 1);
        pedidos.put(pedido.obtenerId(), pedido);
        try { persistir(); } catch (RuntimeException e) { pedidos.remove(pedido.obtenerId()); throw e; }
        secuencia++;
        observadores.forEach(l -> l.alCambiarEstado(pedido, null, pedido.obtenerEstado()));
        return pedido;
    }

    public Pedido modificar(long id, ConstructorPedido constructor) {
        Pedido pedido = exigir(id);
        if (!pedido.obtenerEstado().permiteModificacion()) {
            throw new IllegalStateException("El pedido #" + id + " ya no puede modificarse (estado: "
                    + pedido.obtenerEstado().obtenerEtiqueta() + ")");
        }
        Pedido anterior = new Pedido(id, pedido.obtenerNombreCliente(), pedido.obtenerDetalles(), pedido.obtenerPromocion().orElse(null),
                pedido.obtenerFechaCreacion(), pedido.obtenerEstado(), pedido.descuento());
        pedido.reemplazarContenido(constructor.construir(id));
        try { persistir(); } catch (RuntimeException e) { pedido.reemplazarContenido(anterior); throw e; }
        observadores.forEach(l -> l.alModificarPedido(pedido));
        return pedido;
    }

    public Optional<Pedido> buscar(long id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    public Collection<Pedido> todos() {
        return List.copyOf(pedidos.values());
    }

    public List<Pedido> pedidosActivos() {
        var cerrado = ventas.todos().stream().filter(s -> !s.estaAbierta())
                .map(s -> s.idPedido()).collect(java.util.stream.Collectors.toSet());
        return pedidos.values().stream()
                .filter(o -> o.obtenerEstado() != EstadoPedido.CANCELADO)
                .filter(o -> o.obtenerEstado() != EstadoPedido.ENTREGADO || !cerrado.contains(o.obtenerId()))
                .toList();
    }

    public List<Pedido> pedidosArchivados() {
        var activo = pedidosActivos().stream().map(Pedido::obtenerId)
                .collect(java.util.stream.Collectors.toSet());
        return pedidos.values().stream().filter(o -> !activo.contains(o.obtenerId())).toList();
    }

    public Pedido avanzar(long id) {
        Pedido pedido = exigir(id);
        EstadoPedido siguiente = pedido.obtenerEstado().siguiente()
                .orElseThrow(() -> new IllegalStateException(
                        "El pedido #" + id + " ya está en estado final (" + pedido.obtenerEstado().obtenerEtiqueta() + ")"));
        return cambiarEstado(pedido, siguiente);
    }

    public Pedido cancelar(long id) {
        return cambiarEstado(exigir(id), EstadoPedido.CANCELADO);
    }

    private Pedido cambiarEstado(Pedido pedido, EstadoPedido destino) {
        EstadoPedido previo = pedido.obtenerEstado();
        if (!previo.puedeCambiarA(destino)) {
            throw new IllegalStateException("No se puede pasar de '" + previo.obtenerEtiqueta()
                    + "' a '" + destino.obtenerEtiqueta() + "'");
        }
        pedido.establecerEstado(destino);
        try { persistir(); } catch (RuntimeException e) { pedido.establecerEstado(previo); throw e; }
        if (destino == EstadoPedido.ENTREGADO) {
            try { registroVentas.alCambiarEstado(pedido, previo, destino); }
            catch (RuntimeException e) {
                pedido.establecerEstado(previo);
                try { persistir(); }
                catch (RuntimeException reversion) {
                    pedido.establecerEstado(destino);
                    e.addSuppressed(reversion);
                    throw new IllegalStateException("No se pudo registrar la venta ni revertir el pedido. Reinicia para recuperar la venta pendiente.", e);
                }
                throw new IllegalStateException("No se pudo registrar la venta. El pedido conserva su estado anterior.", e);
            }
        }
        observadores.forEach(l -> l.alCambiarEstado(pedido, previo, destino));
        return pedido;
    }

    private Pedido exigir(long id) {
        return buscar(id).orElseThrow(() -> new IllegalArgumentException("No existe el pedido #" + id));
    }
}
