package com.cafeteria.interfaz;

import com.cafeteria.notificacion.Notificacion;
import com.cafeteria.notificacion.NotificadorArea;
import com.cafeteria.pedido.ServicioPedidos;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

final class PanelNotificaciones extends JPanel {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    PanelNotificaciones(ServicioPedidos pedidos) {
        super(new GridLayout(1, 3, 10, 10));
        add(bandeja(pedidos.cocina()));
        add(bandeja(pedidos.cajero()));
        add(bandeja(pedidos.mesero()));
    }

    private JPanel bandeja(NotificadorArea notificador) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        notificador.bandeja().forEach(n -> area.append(linea(n)));
        notificador.agregarReceptor(n -> SwingUtilities.invokeLater(() -> {
            area.append(linea(n));
            area.setCaretPosition(area.getDocument().getLength());
        }));
        JButton limpiar = new JButton("Vaciar");
        limpiar.addActionListener(e -> {
            notificador.limpiar();
            area.setText("");
        });
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder(notificador.destinatario()));
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(limpiar);
        panel.add(sur, BorderLayout.SOUTH);
        return panel;
    }

    private static String linea(Notificacion n) {
        return n.fechaCreacion().format(HORA) + "  Pedido #" + n.idPedido() + ": " + n.mensaje() + "\n";
    }
}
