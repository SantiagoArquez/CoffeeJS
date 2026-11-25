package com.cafeteria.order;

import com.cafeteria.notification.CashierNotifier;
import com.cafeteria.notification.KitchenNotifier;
import com.cafeteria.notification.WaiterNotifier;
import com.cafeteria.sales.SalesLedger;
import com.cafeteria.sales.SalesService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio de aplicación para gestionar pedidos: registrarlos, modificarlos y cambiar su
 * estado (RF08, RF09 y RF10). Es el núcleo que comparten la consola y la interfaz gráfica.
 *
 * <p><b>Patrón Observer (rol Subject):</b> mantiene la lista de {@link OrderListener} y los
 * avisa en cada cambio. Al construirse registra por sí mismo los observadores internos:
 * {@link KitchenNotifier}, {@link CashierNotifier}, {@link WaiterNotifier} y
 * {@link SalesLedger}; las interfaces pueden añadir los suyos con
 * {@link #addListener(OrderListener)}.</p>
 */
public final class OrderService {

    private final Map<Long, Order> orders = new LinkedHashMap<>();
    private final List<OrderListener> listeners = new ArrayList<>();
    private final SalesService sales;
    private final KitchenNotifier kitchen = new KitchenNotifier();
    private final CashierNotifier cashier = new CashierNotifier();
    private final WaiterNotifier waiter = new WaiterNotifier();
    private long sequence;

    /**
     * Crea el servicio con el almacenamiento de ventas por defecto (archivos en la carpeta
     * de datos). Es el constructor que usa la aplicación.
     */
    public OrderService() {
        this(SalesService.createDefault());
    }

    /**
     * Crea el servicio con un servicio de ventas específico (útil para pruebas).
     *
     * @param sales servicio donde se registran las ventas
     */
    public OrderService(SalesService sales) {
        this.sales = Objects.requireNonNull(sales);
        this.sequence = sales.lastOrderId();
        listeners.add(kitchen);
        listeners.add(cashier);
        listeners.add(waiter);
        listeners.add(new SalesLedger(sales));
    }

    /**
     * Suscribe un observador a los cambios de los pedidos.
     *
     * @param listener observador a registrar
     */
    public void addListener(OrderListener listener) {
        listeners.add(listener);
    }

    /**
     * Cancela la suscripción de un observador.
     *
     * @param listener observador a retirar
     */
    public void removeListener(OrderListener listener) {
        listeners.remove(listener);
    }

    /** @return bandeja de avisos del área de cocina */
    public KitchenNotifier kitchen() {
        return kitchen;
    }

    /** @return bandeja de avisos del área de caja */
    public CashierNotifier cashier() {
        return cashier;
    }

    /** @return bandeja de avisos del área de meseros */
    public WaiterNotifier waiter() {
        return waiter;
    }

    /** @return servicio de ventas, cierre de caja y reportes */
    public SalesService sales() {
        return sales;
    }

    /**
     * Registra un pedido nuevo y avisa a los observadores.
     *
     * @param builder pedido armado
     * @return el pedido registrado, con su número y en estado «Creado»
     * @throws IllegalStateException si el pedido no tiene productos
     */
    public Order place(OrderBuilder builder) {
        Order order = builder.build(sequence + 1);
        sequence++;
        orders.put(order.getId(), order);
        listeners.forEach(l -> l.onStatusChanged(order, null, order.getStatus()));
        return order;
    }

    /**
     * Modifica el contenido de un pedido (cliente, productos, cantidades o promoción)
     * mientras esté creado o en preparación (RF09). El estado no cambia.
     *
     * @param id      número del pedido
     * @param builder nuevo contenido, normalmente obtenido con {@link OrderBuilder#from(Order)}
     * @return el pedido modificado
     * @throws IllegalArgumentException si el pedido no existe
     * @throws IllegalStateException    si el pedido ya está listo, entregado o cancelado, o el nuevo contenido está vacío
     */
    public Order modify(long id, OrderBuilder builder) {
        Order order = require(id);
        if (!order.getStatus().allowsModification()) {
            throw new IllegalStateException("El pedido #" + id + " ya no puede modificarse (estado: "
                    + order.getStatus().getLabel() + ")");
        }
        order.replaceContent(builder.build(id));
        listeners.forEach(l -> l.onOrderModified(order));
        return order;
    }

    /**
     * @param id número del pedido
     * @return el pedido, o vacío si no existe
     */
    public Optional<Order> find(long id) {
        return Optional.ofNullable(orders.get(id));
    }

    /** @return todos los pedidos de esta sesión en orden de creación */
    public Collection<Order> all() {
        return List.copyOf(orders.values());
    }

    /**
     * Pasa el pedido al siguiente estado del flujo normal.
     *
     * @param id número del pedido
     * @return el pedido actualizado
     * @throws IllegalArgumentException si no existe
     * @throws IllegalStateException    si ya está en un estado final
     */
    public Order advance(long id) {
        Order order = require(id);
        OrderStatus next = order.getStatus().next()
                .orElseThrow(() -> new IllegalStateException(
                        "El pedido #" + id + " ya está en estado final (" + order.getStatus().getLabel() + ")"));
        return changeStatus(order, next);
    }

    /**
     * Cancela un pedido que todavía no está listo.
     *
     * @param id número del pedido
     * @return el pedido cancelado
     * @throws IllegalArgumentException si no existe
     * @throws IllegalStateException    si el estado actual no permite cancelar
     */
    public Order cancel(long id) {
        return changeStatus(require(id), OrderStatus.CANCELLED);
    }

    /** Valida la transición, actualiza el estado y avisa a los observadores. */
    private Order changeStatus(Order order, OrderStatus target) {
        OrderStatus previous = order.getStatus();
        if (!previous.canTransitionTo(target)) {
            throw new IllegalStateException("No se puede pasar de '" + previous.getLabel()
                    + "' a '" + target.getLabel() + "'");
        }
        order.setStatus(target);
        listeners.forEach(l -> l.onStatusChanged(order, previous, target));
        return order;
    }

    /** Busca un pedido o lanza una excepción descriptiva. */
    private Order require(long id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("No existe el pedido #" + id));
    }
}
