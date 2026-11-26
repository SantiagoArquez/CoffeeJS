package com.cafeteria.interfaz;

import com.cafeteria.catalogo.*;
import com.cafeteria.pedido.*;
import com.cafeteria.promocion.*;
import com.cafeteria.ventas.*;
import com.cafeteria.utilidad.Dinero;
import javax.swing.*;
import java.awt.*;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class VerificacionInterfaz {
    private static int comprobaciones;

    private static <T extends Component> List<T> componentes(Component raiz, Class<T> tipo) {
        List<T> resultado = new ArrayList<>();
        if (tipo.isInstance(raiz)) resultado.add(tipo.cast(raiz));
        if (raiz instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) resultado.addAll(componentes(hijo, tipo));
        }
        return resultado;
    }

    private static void pulsar(Component raiz, String texto) {
        componentes(raiz, AbstractButton.class).stream().filter(b -> texto.equals(b.getText()))
                .findFirst().orElseThrow(() -> new AssertionError("Botón no encontrado: " + texto)).doClick();
    }

    private static void comprobar(boolean valor, String nombre) {
        if (!valor) throw new AssertionError(nombre);
        comprobaciones++;
        System.out.println("OK: " + nombre);
    }

    private record Respuesta(String titulo, Consumer<JDialog> accion) {}

    private static Respuesta opcion(String titulo, int valor) {
        return new Respuesta(titulo, dialogo -> componentes(dialogo, JOptionPane.class).getFirst().setValue(valor));
    }

    private static void dialogos(Runnable accion, Respuesta... respuestas) {
        int[] siguiente = {0};
        Throwable[] error = {null};
        Timer temporizador = new Timer(50, null);
        temporizador.addActionListener(evento -> {
            List<Window> ventanas = new ArrayList<>(List.of(Window.getWindows()));
            java.util.Collections.reverse(ventanas);
            for (Window ventana : ventanas) {
                if (!(ventana instanceof JDialog dialogo) || !dialogo.isShowing()) continue;
                try {
                    if (siguiente[0] >= respuestas.length) throw new AssertionError("Diálogo inesperado: " + dialogo.getTitle());
                    Respuesta respuestaDialogo = respuestas[siguiente[0]];
                    if (!dialogo.getTitle().startsWith(respuestaDialogo.titulo())) throw new AssertionError("Esperado " + respuestaDialogo.titulo() + ", recibido " + dialogo.getTitle());
                    siguiente[0]++;
                    temporizador.stop();
                    try { respuestaDialogo.accion().accept(dialogo); } finally { temporizador.start(); }
                } catch (Throwable t) {
                    error[0] = t;
                    dialogo.dispose();
                }
                break;
            }
        });
        temporizador.start();
        try { accion.run(); } finally { temporizador.stop(); }
        if (error[0] != null) throw new AssertionError(error[0]);
        if (siguiente[0] != respuestas.length) throw new AssertionError("Faltaron diálogos");
    }

    private static void rellenar(JDialog dialogo, String... valores) {
        List<JTextField> campos = componentes(dialogo, JTextField.class);
        for (int i = 0; i < valores.length; i++) campos.get(i).setText(valores[i]);
        componentes(dialogo, JOptionPane.class).getFirst().setValue(JOptionPane.OK_OPTION);
    }

    private static void seleccionar(JTable tabla, String codigo) {
        for (int i = 0; i < tabla.getRowCount(); i++) {
            if (codigo.equals(String.valueOf(tabla.getValueAt(i, 0)))) {
                tabla.setRowSelectionInterval(i, i);
                return;
            }
        }
        throw new AssertionError("Fila no encontrada: " + codigo);
    }

    private static void ejecutarVerificacion() {
        CatalogoProductos catalogo = MenuPredeterminado.cargarPredeterminados();
        MotorPromociones promociones = MotorPromociones.conPredeterminados();
        ServicioPedidos pedidos = new ServicioPedidos(new ServicioVentas(new RepositorioVentasMemoria(), Clock.systemDefaultZone()));
        AplicacionGrafica aplicacion = new AplicacionGrafica(catalogo, promociones, pedidos);
        aplicacion.setVisible(true);
        try {
            JTabbedPane pestanas = componentes(aplicacion, JTabbedPane.class).getFirst();
            comprobar(pestanas.getTabCount() == 6, "Inicio y seis pestañas");
            pestanas.setSelectedIndex(3);
            Component administracion = pestanas.getSelectedComponent();
            JTable tablaAdministracion = componentes(administracion, JTable.class).getFirst();
            dialogos(() -> pulsar(administracion, "Registrar"), new Respuesta("Registrar producto", d -> rellenar(d, "QA-01", "Café de prueba", "Descripción", "9000")));
            comprobar(catalogo.buscarPorCodigo("QA-01").orElseThrow().obtenerPrecio().equals(Dinero.de(9000)), "Registrar producto");
            seleccionar(tablaAdministracion, "QA-01");
            dialogos(() -> pulsar(administracion, "Modificar"), new Respuesta("Modificar producto", d -> rellenar(d, "QA-01", "Café editado", "Descripción nueva", "10000")));
            comprobar(catalogo.buscarPorCodigo("QA-01").orElseThrow().obtenerPrecio().equals(Dinero.de(10000)), "Modificar producto");
            seleccionar(tablaAdministracion, "QA-01");
            dialogos(() -> pulsar(administracion, "Eliminar"), opcion("Confirmar", JOptionPane.YES_OPTION));
            comprobar(catalogo.buscarPorCodigo("QA-01").isEmpty(), "Eliminar producto");
            dialogos(() -> pulsar(administracion, "Crear combo"), new Respuesta("Crear combo", d -> {
                List<JTextField> campos = componentes(d, JTextField.class);
                campos.get(0).setText("QA-COM"); campos.get(1).setText("Combo prueba");
                campos.get(2).setText("Descripción"); campos.get(3).setText("10");
                JComboBox<?> opciones = componentes(d, JComboBox.class).getFirst();
                opciones.setSelectedIndex(0);
                dialogos(() -> pulsar(d, "Agregar componente"), opcion("Personalizar", JOptionPane.OK_OPTION));
                opciones.setSelectedIndex(1);
                dialogos(() -> pulsar(d, "Agregar componente"), opcion("Personalizar", JOptionPane.OK_OPTION));
                pulsar(d, "Guardar combo");
            }));
            comprobar(catalogo.buscarPorCodigo("QA-COM").orElseThrow().obtenerPrecio().equals(Dinero.de(8550)), "Crear combo desde Swing");
            seleccionar(tablaAdministracion, "QA-COM");
            dialogos(() -> pulsar(administracion, "Modificar"), new Respuesta("Modificar combo", d -> {
                componentes(d, JList.class).getFirst().setSelectedIndex(0);
                pulsar(d, "Quitar componente");
                componentes(d, JComboBox.class).getFirst().setSelectedIndex(2);
                dialogos(() -> pulsar(d, "Agregar componente"), opcion("Personalizar", JOptionPane.OK_OPTION));
                pulsar(d, "Guardar combo");
            }));
            comprobar(catalogo.buscarPorCodigo("QA-COM").orElseThrow().obtenerPrecio().equals(Dinero.de(11250)), "Modificar componentes del combo");
            pestanas.setSelectedIndex(2);
            Component promos = pestanas.getSelectedComponent();
            dialogos(() -> pulsar(promos, "Crear promoción"), new Respuesta("Crear promoción", d -> rellenar(d, "QA10", "Prueba", "10", "5000", "0", "2")));
            comprobar(promociones.buscarPorCodigo("QA10").isPresent(), "Crear promoción");
            pestanas.setSelectedIndex(0);
            Component entrada = pestanas.getSelectedComponent();
            JTable menu = componentes(entrada, JTable.class).get(0);
            JTable carrito = componentes(entrada, JTable.class).get(1);
            dialogos(() -> pulsar(entrada, "Confirmar pedido"), opcion("CoffeeJS", JOptionPane.OK_OPTION));
            comprobar(pedidos.todos().isEmpty(), "Rechazar pedido vacío");
            seleccionar(menu, "COM-01");
            pulsar(entrada, "Agregar producto");
            comprobar(carrito.getRowCount() == 1, "Agregar combo existente");
            carrito.setRowSelectionInterval(0, 0);
            pulsar(entrada, "Quitar línea");
            comprobar(carrito.getRowCount() == 0, "Quitar producto del pedido");
            seleccionar(menu, "COM-01");
            pulsar(entrada, "Agregar producto");
            pulsar(entrada, "Vaciar");
            comprobar(carrito.getRowCount() == 0, "Vaciar pedido");
            componentes(entrada, JTextField.class).getFirst().setText("Cliente prueba");
            seleccionar(menu, "BEB-04");
            dialogos(() -> pulsar(entrada, "Agregar producto"), new Respuesta("Personalizar", d -> {
                componentes(d, JCheckBox.class).stream().filter(b -> b.getText().contains("Shot extra")).findFirst().orElseThrow().setSelected(true);
                componentes(d, JOptionPane.class).getFirst().setValue(JOptionPane.OK_OPTION);
            }));
            comprobar(carrito.getRowCount() == 1 && carrito.getValueAt(0,1).toString().contains("Shot extra"), "Agregar complemento y actualizar descripción");
            JComboBox<?> promo = componentes(entrada, JComboBox.class).getFirst();
            comprobar("Automática".equals(promo.getSelectedItem()), "Promoción automática predeterminada");
            promo.setSelectedItem("QA10");
            dialogos(() -> pulsar(entrada, "Ver recibo"), opcion("Recibo", JOptionPane.OK_OPTION));
            comprobar(carrito.getRowCount() == 1, "Vista previa de recibo");
            dialogos(() -> pulsar(entrada, "Confirmar pedido"), opcion("Confirmar", JOptionPane.YES_OPTION), opcion("Recibo", JOptionPane.OK_OPTION));
            Pedido pedido = pedidos.todos().iterator().next();
            comprobar(pedido.total().equals(Dinero.de(9450)) && carrito.getRowCount() == 0, "Confirmar pedido con descuento");
            pestanas.setSelectedIndex(1);
            Component registrado = pestanas.getSelectedComponent();
            JTable tablaPedidos = componentes(registrado, JTable.class).getFirst();
            seleccionar(tablaPedidos, String.valueOf(pedido.obtenerId()));
            dialogos(() -> pulsar(registrado, "Modificar pedido"), new Respuesta("Modificar pedido", d -> {
                componentes(d, JTextField.class).getFirst().setText("Cliente editado");
                pulsar(d, "Guardar cambios");
            }));
            comprobar(pedido.obtenerNombreCliente().equals("Cliente editado"), "Modificar pedido");
            for (int i = 0; i < 3; i++) {
                seleccionar(tablaPedidos, String.valueOf(pedido.obtenerId()));
                pulsar(registrado, "Avanzar estado");
            }
            comprobar(pedido.obtenerEstado() == EstadoPedido.ENTREGADO, "Avanzar estados hasta entregado");
            comprobar(pedidos.ventas().ventasAbiertas().size() == 1, "Registrar venta al entregar");
            comprobar(!pedidos.cocina().bandeja().isEmpty() && !pedidos.cajero().bandeja().isEmpty() && !pedidos.mesero().bandeja().isEmpty(), "Notificar a las tres áreas");
            pestanas.setSelectedIndex(4);
            pulsar(pestanas.getSelectedComponent(), "Vaciar");
            comprobar(pedidos.cocina().bandeja().isEmpty(), "Vaciar notificaciones de cocina");
            pestanas.setSelectedIndex(5);
            Component ventas = pestanas.getSelectedComponent();
            dialogos(() -> pulsar(ventas, "Reporte de hoy"), opcion("Reporte de hoy", JOptionPane.OK_OPTION));
            dialogos(() -> pulsar(ventas, "Reporte por fechas"), opcion("Reporte por fechas", JOptionPane.OK_OPTION), opcion("Reporte de ventas", JOptionPane.OK_OPTION));
            comprobar(pedidos.ventas().ventasAbiertas().size() == 1, "Consultar reportes");
            dialogos(() -> pulsar(ventas, "Cierre de caja"), new Respuesta("Cierre de caja", d -> {
                JOptionPane panelContenedor = componentes(d, JOptionPane.class).getFirst();
                panelContenedor.setInputValue("Cajero prueba");
                panelContenedor.setValue(JOptionPane.OK_OPTION);
            }), opcion("Cierre de caja", JOptionPane.YES_OPTION), opcion("Cierre de caja", JOptionPane.OK_OPTION));
            comprobar(pedidos.ventas().ventasAbiertas().isEmpty() && pedidos.ventas().cierres().size() == 1, "Cierre de caja");
            dialogos(() -> pulsar(ventas, "Historial de cierres"), opcion("Historial de cierres", JOptionPane.OK_OPTION));
            pestanas.setSelectedIndex(1);
            comprobar(tablaPedidos.getRowCount() == 0, "Ocultar pedido cerrado");
            pulsar(registrado, "Mostrar historial");
            comprobar(tablaPedidos.getRowCount() == 1, "Consultar pedido archivado");
            pestanas.setSelectedIndex(0);
            seleccionar(menu, "BEB-04");
            for (int i = 0; i < 3; i++) {
                dialogos(() -> pulsar(entrada, "Agregar producto"), opcion("Personalizar", JOptionPane.OK_OPTION));
            }
            comprobar(carrito.getRowCount() == 3 && componentes(entrada, JLabel.class).stream().anyMatch(l -> l.getText().contains("CAFE3X2")), "3x2 automático en tres líneas");
            dialogos(() -> pulsar(entrada, "Confirmar pedido"), opcion("Confirmar", JOptionPane.YES_OPTION), opcion("Recibo", JOptionPane.OK_OPTION));
            Pedido cancelado = pedidos.todos().stream().filter(o -> o.obtenerId() != pedido.obtenerId()).findFirst().orElseThrow();
            comprobar(cancelado.total().equals(Dinero.de(16000)) && cancelado.obtenerPromocion().orElseThrow().obtenerCodigo().equals("CAFE3X2"), "Guardar promoción automática");
            pestanas.setSelectedIndex(1);
            pulsar(registrado, "Mostrar historial");
            seleccionar(tablaPedidos, String.valueOf(cancelado.obtenerId()));
            dialogos(() -> pulsar(registrado, "Cancelar pedido"), opcion("Confirmar", JOptionPane.YES_OPTION));
            comprobar(cancelado.obtenerEstado() == EstadoPedido.CANCELADO && tablaPedidos.getRowCount() == 0, "Cancelar pedido y ocultarlo del listado activo");
            pulsar(registrado, "Mostrar historial");
            comprobar(tablaPedidos.getRowCount() == 2 && pedidos.ventas().todos().size() == 1, "Conservar cancelación sin generar venta");
            seleccionar(tablaPedidos, String.valueOf(cancelado.obtenerId()));
            dialogos(() -> pulsar(registrado, "Avanzar estado"), opcion("CoffeeJS", JOptionPane.OK_OPTION));
            comprobar(cancelado.obtenerEstado() == EstadoPedido.CANCELADO, "Impedir cambios de estado finales");
            pestanas.setSelectedIndex(2);
            seleccionar(componentes(promos, JTable.class).getFirst(), "QA10");
            dialogos(() -> pulsar(promos, "Eliminar"), opcion("Confirmar", JOptionPane.YES_OPTION));
            comprobar(promociones.buscarPorCodigo("QA10").isEmpty(), "Eliminar promoción");
        } finally {
            aplicacion.dispose();
        }
    }

    public static void main(String[] argumentos) throws Exception {
        comprobaciones = 0;
        SwingUtilities.invokeAndWait(VerificacionInterfaz::ejecutarVerificacion);
        System.out.println("Comprobaciones Swing: " + comprobaciones);
    }
}
