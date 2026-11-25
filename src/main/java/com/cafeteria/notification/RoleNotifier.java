package com.cafeteria.notification;

import com.cafeteria.order.Order;
import com.cafeteria.order.OrderListener;
import com.cafeteria.order.OrderStatus;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base de los observadores por área. Guarda en una bandeja los avisos que le interesan a
 * su rol y los entrega a los suscriptores (por ejemplo, una pantalla) que se registren
 * con {@link #addSink(Consumer)}.
 *
 * <p><b>Patrón Observer (rol ConcreteObserver):</b> cada subclase decide qué cambios de
 * estado le competen mediante {@link #messageFor(Order, OrderStatus, OrderStatus)}; así
 * se agrega un área nueva (por ejemplo, domicilios) creando otra subclase, sin modificar
 * el pedido ni su servicio (Open/Closed).</p>
 */
public abstract class RoleNotifier implements OrderListener {

    private final String recipient;
    private final Clock clock;
    private final List<Notification> inbox = new ArrayList<>();
    private final List<Consumer<Notification>> sinks = new ArrayList<>();

    /**
     * @param recipient nombre del área destinataria
     * @param clock     reloj para fechar los avisos
     */
    protected RoleNotifier(String recipient, Clock clock) {
        this.recipient = recipient;
        this.clock = clock;
    }

    /**
     * Decide si un cambio de estado interesa a esta área.
     *
     * @param order    pedido que cambió
     * @param previous estado anterior, o {@code null} si el pedido acaba de crearse
     * @param current  estado actual
     * @return el texto del aviso, o {@code null} si el área no debe ser notificada
     */
    protected abstract String messageFor(Order order, OrderStatus previous, OrderStatus current);

    /**
     * Texto del aviso cuando se modifica el contenido de un pedido. Por defecto el área no
     * es notificada.
     *
     * @param order pedido modificado
     * @return el texto del aviso, o {@code null} para ignorarlo
     */
    protected String messageForModification(Order order) {
        return null;
    }

    @Override
    public final void onStatusChanged(Order order, OrderStatus previous, OrderStatus current) {
        deliver(order, messageFor(order, previous, current));
    }

    @Override
    public final void onOrderModified(Order order) {
        deliver(order, messageForModification(order));
    }

    private void deliver(Order order, String message) {
        if (message == null) {
            return;
        }
        Notification notification = new Notification(order.getId(), recipient, message, LocalDateTime.now(clock));
        inbox.add(notification);
        sinks.forEach(s -> s.accept(notification));
    }

    /**
     * Suscribe un consumidor que recibirá cada aviso nuevo apenas se genere.
     *
     * @param sink consumidor del aviso
     */
    public void addSink(Consumer<Notification> sink) {
        sinks.add(sink);
    }

    /** @return el nombre del área destinataria */
    public String recipient() {
        return recipient;
    }

    /** @return copia de todos los avisos recibidos, del más antiguo al más reciente */
    public List<Notification> inbox() {
        return List.copyOf(inbox);
    }

    /** Vacía la bandeja de avisos. */
    public void clear() {
        inbox.clear();
    }
}
