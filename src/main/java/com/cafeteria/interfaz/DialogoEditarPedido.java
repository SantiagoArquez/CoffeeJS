package com.cafeteria.interfaz;

import com.cafeteria.catalogo.CatalogoProductos;
import com.cafeteria.modelo.Producto;
import com.cafeteria.pedido.Pedido;
import com.cafeteria.pedido.ConstructorPedido;
import com.cafeteria.pedido.DetallePedido;
import com.cafeteria.pedido.ServicioPedidos;
import com.cafeteria.promocion.Promocion;
import com.cafeteria.promocion.MotorPromociones;
import com.cafeteria.utilidad.Dinero;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

final class DialogoEditarPedido extends JDialog {

    private final ServicioPedidos pedidos;
    private final MotorPromociones promociones;
    private final Pedido pedido;
    private final UnaryOperator<Producto> personalizador;
    private final Runnable alGuardar;
    private final List<CatalogoProductos.Entrada> menu;
    private final List<Promocion> listaPromos;
    private final List<DetallePedido> detalles;
    private final JTextField cliente;
    private final JComboBox<String> promo = new JComboBox<>();
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final DefaultTableModel modeloMenu = modelo("Código", "Producto", "Precio");
    private final DefaultTableModel modeloDetalles = modelo("Producto", "Cantidad", "Total");
    private final JTable tablaMenu = new JTable(modeloMenu);
    private final JTable tablaDetalles = new JTable(modeloDetalles);
    private final JLabel total = new JLabel(" ");

    DialogoEditarPedido(JFrame propietario, CatalogoProductos catalogo, MotorPromociones promociones, ServicioPedidos pedidos,
                    Pedido pedido, UnaryOperator<Producto> personalizador, Runnable alGuardar) {
        super(propietario, "Modificar pedido #" + pedido.obtenerId(), true);
        this.pedidos = pedidos;
        this.promociones = promociones;
        this.pedido = pedido;
        this.personalizador = personalizador;
        this.alGuardar = alGuardar;
        this.menu = List.copyOf(catalogo.entradas());
        this.listaPromos = List.copyOf(promociones.todos());
        this.detalles = new ArrayList<>(pedido.obtenerDetalles());
        this.cliente = new JTextField(pedido.obtenerNombreCliente(), 18);

        for (CatalogoProductos.Entrada e : menu) {
            modeloMenu.addRow(new Object[]{e.codigo(), e.producto().obtenerNombre(), Dinero.formatear(e.producto().obtenerPrecio())});
        }
        promo.addItem("Automática");
        promo.addItem("Sin promoción");
        for (int i = 0; i < listaPromos.size(); i++) {
            promo.addItem(listaPromos.get(i).obtenerCodigo());

        }
        promo.setSelectedIndex(0);
        promo.addActionListener(e -> actualizarDetalles());
        for (JTable t : List.of(tablaMenu, tablaDetalles)) {
            t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            t.setRowHeight(25);
        }

        JPanel raiz = new JPanel(new BorderLayout(8, 8));
        raiz.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        superior.add(new JLabel("Cliente:")); superior.add(cliente);
        superior.add(new JLabel("   Estado: " + pedido.obtenerEstado().obtenerEtiqueta()));
        raiz.add(superior, BorderLayout.NORTH);

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.setBorder(BorderFactory.createTitledBorder("Menú"));
        izquierda.add(new JScrollPane(tablaMenu));
        JPanel agregar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        agregar.add(new JLabel("Cantidad:")); agregar.add(cantidad);
        agregar.add(boton("Agregar", this::agregarProducto));
        izquierda.add(agregar, BorderLayout.SOUTH);
        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setBorder(BorderFactory.createTitledBorder("Contenido del pedido"));
        derecha.add(new JScrollPane(tablaDetalles));
        JPanel eliminar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        eliminar.add(boton("Quitar línea", this::eliminarDetalle));
        derecha.add(eliminar, BorderLayout.SOUTH);
        JSplitPane dividirTexto = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izquierda, derecha);
        dividirTexto.setResizeWeight(.5);
        raiz.add(dividirTexto, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inferior.add(new JLabel("Promoción:")); inferior.add(promo);
        total.setFont(total.getFont().deriveFont(Font.BOLD, 15f));
        inferior.add(total);
        inferior.add(boton("Guardar cambios", this::guardar));
        JButton cancelar = new JButton("Cancelar");
        cancelar.addActionListener(e -> dispose());
        inferior.add(cancelar);
        raiz.add(inferior, BorderLayout.SOUTH);

        setContentPane(raiz);
        setSize(900, 520);
        setLocationRelativeTo(propietario);
        actualizarDetalles();
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    private JButton boton(String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> {
            try {
                accion.run();
            } catch (IllegalArgumentException | IllegalStateException excepcion) {
                JOptionPane.showMessageDialog(this, excepcion.getMessage(), "CoffeeJS", JOptionPane.WARNING_MESSAGE);
            }
        });
        return b;
    }

    private void agregarProducto() {
        int fila = tablaMenu.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Selecciona un producto del menú");
        Producto producto = personalizador.apply(menu.get(tablaMenu.convertRowIndexToModel(fila)).producto());
        if (producto == null) return;
        detalles.add(new DetallePedido(producto, (Integer) cantidad.getValue()));
        actualizarDetalles();
    }

    private void eliminarDetalle() {
        int fila = tablaDetalles.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Selecciona una línea del pedido");
        detalles.remove(fila);
        actualizarDetalles();
    }

    private ConstructorPedido constructor() {
        ConstructorPedido constructor = new ConstructorPedido().cliente(cliente.getText());
        for (DetallePedido linea : detalles) constructor.agregar(linea.producto(), linea.cantidad());
        if (promo.getSelectedIndex() == 0) constructor.promocionAutomatica(promociones);
        else if (promo.getSelectedIndex() > 1) constructor.promocion(listaPromos.get(promo.getSelectedIndex() - 2));
        return constructor;
    }

    private void actualizarDetalles() {
        modeloDetalles.setRowCount(0);
        for (DetallePedido linea : detalles) {
            modeloDetalles.addRow(new Object[]{linea.producto().obtenerNombre(), linea.cantidad(), Dinero.formatear(linea.total())});
        }
        if (detalles.isEmpty()) {
            total.setText("   Total: $0");
            return;
        }
        Pedido borrador = constructor().previsualizar();
        total.setText("   Total: " + Dinero.formatear(borrador.total())
                + (promo.getSelectedIndex() > 1 && borrador.descuento().signum() == 0 ? "  (la promoción no aplica)" : borrador.obtenerPromocion().map(p -> "  " + p.obtenerCodigo()).orElse("")));
    }

    private void guardar() {
        ConstructorPedido constructor = constructor();
        if (detalles.isEmpty()) throw new IllegalStateException("El pedido debe conservar al menos un producto; usa «Cancelar pedido» si ya no se necesita");
        if (promo.getSelectedIndex() > 1 && constructor.previsualizar().descuento().signum() == 0) constructor.promocion(null);
        pedidos.modificar(pedido.obtenerId(), constructor);
        alGuardar.run();
        dispose();
    }
}
