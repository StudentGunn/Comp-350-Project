import java.awt.*;
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
        orderBtn.addActionListener(e -> {
            // replace with real ordering flow
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(ResturantScreen.this),
                    "Ordering from " + name + "\nAddress: " + address,
                    "Order",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        row.add(orderBtn, BorderLayout.EAST);
        row.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return row;
    }
}
