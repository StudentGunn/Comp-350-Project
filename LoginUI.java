import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class LoginUI extends FoodDeliveryLoginUI {

    private final JTextField userField = new JTextField(15);
    private final JPasswordField passField = new JPasswordField(15);

    public JPanel buildLoginPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);


        // - creates the username and password label+field pairs and positions
        //   them using GridBagLayout constraints
        // - creates Login and Register buttons and attaches action listeners
        //   (loginBtn -> onLogin, registerBtn -> onRegister)
        // - the buttons panel is added to the center area so user can submit
        //   the form; all components are standard Swing components (JLabel,
        //   JTextField, JPasswordField, JButton) and listeners receive
        //   ActionEvent when triggered.
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

        // wrap the centerPanel in a translucent white card so controls are readable
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setOpaque(false);
        JPanel translucentCard = new JPanel(new GridBagLayout());
        translucentCard.setOpaque(true);
        translucentCard.setBackground(new Color(255,255,255,220));
        translucentCard.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        translucentCard.add(centerPanel);
        cardWrapper.add(translucentCard);

        return cardWrapper;
    }
    // buildLoginPage Summary:

    // This is used by sceneSorter as the logic for the
    // "Login" UI. When switchPage is used to call "Login",
    // this logic will be used.

    // Upload background removed; background controlled programmatically.
    public void onLogin(ActionEvent e) {
        messageLabel.setForeground(Color.RED);
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Please enter username and password.");
            return;
        }
        // Hash the password input to a hex string. sha256Hex returns a String.
        // Steps:
        // 1) read password char[] from passField and convert to String
        // 2) call sha256Hex(pass) to produce a hex String
        // 3) pass the username (String) and password hash (String) to
        //    userDb.authenticate(user, hash) which compares the stored value
        // Note: both username and hash are passed as JDBC Strings to PreparedStatement.setString
        String hash = sha256Hex(pass);
        try {
            if (userDb == null) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("User database not initialized.");
                return;
            }
            // Ask the database to verify the provided password hash
            // matches the stored value for this username.
            boolean ok = userDb.authenticate(user, hash);
            if (ok) {
                messageLabel.setForeground(new Color(0, 128, 0));
                messageLabel.setText("Login successful. Welcome, " + user + "!");
                passField.setText("");
            } else {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Invalid username or password.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("DB error: " + ex.getMessage());
        }
    }

    // onLogin(ActionEvent):
    // - validates non-empty inputs
    // - computes SHA-256 hex of password
    // - calls userDb.authenticate(username, hash) to check credentials
    // - updates messageLabel with success or failure messages
    public void onRegister(ActionEvent e) {
        // Registration flow (step-level):
        // 1) Prompt for username (String) via JOptionPane and trim whitespace
        // 2) Prompt for password in a JPasswordField (char[]), convert to String
        // 3) Compute password hash (sha256Hex) producing a String hex
        // 4) Call userDb.userExists(user) which uses p.setString(1, user)
        //    to bind the username as a JDBC String for the SELECT
        // 5) If not exists, call userDb.register(user, hash): this uses
        //    p.setString for username/hash and p.setLong for created_at
        String user = JOptionPane.showInputDialog(null, "Choose a username:", "Register", JOptionPane.QUESTION_MESSAGE);
        if (user == null) return;
        user = user.trim();
        if (user.isEmpty()) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Username cannot be empty.");
            return;
        }
        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(null, pf, "Enter password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        String pass = new String(pf.getPassword()).trim();
        if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String hash = sha256Hex(pass);
        try {
            if (userDb == null) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("User database not initialized.");
                return;
            }
            // Check DB to ensure the username isn't already taken.
            if (userDb.userExists(user)) {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("Username already exists.");
                return;
            }
            // Insert the new user record into the SQLite database.
            userDb.register(user, hash);
            messageLabel.setForeground(new Color(0, 128, 0));
            messageLabel.setText("Registered successfully. You can now login.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            messageLabel.setForeground(Color.RED);
            messageLabel.setText("Failed to save user: " + ex.getMessage());
        }
    }

    // onRegister:

    // The logic used when the user clicks the "Register" button on the "Login" page.

    // No CSV fallback: persistence is provided by the SQLite-backed UserDatabase only.

}
// Summary:
// Login UI handles the JPanel for the login page,
// as well as the logic for both registering and logging in.