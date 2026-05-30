package app;

import view.auth.AuthFrame;
import AdminApp.AdminApp;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    public Main() {
        setTitle("NIG Clothing - System Gateway");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Makes it full-screen like the login panels
        setLocationRelativeTo(null);

        // ==========================================
        // THE OCEAN BACKGROUND
        // ==========================================
        JPanel backgroundPanel = new JPanel(new GridBagLayout()) {
            private final Image backgroundImage = new ImageIcon("pictures/loginbackground.jpg").getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        // ==========================================
        // THE SEMI-TRANSPARENT CONTAINER
        // ==========================================
        JPanel containerPanel = new JPanel(new GridBagLayout());
        containerPanel.setBackground(new Color(0, 0, 0, 210)); // The dark glass effect!
        containerPanel.setBorder(BorderFactory.createEmptyBorder(60, 60, 60, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // 1. Title
        gbc.gridy = 0;
        JLabel title = new JLabel("SYSTEM GATEWAY", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        containerPanel.add(title, gbc);

        // 2. Subtitle
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 30, 20);
        JLabel subTitle = new JLabel("Select your access level to continue", SwingConstants.CENTER);
        subTitle.setFont(new Font("Arial", Font.PLAIN, 18));
        subTitle.setForeground(Color.LIGHT_GRAY);
        containerPanel.add(subTitle, gbc);

        // Reset insets for the buttons
        gbc.insets = new Insets(10, 20, 15, 20);

        // 3. Customer Button
        gbc.gridy = 2;
        JButton customerBtn = new JButton("Enter as CUSTOMER");
        customerBtn.setFont(new Font("Arial", Font.BOLD, 20));
        customerBtn.setPreferredSize(new Dimension(350, 60));
        customerBtn.setBackground(new Color(50, 150, 250));
        customerBtn.setForeground(Color.WHITE);
        customerBtn.setFocusPainted(false);
        customerBtn.addActionListener(e -> {
            new AuthFrame().setVisible(true);
            this.dispose();
        });
        containerPanel.add(customerBtn, gbc);

        // 4. Admin Button
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 20, 20, 20);
        JButton adminBtn = new JButton("Enter as ADMINISTRATOR");
        adminBtn.setFont(new Font("Arial", Font.BOLD, 20));
        adminBtn.setPreferredSize(new Dimension(350, 60));
        adminBtn.setBackground(new Color(220, 50, 50));
        adminBtn.setForeground(Color.WHITE);
        adminBtn.setFocusPainted(false);
        adminBtn.addActionListener(e -> {
            new AdminApp().setVisible(true);
            this.dispose();
        });
        containerPanel.add(adminBtn, gbc);

        // Add the dark container to the center of the ocean background
        backgroundPanel.add(containerPanel);
        add(backgroundPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}