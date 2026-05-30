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

        try {
            ImageIcon appIcon = new ImageIcon("pictures/img.png");
            setIconImage(appIcon.getImage());
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon aplikasi: " + e.getMessage());
        }

        JPanel backgroundPanel = new JPanel(new GridBagLayout()) {
            private final Image backgroundImage = new ImageIcon("pictures/loginbackground.jpg").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imgWidth = backgroundImage.getWidth(this);
                    int imgHeight = backgroundImage.getHeight(this);

                    if (imgWidth <= 0 || imgHeight <= 0) return;

                    double panelRatio = (double) panelWidth / (double) panelHeight;
                    double imgRatio = (double) imgWidth / (double) imgHeight;

                    int drawWidth, drawHeight;

                    if (panelRatio > imgRatio) {
                        drawWidth = panelWidth;
                        drawHeight = (int) (panelWidth / imgRatio);
                    } else {
                        drawHeight = panelHeight;
                        drawWidth = (int) (panelHeight * imgRatio);
                    }

                    int x = 0;
                    int y = 0;

                    g2d.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
                }
            }
        };

        islandContainer.setOpaque(false);
        islandContainer.add(new LoginPanel(this, cardLayout, islandContainer), "LOGIN");
        islandContainer.add(new SignUpPanel(cardLayout, islandContainer), "SIGNUP");
        backgroundPanel.add(islandContainer);
        add(backgroundPanel);
    }
}