package com.cafeteria.ui;

import com.cafeteria.catalog.ProductCatalog;
import com.cafeteria.catalog.ProductFactory;
import com.cafeteria.model.Combo;
import com.cafeteria.model.ProductCategory;
import com.cafeteria.decorator.CustomizationCatalog;
import com.cafeteria.decorator.CustomizationOption;
import com.cafeteria.model.Product;
import com.cafeteria.order.*;
import com.cafeteria.promotion.*;
import com.cafeteria.util.Money;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class SwingApp extends JFrame implements OrderListener {
    private final ProductCatalog catalog;
    private final PromotionEngine promotions;
    private final OrderService orders;
    private final List<OrderLine> cart = new ArrayList<>();
    private List<ProductCatalog.Entry> menu = List.of();
    private final List<Promotion> promoList;
    private final JTextField customer = new JTextField(18);
    private final JComboBox<String> promo = new JComboBox<>();
    private final JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final DefaultTableModel menuModel = model("Código", "Producto", "Categoría", "Precio", "Descripción");
    private final DefaultTableModel cartModel = model("Producto", "Descripción", "Cantidad", "Total");
    private final DefaultTableModel orderModel = model("Pedido", "Cliente", "Estado", "Total");
    private final JTable adminTable = new JTable(menuModel);
    private final JTable menuTable = new JTable(menuModel);
    private final JTable cartTable = new JTable(cartModel);
    private final JTable orderTable = new JTable(orderModel);
    private final JTextArea receipt = new JTextArea(12, 36);
    private final JTextArea notifications = new JTextArea(7, 40);
    private final JLabel total = new JLabel("Total: $0");
    private final JLabel hint = new JLabel(" ");

    public SwingApp(ProductCatalog catalog, PromotionEngine promotions, OrderService orders) {
        super("CoffeeJS - Gestión de cafetería");
        this.catalog = catalog;
        this.promotions = promotions;
        this.orders = orders;
        promoList = List.copyOf(promotions.all());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 600));
        setSize(1100, 740);
        setLocationRelativeTo(null);
        for (JTable table : List.of(menuTable, adminTable, cartTable, orderTable)) {
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowHeight(25);
        }
        receipt.setEditable(false);
        receipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        notifications.setEditable(false);
        refreshMenu();
        promo.addItem("Sin promoción");
        for (Promotion p : promoList) promo.addItem(p.getCode());
        promo.addActionListener(e -> refreshCart());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Nuevo pedido", newOrderPanel());
        tabs.addTab("Pedidos registrados", ordersPanel());
        tabs.addTab("Promociones", promotionsPanel());
        tabs.addTab("Gestionar productos", productsPanel());
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel title = new JLabel("CoffeeJS | Gestión de cafetería");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        root.add(title, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        JPanel notices = new JPanel(new BorderLayout());
        notices.setBorder(BorderFactory.createTitledBorder("Notificaciones internas"));
        notices.add(new JScrollPane(notifications));
        root.add(notices, BorderLayout.SOUTH);
        setContentPane(root);
        orders.addListener(this);
        refreshOrders();
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> {
            try { action.run(); }
            catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "CoffeeJS", JOptionPane.WARNING_MESSAGE);
            }
        });
        return b;
    }

    private JPanel newOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Cliente:")); top.add(customer);
        panel.add(top, BorderLayout.NORTH);
        JPanel left = new JPanel(new BorderLayout());
        left.setBorder(BorderFactory.createTitledBorder("Menú disponible"));
        left.add(new JScrollPane(menuTable));
        JPanel add = new JPanel(new FlowLayout(FlowLayout.LEFT));
        add.add(new JLabel("Cantidad:")); add.add(quantity);
        add.add(button("Agregar producto", this::addProduct));
        left.add(add, BorderLayout.SOUTH);
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("Pedido actual"));
        right.add(new JScrollPane(cartTable));
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        JPanel remove = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remove.add(button("Quitar línea", this::removeLine));
        remove.add(button("Vaciar", () -> { cart.clear(); refreshCart(); }));
        actions.add(remove);
        JPanel promos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        promos.add(new JLabel("Promoción:")); promos.add(promo);
        actions.add(promos); actions.add(hint);
        total.setFont(total.getFont().deriveFont(Font.BOLD, 17f));
        actions.add(total);
        JPanel confirm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        confirm.add(button("Ver recibo", () -> showReceipt(builder().preview())));
        confirm.add(button("Confirmar pedido", this::confirmOrder));
        actions.add(confirm); right.add(actions, BorderLayout.SOUTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(.55); panel.add(split);
        return panel;
    }

    private JPanel ordersPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(orderTable), new JScrollPane(receipt));
        split.setResizeWeight(.55); panel.add(split);
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = orderTable.getSelectedRow();
                receipt.setText(row < 0 ? "" : orders.find((Long) orderModel.getValueAt(row, 0)).map(Order::receipt).orElse(""));
                receipt.setCaretPosition(0);
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(button("Avanzar estado", () -> orders.advance(selectedOrder())));
        buttons.add(button("Cancelar pedido", () -> {
            long id = selectedOrder();
            if (JOptionPane.showConfirmDialog(this, "¿Cancelar pedido #" + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) orders.cancel(id);
        }));
        panel.add(buttons, BorderLayout.SOUTH); return panel;
    }

    private JPanel productsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JScrollPane(adminTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(button("Registrar", () -> productDialog(null)));
        actions.add(button("Modificar", () -> productDialog(selectedProduct())));
        actions.add(button("Eliminar", () -> {
            ProductCatalog.Entry entry = selectedProduct();
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar " + entry.product().getName() + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                catalog.remove(entry.code());
                refreshMenu();
            }
        }));
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private ProductCatalog.Entry selectedProduct() {
        int row = adminTable.getSelectedRow();
        if (row < 0) throw new IllegalArgumentException("Selecciona un producto");
        return menu.get(adminTable.convertRowIndexToModel(row));
    }

    private void productDialog(ProductCatalog.Entry entry) {
        Product current = entry == null ? null : entry.product();
        Combo combo = current instanceof Combo c ? c : null;
        JTextField code = new JTextField(entry == null ? "" : entry.code(), 18);
        code.setEditable(entry == null);
        JTextField name = new JTextField(current == null ? "" : current.getName(), 18);
        JTextField description = new JTextField(current == null ? "" : combo == null ? current.getDescription() : combo.getBaseDescription(), 18);
        JTextField price = new JTextField(current == null ? "" : combo == null ? current.getPrice().toPlainString() : String.valueOf(combo.getDiscountPercent()), 18);
        JComboBox<String> category = new JComboBox<>(new String[]{"Bebida", "Alimento"});
        if (current != null && current.getCategory() == ProductCategory.FOOD) category.setSelectedIndex(1);
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Código:")); form.add(code);
        form.add(new JLabel("Nombre:")); form.add(name);
        form.add(new JLabel("Descripción:")); form.add(description);
        form.add(new JLabel(combo == null ? "Precio en pesos:" : "Descuento (%):")); form.add(price);
        if (combo == null) {
            form.add(new JLabel("Categoría:")); form.add(category);
        }
        while (JOptionPane.showConfirmDialog(this, form, entry == null ? "Registrar producto" : "Modificar producto", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Product product = combo == null
                        ? ProductFactory.simple(name.getText(), description.getText(), price.getText(), category.getSelectedIndex() == 0 ? ProductCategory.BEVERAGE : ProductCategory.FOOD)
                        : ProductFactory.editCombo(combo, name.getText(), description.getText(), price.getText());
                if (entry == null) catalog.register(code.getText(), product);
                else catalog.update(entry.code(), product);
                refreshMenu();
                return;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void refreshMenu() {
        menu = List.copyOf(catalog.entries());
        menuModel.setRowCount(0);
        for (var entry : menu) {
            Product p = entry.product();
            menuModel.addRow(new Object[]{entry.code(), p.getName(), p.getCategory().getLabel(), Money.format(p.getPrice()), p.getDescription()});
        }
    }

    private JPanel promotionsPanel() {
        DefaultTableModel m = model("Código", "Descripción");
        for (Promotion p : promoList) m.addRow(new Object[]{p.getCode(), p.getDescription()});
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(new JTable(m))); return panel;
    }

    private void addProduct() {
        int row = menuTable.getSelectedRow();
        if (row < 0) throw new IllegalArgumentException("Selecciona un producto del menú");
        Product product = menu.get(menuTable.convertRowIndexToModel(row)).product();
        List<CustomizationOption> options = CustomizationCatalog.availableFor(product);
        if (!options.isEmpty()) {
            JPanel panel = new JPanel(new GridLayout(0, 1));
            List<JCheckBox> checks = new ArrayList<>();
            for (CustomizationOption option : options) {
                JCheckBox check = new JCheckBox(option.label() + " (+" + Money.format(option.price()) + ")");
                checks.add(check); panel.add(check);
            }
            if (JOptionPane.showConfirmDialog(this, panel, "Personalizar " + product.getName(), JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            for (int i = 0; i < checks.size(); i++) if (checks.get(i).isSelected()) product = options.get(i).applyTo(product);
        }
        cart.add(new OrderLine(product, (Integer) quantity.getValue()));
        refreshCart();
    }

    private void removeLine() {
        int row = cartTable.getSelectedRow();
        if (row < 0) throw new IllegalArgumentException("Selecciona una línea del pedido");
        cart.remove(row); refreshCart();
    }

    private OrderBuilder builder() {
        OrderBuilder builder = new OrderBuilder().customer(customer.getText());
        for (OrderLine line : cart) builder.add(line.product(), line.quantity());
        if (promo.getSelectedIndex() > 0) builder.promotion(promoList.get(promo.getSelectedIndex() - 1));
        return builder;
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (OrderLine line : cart) cartModel.addRow(new Object[]{line.product().getName(), line.product().getDescription(), line.quantity(), Money.format(line.total())});
        if (cart.isEmpty()) { total.setText("Total: $0"); hint.setText(" "); return; }
        Order draft = builder().preview();
        total.setText("Total: " + Money.format(draft.total()));
        hint.setText(promo.getSelectedIndex() > 0 && draft.discount().signum() == 0 ? "La promoción no aplica" : "Descuento: " + Money.format(draft.discount()));
    }

    private void showReceipt(Order order) {
        JTextArea text = new JTextArea(order.receipt(), 16, 42);
        text.setEditable(false); text.setFont(receipt.getFont());
        JOptionPane.showMessageDialog(this, new JScrollPane(text), "Recibo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmOrder() {
        OrderBuilder builder = builder();
        Order draft = builder.preview();
        if (promo.getSelectedIndex() > 0 && draft.discount().signum() == 0) builder.promotion(null);
        if (JOptionPane.showConfirmDialog(this, "Confirmar pedido por " + Money.format(draft.total()) + "?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        Order order = orders.place(builder);
        cart.clear(); customer.setText(""); promo.setSelectedIndex(0); refreshCart();
        showReceipt(order);
    }

    private long selectedOrder() {
        int row = orderTable.getSelectedRow();
        if (row < 0) throw new IllegalArgumentException("Selecciona un pedido registrado");
        return (Long) orderModel.getValueAt(row, 0);
    }

    private void refreshOrders() {
        int selected = orderTable.getSelectedRow();
        Long id = selected < 0 ? null : (Long) orderModel.getValueAt(selected, 0);
        orderModel.setRowCount(0);
        for (Order order : orders.all()) {
            int index = orderModel.getRowCount();
            orderModel.addRow(new Object[]{order.getId(), order.getCustomerName(), order.getStatus().getLabel(), Money.format(order.total())});
            if (id != null && id.equals(order.getId())) orderTable.setRowSelectionInterval(index, index);
        }
    }

    @Override public void onStatusChanged(Order order, OrderStatus previous, OrderStatus current) {
        Runnable update = () -> {
            notifications.append("Pedido #" + order.getId() + ": " + (previous == null ? "" : previous.getLabel() + " → ") + current.getLabel() + "\n");
            notifications.setCaretPosition(notifications.getDocument().getLength());
            refreshOrders();
        };
        if (SwingUtilities.isEventDispatchThread()) update.run(); else SwingUtilities.invokeLater(update);
    }
}
