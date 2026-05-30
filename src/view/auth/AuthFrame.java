package view.auth;

import javax.swing.*;
import java.awt.*;

public class AuthFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel islandContainer = new JPanel(cardLayout);

    public AuthFrame() {
        setTitle("NIG Clothing - Authentication");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel backgroundPanel = new JPanel(new GridBagLayout()) {
            private final Image backgroundImage = new ImageIcon("pictures/loginbackground.jpg").getImage();
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };

        islandContainer.setOpaque(false);
        islandContainer.add(new LoginPanel(this, cardLayout, islandContainer), "LOGIN");
        islandContainer.add(new SignUpPanel(cardLayout, islandContainer), "SIGNUP");
        backgroundPanel.add(islandContainer);
        add(backgroundPanel);
    }
}