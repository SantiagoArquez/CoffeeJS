package com.cafeteria.interfaz;

import com.cafeteria.pedido.ServicioPedidos;
import com.cafeteria.ventas.CierreCaja;
import com.cafeteria.ventas.Venta;
import com.cafeteria.ventas.ServicioVentas;
import com.cafeteria.utilidad.Dinero;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

final class PanelVentas extends JPanel {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final ServicioVentas ventas;
    private final Component propietario;
    private final Runnable alCerrar;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Pedido", "Cliente", "Fecha", "Subtotal", "Descuento", "Total"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JLabel resumen = new JLabel(" ");

    PanelVentas(Component propietario, ServicioPedidos pedidos, Runnable alCerrar) {
        super(new BorderLayout(8, 8));
        this.propietario = propietario;
        this.alCerrar = alCerrar;
        this.ventas = pedidos.ventas();
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        resumen.setFont(resumen.getFont().deriveFont(Font.BOLD, 15f));
        JPanel superior = new JPanel(new BorderLayout());
        superior.add(new JLabel("Ventas sin cerrar (pedidos entregados desde el último cierre)"), BorderLayout.NORTH);
        superior.add(resumen, BorderLayout.SOUTH);
        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botones.add(boton("Cierre de caja", this::cerrarCaja));
        botones.add(boton("Reporte de hoy", () -> mostrarTexto("Reporte de hoy", ventas.informeDiario(LocalDate.now()))));
        botones.add(boton("Reporte por fechas", this::informeRango));
        botones.add(boton("Historial de cierres", this::mostrarCierres));
        botones.add(boton("Actualizar", this::actualizarVista));
        add(botones, BorderLayout.SOUTH);
        actualizarVista();
    }

    void actualizarVista() {
        Runnable actualizar = () -> {
            modelo.setRowCount(0);
            BigDecimal total = Dinero.CERO;
            for (Venta s : ventas.ventasAbiertas()) {
                modelo.addRow(new Object[]{s.idPedido(), s.cliente(), s.fechaVenta().format(HORA),
                        Dinero.formatear(s.subtotal()), Dinero.formatear(s.descuento()), Dinero.formatear(s.total())});
                total = total.add(s.total());
            }
            resumen.setText("Ventas: " + modelo.getRowCount() + "   |   Total acumulado: " + Dinero.formatear(total));
        };
        if (SwingUtilities.isEventDispatchThread()) actualizar.run(); else SwingUtilities.invokeLater(actualizar);
    }

    private JButton boton(String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> {
            try {
                accion.run();
            } catch (IllegalArgumentException | IllegalStateException excepcion) {
                JOptionPane.showMessageDialog(propietario, excepcion.getMessage(), "CoffeeJS", JOptionPane.WARNING_MESSAGE);
            }
        });
        return b;
    }

    private void cerrarCaja() {
        if (ventas.ventasAbiertas().isEmpty()) {
            throw new IllegalStateException("No hay ventas abiertas para cerrar la caja");
        }
        String cajero = JOptionPane.showInputDialog(propietario, "Nombre del cajero:", "Cierre de caja", JOptionPane.QUESTION_MESSAGE);
        if (cajero == null) {
            return;
        }
        BigDecimal total = ventas.ventasAbiertas().stream().map(Venta::total).reduce(Dinero.CERO, BigDecimal::add);
        if (JOptionPane.showConfirmDialog(propietario, "¿Cerrar la caja con " + ventas.ventasAbiertas().size() + " venta(s) por "
                + Dinero.formatear(total) + "?", "Cierre de caja", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        CierreCaja cierre = ventas.cerrarCaja(cajero);
        actualizarVista();
        alCerrar.run();
        mostrarTexto("Cierre de caja", ventas.informeCierre(cierre));
    }

    private void informeRango() {
        JTextField desde = new JTextField(LocalDate.now().withDayOfMonth(1).format(FECHA), 10);
        JTextField hasta = new JTextField(LocalDate.now().format(FECHA), 10);
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Desde (dd/MM/yyyy):")); formulario.add(desde);
        formulario.add(new JLabel("Hasta (dd/MM/yyyy):")); formulario.add(hasta);
        if (JOptionPane.showConfirmDialog(propietario, formulario, "Reporte por fechas", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            mostrarTexto("Reporte de ventas", ventas.informeVentas(LocalDate.parse(desde.getText().trim(), FECHA),
                    LocalDate.parse(hasta.getText().trim(), FECHA)));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha inválida, usa el formato dd/MM/yyyy (por ejemplo 25/11/2026)");
        }
    }

    private void mostrarCierres() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"Cierre", "Fecha", "Cajero", "Ventas", "Total"}, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        DateTimeFormatter completo = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (CierreCaja c : ventas.cierres()) {
            m.addRow(new Object[]{c.id(), c.fechaCierre().format(completo), c.cajero(), c.cantidadVentas(), Dinero.formatear(c.total())});
        }
        JTable tabla = new JTable(m);
        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setPreferredSize(new Dimension(520, 240));
        JOptionPane.showMessageDialog(propietario, m.getRowCount() == 0 ? new JLabel("Aún no se han realizado cierres de caja.") : desplazamiento,
                "Historial de cierres", JOptionPane.PLAIN_MESSAGE);
    }

    private void mostrarTexto(String titulo, String texto) {
        JTextArea area = new JTextArea(texto, 20, 46);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setCaretPosition(0);
        JOptionPane.showMessageDialog(propietario, new JScrollPane(area), titulo, JOptionPane.PLAIN_MESSAGE);
    }
}
