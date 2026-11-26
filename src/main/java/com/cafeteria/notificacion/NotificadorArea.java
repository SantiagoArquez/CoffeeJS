package com.cafeteria.notificacion;

import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.ObservadorPedido;
import com.cafeteria.pedido.EstadoPedido;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class NotificadorArea implements ObservadorPedido {

    private final String destinatario;
    private final Clock reloj;
    private final List<Notificacion> bandeja = new ArrayList<>();
    private final List<Consumer<Notificacion>> receptores = new ArrayList<>();

    protected NotificadorArea(String destinatario, Clock reloj) {
        this.destinatario = destinatario;
        this.reloj = reloj;
    }

    protected abstract String mensajePara(Pedido pedido, EstadoPedido previo, EstadoPedido actual);

    protected String mensajeParaModificacion(Pedido pedido) {
        return null;
    }

    @Override
    public final void alCambiarEstado(Pedido pedido, EstadoPedido previo, EstadoPedido actual) {
        entregar(pedido, mensajePara(pedido, previo, actual));
    }

    @Override
    public final void alModificarPedido(Pedido pedido) {
        entregar(pedido, mensajeParaModificacion(pedido));
    }

    private void entregar(Pedido pedido, String mensaje) {
        if (mensaje == null) {
            return;
        }
        Notificacion notificacion = new Notificacion(pedido.obtenerId(), destinatario, mensaje, LocalDateTime.now(reloj));
        bandeja.add(notificacion);
        receptores.forEach(s -> s.accept(notificacion));
    }

    public void agregarReceptor(Consumer<Notificacion> receptor) {
        receptores.add(receptor);
    }

    public String destinatario() {
        return destinatario;
    }

    public List<Notificacion> bandeja() {
        return List.copyOf(bandeja);
    }

    public void limpiar() {
        bandeja.clear();
    }
}
