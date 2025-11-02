import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

public class ResturantScreen extends JPanel {
    private FoodDeliveryLoginUI parent;
    private String username;
    private String zip;

    public ResturantScreen(FoodDeliveryLoginUI parent, String username, String zip) {
        this.parent = parent;
        this.username = username;
        this.zip = zip;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(5,5));
        
        // Create header panel with better styling
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        headerPanel.setBackground(new Color(245, 245, 245));
        
        JLabel header = new JLabel("Available Restaurants - " + zip, SwingConstants.LEFT);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 14f));
        headerPanel.add(header, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Content panel with compact spacing
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        if ("02325".equals(zip)) {
            content.add(createRestaurantRow("Crimson Dining", "125 Burrill Ave"));
            content.add(Box.createVerticalStrut(4)); // Reduced spacing
            content.add(createRestaurantRow("Barrett's Alehouse Bridgewater", "425 Bedford St"));
            content.add(Box.createVerticalStrut(4)); // Reduced spacing
            content.add(createRestaurantRow("Greyhound Tavern", "39 Broad Street"));
        } else {
            JPanel noResultsPanel = new JPanel(new BorderLayout());
            noResultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            
            JLabel none = new JLabel("No restaurants available in " + zip, SwingConstants.CENTER);
            none.setFont(none.getFont().deriveFont(12f));
            none.setForeground(Color.GRAY);
            
            noResultsPanel.add(none, BorderLayout.CENTER);
            content.add(noResultsPanel);
        }

        JScrollPane scroll = new JScrollPane(content);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createRestaurantRow(String name, String address) {
        JPanel row = new JPanel(new BorderLayout(8,8));
        row.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        
        // Restaurant name in bold
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
        info.add(nameLabel);
        
        // Address in smaller font
        JLabel addressLabel = new JLabel(address);
        addressLabel.setFont(addressLabel.getFont().deriveFont(11f));
        addressLabel.setForeground(Color.DARK_GRAY);
        info.add(addressLabel);
        
        row.add(info, BorderLayout.CENTER);

        JButton orderBtn = new JButton("Order here");
        orderBtn.setPreferredSize(new Dimension(90, 25));
        orderBtn.setFont(orderBtn.getFont().deriveFont(11f));
        orderBtn.addActionListener(e -> createOrder(name));
        row.add(orderBtn, BorderLayout.EAST);
        row.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return row;
    }

    private void createOrder(String restaurantName) {
        // Show menu options (simplified for demo)
        String[] menuItems = {
            "Burger - $12.99",
            "Pizza - $15.99",
            "Salad - $8.99",
            "Pasta - $13.99",
            "Sandwich - $9.99"
        };
        double[] prices = {12.99, 15.99, 8.99, 13.99, 9.99};

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel("Select items to order from " + restaurantName);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        
        JCheckBox[] checkBoxes = new JCheckBox[menuItems.length];
        JSpinner[] quantities = new JSpinner[menuItems.length];
        
        for (int i = 0; i < menuItems.length; i++) {
            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            checkBoxes[i] = new JCheckBox(menuItems[i]);
            checkBoxes[i].setFont(checkBoxes[i].getFont().deriveFont(12f));
            
            quantities[i] = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
            quantities[i].setPreferredSize(new Dimension(60, 25));
            ((JSpinner.DefaultEditor)quantities[i].getEditor()).getTextField().setColumns(2);
            
            itemPanel.add(checkBoxes[i]);
            itemPanel.add(quantities[i]);
            panel.add(itemPanel);
        }
        
        // Add a note about quantity
        JLabel noteLabel = new JLabel("* Use spinners to select quantity (1-10)");
        noteLabel.setFont(noteLabel.getFont().deriveFont(10f));
        noteLabel.setForeground(Color.GRAY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(noteLabel);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Order from " + restaurantName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            double total = 0.0;
            boolean anySelected = false;
            
            StringBuilder orderDetails = new StringBuilder();
            orderDetails.append("Order Summary:\n\n");
            
            for (int i = 0; i < checkBoxes.length; i++) {
                if (checkBoxes[i].isSelected()) {
                    anySelected = true;
                    int quantity = (Integer)quantities[i].getValue();
                    double itemTotal = prices[i] * quantity;
                    total += itemTotal;
                    orderDetails.append(String.format("%dx %s: $%.2f\n", 
                        quantity, menuItems[i].split(" - ")[0], itemTotal));
                }
            }
            
            if (!anySelected) {
                JOptionPane.showMessageDialog(this,
                    "Please select at least one item to order.",
                    "No Items Selected",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            orderDetails.append(String.format("\nTotal: $%.2f", total));
            
            int confirm = JOptionPane.showConfirmDialog(this,
                orderDetails.toString(),
                "Confirm Order",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
                
            if (confirm == JOptionPane.OK_OPTION) {
                try {
                    // Count total items
                    int totalItems = 0;
                    for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i].isSelected()) {
                            totalItems += (Integer)quantities[i].getValue();
                        }
                    }
                    
                    // Get active payment method
                    PaymentInformation paymentInfo = parent.paymentDb.getActivePaymentMethod(username);
                    if (paymentInfo == null) {
                        JOptionPane.showMessageDialog(this,
                            "Please set up a payment method first.",
                            "Payment Required",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Create the order in the database
                    long orderId = parent.orderDb.createOrder(username, restaurantName, 
                        "123 Main St", // TODO: Get actual delivery address
                        "No special instructions", // TODO: Add special instructions field
                        total,
                        totalItems,  // Using the total items count we calculated above
                        paymentInfo.getPaymentType());
                    
                    // Add each ordered item to the database
                    for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i].isSelected()) {
                            int quantity = (Integer)quantities[i].getValue();
                            String itemName = menuItems[i].split(" - ")[0];
                            double price = prices[i];
                            parent.orderDb.addOrderItem(orderId, itemName, quantity, price, null);
                        }
                    }
                    
                    // Calculate and show ETA
                    ETA eta = new ETA((int)orderId, totalItems);
                    String confirmMessage = String.format(
                        "Order #%d placed successfully!\n" +
                        "Total: $%.2f\n\n" +
                        "%s",
                        orderId, total, eta.getETAMessage());
                    
                    JOptionPane.showMessageDialog(this,
                        confirmMessage,
                        "Order Confirmation",
                        JOptionPane.INFORMATION_MESSAGE);
                        
                    // Switch back to main screen to show order status
                    MainScreen mainScreen = new MainScreen(parent, username);
                    parent.getSceneSorter().addScene("MainScreen", mainScreen);
                    parent.getSceneSorter().switchPage("MainScreen");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error creating order: " + ex.getMessage(),
                        "Order Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
