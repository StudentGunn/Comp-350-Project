import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainScreen extends JPanel {
    private String username;
    private String zipCode = "";
    private String email = "you@example.com"; // placeholder profile field
    private FoodDeliveryLoginUI parent;

    public MainScreen(FoodDeliveryLoginUI parent, String username) {
        this.parent = parent;
        this.username = (username == null || username.isEmpty()) ? "User" : username;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(18f));
        add(welcomeLabel, BorderLayout.NORTH);

        // Create a panel to hold the content with some padding
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel center = new JPanel();
        center.setLayout(new GridLayout(3, 1, 10, 10));

        JButton orderBtn = new JButton("Order");
        JButton zipBtn = new JButton("Enter Zip Code");
        JButton profileBtn = new JButton("My Profile");

        center.add(orderBtn);
        center.add(zipBtn);
        center.add(profileBtn);

        JPanel padding = new JPanel(new BorderLayout());
        padding.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        padding.add(center, BorderLayout.CENTER);
        contentPanel.add(padding, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Actions
        orderBtn.addActionListener(e -> openOrderScreen());
        zipBtn.addActionListener(e -> promptZipCode());
        profileBtn.addActionListener(e -> openProfileDialog());
    }

    private void openOrderScreen() {
        // Placeholder for actual order screen navigation
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                "Order screen not implemented.\n(Here you would start the ordering flow.)",
                "Order", JOptionPane.INFORMATION_MESSAGE);
    }

    private void promptZipCode() {
        String input = JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(this),
                "Enter your 5-digit zip code:", zipCode.isEmpty() ? "" : zipCode);
        if (input != null) {
            input = input.trim();
            if (input.matches("\\d{5}")) {
                zipCode = input;
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        "Zip code set to: " + zipCode,
                        "Zip Code Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        "Please enter a valid 5-digit zip code.",
                        "Invalid Zip", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void openProfileDialog() {
        String message = String.format("Username: %s%nEmail: %s%nZip Code: %s",
                username, email, zipCode.isEmpty() ? "(not set)" : zipCode);
        int option = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(this),
                message,
                "My Profile",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[] { "Edit Email", "Close" },
                "Close");

        if (option == 0) {
            String newEmail = JOptionPane.showInputDialog(this, "Enter new email:", email);
            if (newEmail != null && !newEmail.trim().isEmpty()) {
                email = newEmail.trim();
                JOptionPane.showMessageDialog(this, "Email updated.", "Profile", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

}