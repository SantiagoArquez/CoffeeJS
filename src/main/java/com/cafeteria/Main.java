package com.cafeteria;

import com.cafeteria.catalog.DefaultMenu;
import com.cafeteria.catalog.ProductCatalog;
import com.cafeteria.order.OrderService;
import com.cafeteria.promotion.PromotionEngine;
import com.cafeteria.ui.ConsoleApp;
import com.cafeteria.ui.ConsoleNotifier;
import com.cafeteria.ui.Demo;
import com.cafeteria.ui.SwingApp;
import javax.swing.SwingUtilities;

import java.util.Arrays;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ProductCatalog catalog = DefaultMenu.load();
        PromotionEngine promotions = PromotionEngine.withDefaults();
        OrderService orders = new OrderService();
        orders.addListener(new ConsoleNotifier());

        if (Arrays.asList(args).contains("--demo")) {
            Demo.run(catalog, promotions, orders);
        } else if (Arrays.asList(args).contains("--console")) {
            new ConsoleApp(catalog, promotions, orders).run();
        } else {
            SwingUtilities.invokeLater(() -> new SwingApp(catalog, promotions, orders).setVisible(true));
        }
    }
}
