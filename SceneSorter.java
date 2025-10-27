import java.awt.*;
import javax.swing.JPanel;
import java.util.HashMap;
import java.util.Map;

public class SceneSorter {
    private final JPanel cardsPanel;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> scenes = new HashMap<>();

    public SceneSorter() {
        this.cardLayout = new CardLayout();
        this.cardsPanel = new JPanel(cardLayout);
    }

    public void addScene(String name, JPanel scene) {
        if (scenes.containsKey(name)) {
            throw new IllegalArgumentException("A scene with this name already exists.");
        }
        scenes.put(name, scene);
        cardsPanel.add(scene, name);
    }

    public void switchPage(String name) {
        if (!scenes.containsKey(name)) {
            throw new IllegalArgumentException("There is no page named " + name);
        }
        cardLayout.show(cardsPanel, name);
    }
    public JPanel getCardsPanel() {
        return cardsPanel;
    }
}
