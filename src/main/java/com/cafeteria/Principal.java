package com.cafeteria;

import com.cafeteria.catalogo.MenuPredeterminado;
import com.cafeteria.catalogo.CatalogoProductos;
import com.cafeteria.pedido.ServicioPedidos;
import com.cafeteria.promocion.MotorPromociones;
import com.cafeteria.interfaz.AplicacionConsola;
import com.cafeteria.interfaz.NotificadorConsola;
import com.cafeteria.interfaz.Demostracion;
import com.cafeteria.interfaz.AplicacionGrafica;
import javax.swing.SwingUtilities;

import java.util.Arrays;

public final class Principal {

    private Principal() {
    }

    public static void main(String[] argumentos) {
        try {
            iniciar(argumentos);
        } catch (RuntimeException e) {
            String mensaje = "No se pudo iniciar CoffeeJS: " + e.getMessage();
            System.err.println(mensaje);
            if (!java.awt.GraphicsEnvironment.isHeadless() && !Arrays.asList(argumentos).contains("--console") && !Arrays.asList(argumentos).contains("--demo")) {
                javax.swing.JOptionPane.showMessageDialog(null, mensaje, "CoffeeJS", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            System.exit(1);
        }
    }

    private static void iniciar(String[] argumentos) {
        CatalogoProductos catalogo = MenuPredeterminado.cargar();
        MotorPromociones promociones = MotorPromociones.crearPredeterminado();
        ServicioPedidos pedidos = new ServicioPedidos();
        pedidos.agregarObservador(new NotificadorConsola());

        if (Arrays.asList(argumentos).contains("--demo")) {
            Demostracion.ejecutar(catalogo, promociones, pedidos);
        } else if (Arrays.asList(argumentos).contains("--console")) {
            new AplicacionConsola(catalogo, promociones, pedidos).ejecutar();
        } else {
            SwingUtilities.invokeLater(() -> new AplicacionGrafica(catalogo, promociones, pedidos).setVisible(true));
        }
    }
}
