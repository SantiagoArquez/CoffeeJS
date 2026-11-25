package com.cafeteria.ui;

import com.cafeteria.catalog.ProductCatalog;
import com.cafeteria.decorator.CustomizationCatalog;
import com.cafeteria.decorator.CustomizationOption;
import com.cafeteria.model.Product;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.order.Order;
import com.cafeteria.order.OrderBuilder;
import com.cafeteria.order.OrderService;
import com.cafeteria.promotion.Promotion;
import com.cafeteria.promotion.PromotionEngine;
import com.cafeteria.util.Money;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public final class ConsoleApp {

    private final ProductCatalog catalog;
    private final PromotionEngine promotions;
    private final OrderService orders;
    private final Scanner in = new Scanner(System.in);

    public ConsoleApp(ProductCatalog catalog, PromotionEngine promotions, OrderService orders) {
        this.catalog = catalog;
        this.promotions = promotions;
        this.orders = orders;
    }

    public void run() {
        System.out.println("Bienvenido al sistema de gestión de la cafetería");
        while (true) {
            System.out.println();
            System.out.println("1. Ver menú");
            System.out.println("2. Ver promociones");
            System.out.println("3. Nuevo pedido");
            System.out.println("4. Ver pedidos");
            System.out.println("5. Avanzar estado de un pedido");
            System.out.println("6. Cancelar un pedido");
            System.out.println("0. Salir");
            String option = prompt("Elige una opción: ");
            if (option == null) {
                return;
            }
            try {
                switch (option) {
                    case "1" -> showMenu();
                    case "2" -> showPromotions();
                    case "3" -> newOrder();
                    case "4" -> showOrders();
                    case "5" -> advanceOrder();
                    case "6" -> cancelOrder();
                    case "0" -> {
                        System.out.println("¡Hasta pronto!");
                        return;
                    }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("" + e.getMessage());
            }
        }
    }


    private void showMenu() {
        for (ProductCategory category : ProductCategory.values()) {
            System.out.println("\n--- " + category.getLabel() + "s ---");
            for (ProductCatalog.Entry e : catalog.entries()) {
                if (e.product().getCategory() == category) {
                    System.out.printf("%-7s %-24s %9s  %s%n", e.code(), e.product().getName(),
                            Money.format(e.product().getPrice()), e.product().getDescription());
                }
            }
        }
    }

    private void showPromotions() {
        System.out.println("\n--- Promociones disponibles ---");
        for (Promotion p : promotions.all()) {
            System.out.printf("%-14s %s%n", p.getCode(), p.getDescription());
        }
    }

    private void newOrder() {
        OrderBuilder builder = new OrderBuilder().customer(prompt("Nombre del cliente: "));
        while (true) {
            String code = prompt("Código de producto (ENTER para terminar): ");
            if (code == null || code.isBlank()) {
                break;
            }
            Optional<Product> found = catalog.findByCode(code);
            if (found.isEmpty()) {
                System.out.println("No existe el producto '" + code + "'. Revisa el menú.");
                continue;
            }
            Product product = customize(found.get());
            int quantity = askQuantity();
            builder.add(product, quantity);
            System.out.printf("Agregado: %dx %s (%s c/u)%n", quantity, product.getName(),
                    Money.format(product.getPrice()));
        }
        if (builder.isEmpty()) {
            System.out.println("Pedido vacío, no se creó.");
            return;
        }

        showPromotions();
        String promoCode = prompt("Código de promoción (ENTER para ninguna): ");
        if (promoCode != null && !promoCode.isBlank()) {
            Optional<Promotion> promo = promotions.findByCode(promoCode);
            if (promo.isEmpty()) {
                System.out.println("La promoción no existe, se continúa sin ella.");
            } else {
                builder.promotion(promo.get());
                if (builder.preview().discount().signum() == 0) {
                    System.out.println("La promoción no aplica a este pedido, se continúa sin ella.");
                    builder.promotion(null);
                }
            }
        } else {
            promotions.bestFor(builder.preview()).ifPresent(best ->
                    System.out.println("Sugerencia: con " + best.getCode() + " ahorrarías "
                            + Money.format(best.discountFor(builder.preview())) + "."));
        }

        System.out.println("\n" + builder.preview().receipt());
        String confirm = prompt("¿Confirmar pedido? (s/n): ");
        if (confirm != null && confirm.equalsIgnoreCase("s")) {
            Order order = orders.place(builder);
            System.out.println("Pedido #" + order.getId() + " creado.");
        } else {
            System.out.println("Pedido descartado.");
        }
    }

    private Product customize(Product product) {
        List<CustomizationOption> options = CustomizationCatalog.availableFor(product);
        if (options.isEmpty()) {
            return product;
        }
        System.out.println("Personalizar '" + product.getName() + "':");
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("  %d. %s (+%s)%n", i + 1, options.get(i).label(), Money.format(options.get(i).price()));
        }
        String answer = prompt("Extras (números separados por coma, ENTER para ninguno): ");
        if (answer == null || answer.isBlank()) {
            return product;
        }
        Product result = product;
        for (String token : answer.split(",")) {
            try {
                int index = Integer.parseInt(token.trim()) - 1;
                if (index >= 0 && index < options.size()) {
                    result = options.get(index).applyTo(result);
                } else {
                    System.out.println("Extra ignorado: " + token.trim());
                }
            } catch (NumberFormatException e) {
                System.out.println("Extra ignorado: " + token.trim());
            }
        }
        return result;
    }

    private int askQuantity() {
        String answer = prompt("Cantidad [1]: ");
        if (answer == null || answer.isBlank()) {
            return 1;
        }
        try {
            int q = Integer.parseInt(answer.trim());
            if (q > 0) {
                return q;
            }
        } catch (NumberFormatException ignored) {
        }
        System.out.println("Cantidad inválida, se usará 1.");
        return 1;
    }

    private void showOrders() {
        if (orders.all().isEmpty()) {
            System.out.println("Aún no hay pedidos.");
            return;
        }
        orders.all().forEach(o -> System.out.println("\n" + o.receipt()));
    }

    private void advanceOrder() {
        orders.advance(askOrderId());
    }

    private void cancelOrder() {
        orders.cancel(askOrderId());
    }

    private long askOrderId() {
        String answer = prompt("Número de pedido: ");
        try {
            return Long.parseLong(answer == null ? "" : answer.trim().replace("#", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número de pedido inválido");
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return in.hasNextLine() ? in.nextLine() : null;
    }
}
