import java.io.IOException;
import javax.swing.*;

public class Main {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FoodDeliveryLoginUI app = new FoodDeliveryLoginUI();
            // initialize SQLite DB
            try {
                app.userDb = new UserDataBase(java.nio.file.Path.of("users.db"));
                app.userDb.init();
            } catch (Exception ex) {
                ex.printStackTrace();
                // Surface initialization error in the UI so user sees that DB is required
                JOptionPane.showMessageDialog(null, "Failed to initialize user database: " + ex.getMessage(), "DB error", JOptionPane.ERROR_MESSAGE);
            }
            try {
                app.setBackgroundImage(java.nio.file.Paths.get("C:\\Users\\skylg\\OneDrive\\Desktop\\Food Deilvery app.jpg"), true);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to load background image: " + ex.getMessage(), "Image load error", JOptionPane.ERROR_MESSAGE);
            }
            app.createAndShow();
        });
    }
}

// main() summary:
// - runs on the Event Dispatch Thread using SwingUtilities.invokeLater
// - constructs FoodDeliveryLoginUI, attempts to initialize the SQLite-backed
//   UserDatabase (creating users.db if missing) and loads a background image
// - any initialization failures are shown to the user via dialogs so they
//   understand DB or image problems early
