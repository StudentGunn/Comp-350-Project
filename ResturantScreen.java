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
        
        JLabel label = new JLabel("Select items to order:");
        panel.add(label);
        
        JCheckBox[] checkBoxes = new JCheckBox[menuItems.length];
        JSpinner[] quantities = new JSpinner[menuItems.length];
        
        for (int i = 0; i < menuItems.length; i++) {
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            checkBoxes[i] = new JCheckBox(menuItems[i]);
            quantities[i] = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
            quantities[i].setPreferredSize(new Dimension(50, 25));
            itemPanel.add(checkBoxes[i]);
            itemPanel.add(quantities[i]);
            panel.add(itemPanel);
        }

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
                    // Create the order in the database
                    long orderId = parent.userDb.createOrder(username, restaurantName, total);
                    JOptionPane.showMessageDialog(this,
                        String.format("Order #%d placed successfully!\nTotal: $%.2f", orderId, total),
                        "Order Confirmation",
                        JOptionPane.INFORMATION_MESSAGE);
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
