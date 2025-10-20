import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/*
 * Simple Swing login/register UI for the Food Delivery app.
 * - Top contains: welcome message
 * - Center: username/password fields with Login and Register
 * - Users create  to a local CSV file `users.csv` in the working directory for UserData Base
 * - Passwords are stored as SHA-256 hex hashes 
 */
public class FoodDeliveryLoginUI {

    private static final Path USER_DB = Path.of("users.csv");

    private final JFrame frame = new JFrame("Food Delivery Service");
    private final JPanel main = new JPanel(new BorderLayout(10, 10));

    private final JTextField userField = new JTextField(15);
    private final JPasswordField passField = new JPasswordField(15);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);

    private final JPanel centerPanel = new JPanel(new GridBagLayout());

    private final Map<String, String> users = new HashMap<>(); // username -> sha256(password)

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FoodDeliveryLoginUI app = new FoodDeliveryLoginUI();
            app.loadUsers();
            app.createAndShow();
        });
    }

    private void createAndShow() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(420, 260));

        JLabel title = new JLabel("Welcome to ordering with Food Delivery Service", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        main.add(title, BorderLayout.NORTH);

        buildCenter();
        main.add(centerPanel, BorderLayout.CENTER);

        messageLabel.setForeground(Color.RED);
        main.add(messageLabel, BorderLayout.SOUTH);

        frame.setContentPane(main);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void buildCenter() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.EAST;
        centerPanel.add(new JLabel("Username:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        centerPanel.add(userField, c);

        c.gridx = 0; c.gridy = 1; c.anchor = GridBagConstraints.EAST;
        centerPanel.add(new JLabel("Password:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        centerPanel.add(passField, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(this::onLogin);
        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(this::onRegister);
        btns.add(loginBtn);
        btns.add(registerBtn);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        centerPanel.add(btns, c);
    }

    private void onLogin(ActionEvent e) {
        messageLabel.setForeground(Color.RED);
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        String hash = sha256Hex(pass);
        String stored = users.get(user);
        if (stored != null && stored.equals(hash)) {
            messageLabel.setForeground(new Color(0, 128, 0));
            messageLabel.setText("Login successful. Welcome, " + user + "!");
            // Clear the fields
            passField.setText("");
        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }

    private void onRegister(ActionEvent e) {
        String user = JOptionPane.showInputDialog(frame, "Choose a username:", "Register", JOptionPane.QUESTION_MESSAGE);
        if (user == null) return; // cancelled
        user = user.trim();
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Username cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (users.containsKey(user)) {
            JOptionPane.showMessageDialog(frame, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(frame, pf, "Enter password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        String pass = new String(pf.getPassword()).trim();
        if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String hash = sha256Hex(pass);
        users.put(user, hash);
        try {
            appendUserToFile(user, hash);
            JOptionPane.showMessageDialog(frame, "Registered successfully. You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to save user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- users ---

    private void loadUsers() {
        users.clear();
        if (!Files.exists(USER_DB)) return;
        try (BufferedReader r = Files.newBufferedReader(USER_DB, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) users.put(parts[0], parts[1]);
            }
        } catch (IOException ex) {
            System.err.println("Failed to read user db: " + ex.getMessage());
        }
    }

    private void appendUserToFile(String user, String hash) throws IOException {
        if (!Files.exists(USER_DB)) {
            Files.createFile(USER_DB);
        }
        String line = user + "," + hash + System.lineSeparator();
        Files.writeString(USER_DB, line, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    // --- Save User & Pass/ ---

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(b.length * 2);
            for (byte x : b) sb.append(String.format("%02x", x & 0xff));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
