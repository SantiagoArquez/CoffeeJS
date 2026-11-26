package com.cafeteria.interfaz;

import com.cafeteria.catalogo.CatalogoProductos;
import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.modelo.Combo;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.decorador.CatalogoComplementos;
import com.cafeteria.decorador.OpcionPersonalizacion;
import com.cafeteria.modelo.Producto;
import com.cafeteria.pedido.*;
import com.cafeteria.promocion.*;
import com.cafeteria.utilidad.Dinero;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class AplicacionGrafica extends JFrame implements ObservadorPedido {
    private final CatalogoProductos catalogo;
    private final MotorPromociones promociones;
    private final ServicioPedidos pedidos;
    private final List<DetallePedido> carrito = new ArrayList<>();
    private List<CatalogoProductos.Entrada> menu = List.of();
    private List<Promocion> listaPromos = List.of();
    private boolean actualizandoPromos;
    private PanelVentas panelVentas;
    private final JCheckBox historial = new JCheckBox("Mostrar historial");
    private final JTextField cliente = new JTextField(18);
    private final JComboBox<String> promo = new JComboBox<>();
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final DefaultTableModel modeloMenu = modelo("Código", "Producto", "Categoría", "Precio", "Descripción");
    private final DefaultTableModel modeloCarrito = modelo("Producto", "Descripción", "Cantidad", "Total");
    private final DefaultTableModel modeloPedido = modelo("Pedido", "Cliente", "Estado", "Total");
    private final DefaultTableModel modeloPromos = modelo("Código", "Descripción", "Vigencia");
    private final JTable tablaAdministracion = new JTable(modeloMenu);
    private final JTable tablaMenu = new JTable(modeloMenu);
    private final JTable tablaCarrito = new JTable(modeloCarrito);
    private final JTable tablaPedidos = new JTable(modeloPedido);
    private final JTextArea recibo = new JTextArea(12, 36);
    private final JTextArea notificaciones = new JTextArea(7, 40);
    private final JLabel total = new JLabel("Total: $0");
    private final JLabel indicacion = new JLabel(" ");

    public AplicacionGrafica(CatalogoProductos catalogo, MotorPromociones promociones, ServicioPedidos pedidos) {
        super("CoffeeJS - Gestión de cafetería");
        this.catalogo = catalogo;
        this.promociones = promociones;
        this.pedidos = pedidos;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 600));
        setSize(1100, 740);
        setLocationRelativeTo(null);
        for (JTable tabla : List.of(tablaMenu, tablaAdministracion, tablaCarrito, tablaPedidos)) {
            tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tabla.setRowHeight(25);
        }
        recibo.setEditable(false);
        recibo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        notificaciones.setEditable(false);
        actualizarMenu();
        actualizarPromociones();
        promo.addActionListener(e -> { if (!actualizandoPromos) actualizarCarrito(); });
        panelVentas = new PanelVentas(this, pedidos, this::actualizarPedidos);
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Nuevo pedido", panelNuevoPedido());
        pestanas.addTab("Pedidos registrados", panelPedidos());
        pestanas.addTab("Promociones", panelPromociones());
        pestanas.addTab("Gestionar productos", panelProductos());
        pestanas.addTab("Notificaciones por área", new PanelNotificaciones(pedidos));
        pestanas.addTab("Ventas y caja", panelVentas);
        pestanas.addChangeListener(e -> { actualizarPromociones(); panelVentas.actualizarVista(); actualizarPedidos(); });
        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel titulo = new JLabel("CoffeeJS | Gestión de cafetería");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        raiz.add(titulo, BorderLayout.NORTH);
        raiz.add(pestanas, BorderLayout.CENTER);
        JPanel avisos = new JPanel(new BorderLayout());
        avisos.setBorder(BorderFactory.createTitledBorder("Notificaciones internas"));
        avisos.add(new JScrollPane(notificaciones));
        raiz.add(avisos, BorderLayout.SOUTH);
        setContentPane(raiz);
        pedidos.agregarObservador(this);
        actualizarPedidos();
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    private JButton boton(String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> {
            try { accion.run(); }
            catch (IllegalArgumentException | IllegalStateException excepcion) {
                JOptionPane.showMessageDialog(this, excepcion.getMessage(), "CoffeeJS", JOptionPane.WARNING_MESSAGE);
            }
        });
        return b;
    }

    private JPanel panelNuevoPedido() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        superior.add(new JLabel("Cliente:")); superior.add(cliente);
        panel.add(superior, BorderLayout.NORTH);
        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.setBorder(BorderFactory.createTitledBorder("Menú disponible"));
        izquierda.add(new JScrollPane(tablaMenu));
        JPanel agregar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        agregar.add(new JLabel("Cantidad:")); agregar.add(cantidad);
        agregar.add(boton("Agregar producto", this::agregarProducto));
        izquierda.add(agregar, BorderLayout.SOUTH);
        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setBorder(BorderFactory.createTitledBorder("Pedido actual"));
        derecha.add(new JScrollPane(tablaCarrito));
        JPanel acciones = new JPanel();
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.Y_AXIS));
        JPanel eliminar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        eliminar.add(boton("Quitar línea", this::eliminarDetalle));
        eliminar.add(boton("Vaciar", () -> { carrito.clear(); actualizarCarrito(); }));
        acciones.add(eliminar);
        JPanel promos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        promos.add(new JLabel("Promoción:")); promos.add(promo);
        acciones.add(promos); acciones.add(indicacion);
        total.setFont(total.getFont().deriveFont(Font.BOLD, 17f));
        acciones.add(total);
        JPanel confirmar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        confirmar.add(boton("Ver recibo", () -> mostrarRecibo(constructor().previsualizar())));
        confirmar.add(boton("Confirmar pedido", this::confirmarPedido));
        acciones.add(confirmar); derecha.add(acciones, BorderLayout.SOUTH);
        JSplitPane dividirTexto = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, izquierda, derecha);
        dividirTexto.setResizeWeight(.55); panel.add(dividirTexto);
        return panel;
    }

    private JPanel panelPedidos() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JSplitPane dividirTexto = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tablaPedidos), new JScrollPane(recibo));
        dividirTexto.setResizeWeight(.55); panel.add(dividirTexto);
        tablaPedidos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tablaPedidos.getSelectedRow();
                recibo.setText(fila < 0 ? "" : pedidos.buscar((Long) modeloPedido.getValueAt(fila, 0)).map(Pedido::recibo).orElse(""));
                recibo.setCaretPosition(0);
            }
        });
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        historial.addActionListener(e -> actualizarPedidos());
        botones.add(historial);
        botones.add(boton("Avanzar estado", () -> pedidos.avanzar(pedidoSeleccionado())));
        botones.add(boton("Modificar pedido", this::modificarPedido));
        botones.add(boton("Cancelar pedido", () -> {
            long id = pedidoSeleccionado();
            if (JOptionPane.showConfirmDialog(this, "¿Cancelar pedido #" + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) pedidos.cancelar(id);
        }));
        panel.add(botones, BorderLayout.SOUTH); return panel;
    }

    private JPanel panelProductos() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JScrollPane(tablaAdministracion), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(boton("Registrar", () -> dialogoProducto(null)));
        acciones.add(boton("Crear combo", () -> dialogoCombo(null)));
        acciones.add(boton("Modificar", () -> dialogoProducto(productoSeleccionado())));
        acciones.add(boton("Eliminar", () -> {
            CatalogoProductos.Entrada entrada = productoSeleccionado();
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar " + entrada.producto().obtenerNombre() + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                catalogo.eliminar(entrada.codigo());
                actualizarMenu();
            }
        }));
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private CatalogoProductos.Entrada productoSeleccionado() {
        int fila = tablaAdministracion.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Selecciona un producto");
        return menu.get(tablaAdministracion.convertRowIndexToModel(fila));
    }

    private void dialogoProducto(CatalogoProductos.Entrada entrada) {
        Producto actual = entrada == null ? null : entrada.producto();
        if (actual instanceof Combo) { dialogoCombo(entrada); return; }
        JTextField codigo = new JTextField(entrada == null ? "" : entrada.codigo(), 18);
        codigo.setEditable(entrada == null);
        JTextField nombre = new JTextField(actual == null ? "" : actual.obtenerNombre(), 18);
        JTextField descripcion = new JTextField(actual == null ? "" : actual.obtenerDescripcion(), 18);
        JTextField precio = new JTextField(actual == null ? "" : actual.obtenerPrecio().toPlainString(), 18);
        JComboBox<String> categoria = new JComboBox<>(new String[]{"Bebida", "Alimento"});
        if (actual != null && actual.obtenerCategoria() == CategoriaProducto.ALIMENTO) categoria.setSelectedIndex(1);
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Código:")); formulario.add(codigo);
        formulario.add(new JLabel("Nombre:")); formulario.add(nombre);
        formulario.add(new JLabel("Descripción:")); formulario.add(descripcion);
        formulario.add(new JLabel("Precio en pesos:")); formulario.add(precio);
        formulario.add(new JLabel("Categoría:")); formulario.add(categoria);
        while (JOptionPane.showConfirmDialog(this, formulario, entrada == null ? "Registrar producto" : "Modificar producto", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Producto producto = FabricaProductos.sencillo(nombre.getText(), descripcion.getText(), precio.getText(), categoria.getSelectedIndex() == 0 ? CategoriaProducto.BEBIDA : CategoriaProducto.ALIMENTO);
                if (entrada == null) catalogo.registrar(codigo.getText(), producto);
                else catalogo.actualizar(entrada.codigo(), producto);
                actualizarMenu();
                return;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void dialogoCombo(CatalogoProductos.Entrada entrada) {
        Combo previo = entrada == null ? null : (Combo) entrada.producto();
        DialogoCombo dialogo = new DialogoCombo(this, catalogo, entrada == null ? "" : entrada.codigo(), previo, this::personalizarProducto);
        dialogo.setVisible(true);
        if (dialogo.resultado() != null) {
            if (entrada == null) catalogo.registrar(dialogo.codigo(), dialogo.resultado());
            else catalogo.actualizar(entrada.codigo(), dialogo.resultado());
            actualizarMenu();
        }
    }

    private void actualizarMenu() {
        menu = List.copyOf(catalogo.entradas());
        modeloMenu.setRowCount(0);
        for (var entrada : menu) {
            Producto p = entrada.producto();
            modeloMenu.addRow(new Object[]{entrada.codigo(), p.obtenerNombre(), p.obtenerCategoria().obtenerEtiqueta(), Dinero.formatear(p.obtenerPrecio()), p.obtenerDescripcion()});
        }
    }

    private JPanel panelPromociones() {
        JTable tabla = new JTable(modeloPromos);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(25);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(boton("Crear promoción", () -> {
            ConfiguracionPromocion configuracion = DialogoPromocion.preguntar(this);
            if (configuracion != null) {
                promociones.definir(configuracion);
                actualizarPromociones();
            }
        }));
        acciones.add(boton("Eliminar", () -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) throw new IllegalArgumentException("Selecciona una promoción");
            String codigo = (String) modeloPromos.getValueAt(fila, 0);
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar la promoción " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                promociones.eliminar(codigo);
                actualizarPromociones();
            }
        }));
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private void actualizarPromociones() {
        actualizandoPromos = true;
        try {
            int indiceAnterior = promo.getSelectedIndex();
            String seleccionado = indiceAnterior > 1 ? listaPromos.get(indiceAnterior - 2).obtenerCodigo() : null;
            listaPromos = List.copyOf(promociones.todos());
            promo.removeAllItems();
            promo.addItem("Automática");
            promo.addItem("Sin promoción");
            int indice = indiceAnterior == 1 ? 1 : 0;
            for (int i = 0; i < listaPromos.size(); i++) {
                promo.addItem(listaPromos.get(i).obtenerCodigo());
                if (listaPromos.get(i).obtenerCodigo().equalsIgnoreCase(seleccionado)) indice = i + 2;
            }
            promo.setSelectedIndex(indice);
            modeloPromos.setRowCount(0);
            for (Promocion p : listaPromos) {
                modeloPromos.addRow(new Object[]{p.obtenerCodigo(), p.obtenerDescripcion(), p.estaActivaAhora() ? "Vigente" : "Fuera de horario"});
            }
        } finally {
            actualizandoPromos = false;
        }
        actualizarCarrito();
    }

    private void modificarPedido() {
        long id = pedidoSeleccionado();
        Pedido pedido = pedidos.buscar(id).orElseThrow(() -> new IllegalArgumentException("No existe el pedido #" + id));
        if (!pedido.obtenerEstado().permiteModificacion()) {
            throw new IllegalStateException("El pedido #" + id + " ya no puede modificarse (estado: " + pedido.obtenerEstado().obtenerEtiqueta() + ")");
        }
        new DialogoEditarPedido(this, catalogo, promociones, pedidos, pedido, this::personalizarProducto, this::actualizarPedidos).setVisible(true);
    }

    private void agregarProducto() {
        int fila = tablaMenu.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Selecciona un producto del menú");
        Producto producto = personalizarProducto(menu.get(tablaMenu.convertRowIndexToModel(fila)).producto());
        if (producto == null) return;
        carrito.add(new DetallePedido(producto, (Integer) cantidad.getValue()));
        actualizarCarrito();
    }

    private Producto personalizarProducto(Producto producto) {
        List<OpcionPersonalizacion> opciones = CatalogoComplementos.disponiblesPara(producto);
        if (opciones.isEmpty()) return producto;
        JPanel panel = new JPanel(new GridLayout(0, 1));
        List<JCheckBox> comprobaciones = new ArrayList<>();
        for (OpcionPersonalizacion opcion : opciones) {
            JCheckBox comprobar = new JCheckBox(opcion.etiqueta() + " (+" + Dinero.formatear(opcion.precio()) + ")");
            comprobaciones.add(comprobar); panel.add(comprobar);
        }
        if (JOptionPane.showConfirmDialog(this, panel, "Personalizar " + producto.obtenerNombre(), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return null;
        for (int i = 0; i < comprobaciones.size(); i++) if (comprobaciones.get(i).isSelected()) producto = opciones.get(i).aplicarA(producto);
        return producto;
    }

    private void eliminarDetalle() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Selecciona una línea del pedido");
        carrito.remove(fila); actualizarCarrito();
    }

    private ConstructorPedido constructor() {
        ConstructorPedido constructor = new ConstructorPedido().cliente(cliente.getText());
        for (DetallePedido linea : carrito) constructor.agregar(linea.producto(), linea.cantidad());
        if (promo.getSelectedIndex() == 0) constructor.promocionAutomatica(promociones);
        else if (promo.getSelectedIndex() > 1) constructor.promocion(listaPromos.get(promo.getSelectedIndex() - 2));
        return constructor;
    }

    private void actualizarCarrito() {
        modeloCarrito.setRowCount(0);
        for (DetallePedido linea : carrito) modeloCarrito.addRow(new Object[]{linea.producto().obtenerNombre(), linea.producto().obtenerDescripcion(), linea.cantidad(), Dinero.formatear(linea.total())});
        if (carrito.isEmpty()) { total.setText("Total: $0"); indicacion.setText(" "); return; }
        Pedido borrador = constructor().previsualizar();
        total.setText("Total: " + Dinero.formatear(borrador.total()));
        indicacion.setText(promo.getSelectedIndex() > 1 && borrador.descuento().signum() == 0 ? "La promoción no aplica" : borrador.obtenerPromocion().map(p -> p.obtenerCodigo() + ": -" + Dinero.formatear(borrador.descuento())).orElse("Sin descuento"));
    }

    private void mostrarRecibo(Pedido pedido) {
        JTextArea texto = new JTextArea(pedido.recibo(), 16, 42);
        texto.setEditable(false); texto.setFont(recibo.getFont());
        JOptionPane.showMessageDialog(this, new JScrollPane(texto), "Recibo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmarPedido() {
        ConstructorPedido constructor = constructor();
        Pedido borrador = constructor.previsualizar();
        if (promo.getSelectedIndex() > 1 && borrador.descuento().signum() == 0) constructor.promocion(null);
        if (JOptionPane.showConfirmDialog(this, "Confirmar pedido por " + Dinero.formatear(borrador.total()) + "?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Pedido pedido = pedidos.registrarPedido(constructor);
        carrito.clear(); cliente.setText(""); promo.setSelectedIndex(0); actualizarCarrito();
        mostrarRecibo(pedido);
    }

    private long pedidoSeleccionado() {
        int fila = tablaPedidos.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Selecciona un pedido registrado");
        return (Long) modeloPedido.getValueAt(fila, 0);
    }

    private void actualizarPedidos() {
        int seleccionado = tablaPedidos.getSelectedRow();
        Long id = seleccionado < 0 ? null : (Long) modeloPedido.getValueAt(seleccionado, 0);
        modeloPedido.setRowCount(0);
        for (Pedido pedido : historial.isSelected() ? pedidos.pedidosArchivados() : pedidos.pedidosActivos()) {
            int indice = modeloPedido.getRowCount();
            modeloPedido.addRow(new Object[]{pedido.obtenerId(), pedido.obtenerNombreCliente(), pedido.obtenerEstado().obtenerEtiqueta(), Dinero.formatear(pedido.total())});
            if (id != null && id.equals(pedido.obtenerId())) tablaPedidos.setRowSelectionInterval(indice, indice);
        }
    }

    @Override public void alCambiarEstado(Pedido pedido, EstadoPedido previo, EstadoPedido actual) {
        Runnable actualizar = () -> {
            notificaciones.append("Pedido #" + pedido.obtenerId() + ": " + (previo == null ? "" : previo.obtenerEtiqueta() + " → ") + actual.obtenerEtiqueta() + "\n");
            notificaciones.setCaretPosition(notificaciones.getDocument().getLength());
            actualizarPedidos();
            if (panelVentas != null) panelVentas.actualizarVista();
        };
        if (SwingUtilities.isEventDispatchThread()) actualizar.run(); else SwingUtilities.invokeLater(actualizar);
    }

    @Override public void alModificarPedido(Pedido pedido) {
        Runnable actualizar = () -> {
            notificaciones.append("Pedido #" + pedido.obtenerId() + ": contenido modificado\n");
            notificaciones.setCaretPosition(notificaciones.getDocument().getLength());
            actualizarPedidos();
        };
        if (SwingUtilities.isEventDispatchThread()) actualizar.run(); else SwingUtilities.invokeLater(actualizar);
    }
}
