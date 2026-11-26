package com.cafeteria.interfaz;

import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.promocion.ConfiguracionPromocion;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Set;

final class DialogoPromocion {

    private static final String[] TIPOS = {"Porcentaje sobre todo el pedido", "Valor fijo con compra mínima",
            "Lleva N+1 y paga N (categoría)", "Porcentaje en una categoría"};
    private static final String[] NOMBRES_DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

    private DialogoPromocion() {
    }

    static ConfiguracionPromocion preguntar(Component padre) {
        JComboBox<String> tipo = new JComboBox<>(TIPOS);
        JTextField codigo = new JTextField(16);
        JTextField descripcion = new JTextField(16);
        JTextField porcentaje = new JTextField("10", 16);
        JTextField monto = new JTextField("5000", 16);
        JTextField minimoRequerido = new JTextField("0", 16);
        JTextField compra = new JTextField("2", 16);
        JComboBox<String> selectorCategoria = new JComboBox<>(new String[]{"Bebida", "Alimento", "Combo"});
        JCheckBox limitado = new JCheckBox("Limitar a ciertos días u horas");
        JCheckBox[] dias = new JCheckBox[7];
        JPanel panelDias = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        for (int i = 0; i < 7; i++) {
            dias[i] = new JCheckBox(NOMBRES_DIAS[i]);
            panelDias.add(dias[i]);
        }
        JTextField desde = new JTextField("", 6);
        JTextField hasta = new JTextField("", 6);
        JPanel horas = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        horas.add(new JLabel("de ")); horas.add(desde); horas.add(new JLabel(" a ")); horas.add(hasta);
        horas.add(new JLabel("  (HH:mm, vacío = todo el día)"));

        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Tipo:")); formulario.add(tipo);
        formulario.add(new JLabel("Código (sin espacios):")); formulario.add(codigo);
        formulario.add(new JLabel("Descripción:")); formulario.add(descripcion);
        formulario.add(new JLabel("Porcentaje (1-100):")); formulario.add(porcentaje);
        formulario.add(new JLabel("Valor a descontar ($):")); formulario.add(monto);
        formulario.add(new JLabel("Subtotal mínimo ($):")); formulario.add(minimoRequerido);
        formulario.add(new JLabel("Unidades que se pagan:")); formulario.add(compra);
        formulario.add(new JLabel("Categoría:")); formulario.add(selectorCategoria);
        formulario.add(limitado); formulario.add(new JLabel(" "));
        formulario.add(new JLabel("Días (ninguno = todos):")); formulario.add(panelDias);
        formulario.add(new JLabel("Horario:")); formulario.add(horas);

        Runnable sincronizar = () -> {
            int t = tipo.getSelectedIndex();
            porcentaje.setEnabled(t == 0 || t == 3);
            monto.setEnabled(t == 1);
            minimoRequerido.setEnabled(t == 0 || t == 1);
            compra.setEnabled(t == 2);
            selectorCategoria.setEnabled(t == 2 || t == 3);
            for (JCheckBox d : dias) d.setEnabled(limitado.isSelected());
            desde.setEnabled(limitado.isSelected());
            hasta.setEnabled(limitado.isSelected());
        };
        tipo.addActionListener(e -> sincronizar.run());
        limitado.addActionListener(e -> sincronizar.run());
        sincronizar.run();

        while (JOptionPane.showConfirmDialog(padre, formulario, "Crear promoción", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                CategoriaProducto categoria = CategoriaProducto.values()[selectorCategoria.getSelectedIndex()];
                ConfiguracionPromocion configuracion = switch (tipo.getSelectedIndex()) {
                    case 0 -> ConfiguracionPromocion.porcentaje(codigo.getText(), descripcion.getText(), entero(porcentaje, "porcentaje"), numero(minimoRequerido, "subtotal mínimo"));
                    case 1 -> ConfiguracionPromocion.montoFijo(codigo.getText(), descripcion.getText(), numero(monto, "valor a descontar"), numero(minimoRequerido, "subtotal mínimo"));
                    case 2 -> ConfiguracionPromocion.compraNRecibeUno(codigo.getText(), descripcion.getText(), categoria, entero(compra, "unidades"));
                    default -> ConfiguracionPromocion.porcentajeCategoria(codigo.getText(), descripcion.getText(), entero(porcentaje, "porcentaje"), categoria);
                };
                if (limitado.isSelected()) {
                    Set<DayOfWeek> seleccionado = EnumSet.noneOf(DayOfWeek.class);
                    for (int i = 0; i < 7; i++) if (dias[i].isSelected()) seleccionado.add(DayOfWeek.of(i + 1));
                    boolean todoElDia = desde.getText().isBlank() && hasta.getText().isBlank();
                    configuracion = configuracion.conHorario(seleccionado, todoElDia ? null : hora(desde), todoElDia ? null : hora(hasta));
                }
                return configuracion;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(padre, e.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            }
        }
        return null;
    }

    private static int entero(JTextField campo, String nombre) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresa un número entero válido en " + nombre);
        }
    }

    private static long numero(JTextField campo, String nombre) {
        try {
            return Long.parseLong(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresa un valor entero en pesos, sin separadores, en " + nombre);
        }
    }

    private static LocalTime hora(JTextField campo) {
        try {
            return LocalTime.parse(campo.getText().trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Hora inválida, usa el formato HH:mm (por ejemplo 15:30)");
        }
    }
}
