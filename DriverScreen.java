import java.awt.*;
import javax.swing.*;

public class DriverScreen extends JPanel {
    private final FoodDeliveryLoginUI parent;
    private final String username;

    public DriverScreen(FoodDeliveryLoginUI parent, String username) {
	this.parent = parent;
	this.username = username == null || username.isEmpty() ? "Driver" : username;
	initUI();
    }

    private void initUI() {
	setLayout(new BorderLayout(8,8));
	setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

	JLabel title = new JLabel("Welcome Driver " + username, SwingConstants.LEFT);
	title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
	add(title, BorderLayout.NORTH);

	JPanel center = new JPanel();
	center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
	center.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

	JButton getOrderBtn = new JButton("GetOrder");
	JButton deliveryHistoryBtn = new JButton("Delivery History");
	JButton paymentHistoryBtn = new JButton("Payment History");
	JButton paymentMethodBtn = new JButton("Payment Method");

	Dimension btnSize = new Dimension(160, 30);
	getOrderBtn.setMaximumSize(btnSize);
	deliveryHistoryBtn.setMaximumSize(btnSize);
	paymentHistoryBtn.setMaximumSize(btnSize);
	paymentMethodBtn.setMaximumSize(btnSize);

	getOrderBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
	deliveryHistoryBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
	paymentHistoryBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
	paymentMethodBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

	center.add(getOrderBtn);
	center.add(Box.createRigidArea(new Dimension(0,6)));
	center.add(deliveryHistoryBtn);
	center.add(Box.createRigidArea(new Dimension(0,6)));
	center.add(paymentHistoryBtn);
	center.add(Box.createRigidArea(new Dimension(0,6)));
	center.add(paymentMethodBtn);

	add(center, BorderLayout.CENTER);

	

	

	// Connect to DriverGetOrder screen
	getOrderBtn.addActionListener(e -> {
		DriverGetOrder getOrderScreen = new DriverGetOrder(parent, username);
		try {
			parent.getSceneSorter().addScene("DriverGetOrder", getOrderScreen);
		} catch (IllegalArgumentException ex) {
			// Scene already exists, that's fine
		}
		parent.getSceneSorter().switchPage("DriverGetOrder");
	});

	deliveryHistoryBtn.addActionListener(e -> {
		DriveryHistory historyScreen = new DriveryHistory(parent, username);
		try {
			parent.getSceneSorter().addScene("DriverHistory", historyScreen);
		} catch (IllegalArgumentException ex) {
			// Scene already exists, that's fine
		}
		parent.getSceneSorter().switchPage("DriverHistory");
	});

	paymentHistoryBtn.addActionListener(e -> JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
		"Payment history not implemented yet.", "Payment History", JOptionPane.INFORMATION_MESSAGE));

	paymentMethodBtn.addActionListener(e -> {
		DriverSetPaymentMethod paymentMethodScreen = new DriverSetPaymentMethod(parent, username);
		try {
			parent.getSceneSorter().addScene("DriverSetPaymentMethod", paymentMethodScreen);
		} catch (IllegalArgumentException ex) {
			// Scene already exists, that's fine
		}
		parent.getSceneSorter().switchPage("DriverSetPaymentMethod");
		
	});
	
    }
}
