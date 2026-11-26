package com.cafeteria.interfaz;

import com.cafeteria.catalogo.CatalogoProductos;
import com.cafeteria.catalogo.FabricaProductos;
import com.cafeteria.modelo.Combo;
import com.cafeteria.modelo.Producto;
import com.cafeteria.modelo.CategoriaProducto;
import com.cafeteria.utilidad.Dinero;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

final class DialogoCombo extends JDialog {
    private final JTextField codigo = new JTextField(16);
    private final JTextField nombre = new JTextField(16);
    private final JTextField descripcion = new JTextField(16);
    private final JTextField descuento = new JTextField("0", 16);
    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modeloLista);
    private final List<Producto> articulos = new ArrayList<>();
    private final JLabel precio = new JLabel(" ");
    private Combo resultado;

    DialogoCombo(JFrame propietario, CatalogoProductos catalogo, String codigoExistente, Combo previo, UnaryOperator<Producto> personalizar) {
        super(propietario, previo == null ? "Crear combo" : "Modificar combo", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        codigo.setText(codigoExistente);
        codigo.setEditable(previo == null);
        if (previo != null) {
            nombre.setText(previo.obtenerNombre());
            descripcion.setText(previo.obtenerDescripcionBase());
            descuento.setText(String.valueOf(previo.obtenerPorcentajeDescuento()));
            articulos.addAll(previo.obtenerArticulos());
        }
        List<CatalogoProductos.Entrada> opciones = catalogo.entradas().stream().filter(e -> e.producto().obtenerCategoria() != CategoriaProducto.COMBO).toList();
        JComboBox<String> disponibles = new JComboBox<>(opciones.stream().map(e -> e.codigo() + " - " + e.producto().obtenerNombre() + " - " + Dinero.formatear(e.producto().obtenerPrecio())).toArray(String[]::new));
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Código:")); formulario.add(codigo);
        formulario.add(new JLabel("Nombre:")); formulario.add(nombre);
        formulario.add(new JLabel("Descripción:")); formulario.add(descripcion);
        formulario.add(new JLabel("Descuento (%):")); formulario.add(descuento);
        JPanel raiz = new JPanel(new BorderLayout(8, 8));
        raiz.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        raiz.add(formulario, BorderLayout.NORTH);
        JPanel centro = new JPanel(new BorderLayout(6, 6));
        JPanel agregar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        agregar.add(disponibles);
        agregar.add(boton("Agregar componente", () -> {
            if (disponibles.getSelectedIndex() < 0) throw new IllegalArgumentException("Selecciona un producto");
            Producto producto = personalizar.apply(opciones.get(disponibles.getSelectedIndex()).producto());
            if (producto != null) { articulos.add(producto); actualizarVista(); }
        }));
        centro.add(agregar, BorderLayout.NORTH);
        centro.add(new JScrollPane(lista));
        centro.add(boton("Quitar componente", () -> {
            if (lista.getSelectedIndex() < 0) throw new IllegalArgumentException("Selecciona un componente");
            articulos.remove(lista.getSelectedIndex()); actualizarVista();
        }), BorderLayout.SOUTH);
        raiz.add(centro);
        JPanel inferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        inferior.add(precio);
        inferior.add(boton("Calcular precio", this::actualizarVista));
        inferior.add(boton("Guardar combo", () -> {
            if (codigo.getText().isBlank()) throw new IllegalArgumentException("El código es obligatorio");
            if (previo == null && catalogo.buscarPorCodigo(codigo.getText()).isPresent()) throw new IllegalArgumentException("El código ya existe");
            resultado = FabricaProductos.comboPersonalizado(nombre.getText(), descripcion.getText(), descuento.getText(), articulos);
            dispose();
        }));
        inferior.add(boton("Cancelar", this::dispose));
        raiz.add(inferior, BorderLayout.SOUTH);
        setContentPane(raiz);
        setSize(760, 500);
        setLocationRelativeTo(propietario);
        actualizarVista();
    }

    private JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(e -> {
            try { accion.run(); }
            catch (IllegalArgumentException | IllegalStateException excepcion) {
                JOptionPane.showMessageDialog(this, excepcion.getMessage(), "CoffeeJS", JOptionPane.WARNING_MESSAGE);
            }
        });
        return boton;
    }

    private void actualizarVista() {
        modeloLista.clear();
        articulos.forEach(p -> modeloLista.addElement(p.obtenerNombre() + " - " + Dinero.formatear(p.obtenerPrecio())));
        try {
            precio.setText("Total: " + Dinero.formatear(FabricaProductos.comboPersonalizado("Vista previa", "Vista previa", descuento.getText(), articulos).obtenerPrecio()));
        } catch (IllegalArgumentException e) { precio.setText(e.getMessage()); }
    }

    Combo resultado() { return resultado; }
    String codigo() { return codigo.getText(); }
}
