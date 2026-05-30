package view.dashboard;

import controller.OrderController;
import view.component.OrderCard;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PurchasedPanel extends JPanel {
    private DashboardFrame parentFrame;
    private OrderController orderController;
    private JPanel purchasedContainerPanel;

    public PurchasedPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.orderController = new OrderController();

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel("Your Order History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        purchasedContainerPanel = new JPanel();
        purchasedContainerPanel.setLayout(new BoxLayout(purchasedContainerPanel, BoxLayout.Y_AXIS));
        purchasedContainerPanel.setBackground(new Color(245, 245, 245));
        purchasedContainerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(purchasedContainerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshPurchasedData() {
        purchasedContainerPanel.removeAll();

        List<String[]> orders = orderController.getOrderHistory(parentFrame.getLoggedInUser());

        if (orders.isEmpty()) {
            JLabel emptyLabel = new JLabel("You haven't placed any orders yet.");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            purchasedContainerPanel.add(emptyLabel);
        } else {
            for (String[] order : orders) {
                purchasedContainerPanel.add(new OrderCard(order[0], order[1], order[2], order[3], parentFrame, orderController));
                purchasedContainerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }

        purchasedContainerPanel.revalidate();
        purchasedContainerPanel.repaint();
    }
}