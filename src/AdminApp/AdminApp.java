package AdminApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class AdminApp extends JFrame {

    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "11111";


    public AdminApp() {
        setTitle("NIG Clothing - Admin Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

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

        backgroundPanel.add(createLoginPanel());
        add(backgroundPanel);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0, 0, 0, 210));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font fieldFont = new Font("Arial", Font.PLAIN, 18);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(40, 40, 10, 40);
        JLabel logoLabel = new JLabel(scaleImage("pictures/logo.png", 200, true));
        panel.add(logoLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 15, 40);
        JLabel welcomeText = new JLabel("STAFF LOGIN", SwingConstants.CENTER);
        welcomeText.setFont(new Font("Arial", Font.BOLD, 14));
        welcomeText.setForeground(Color.WHITE);
        panel.add(welcomeText, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 40, 15, 40);
        JTextField usernameField = new JTextField("Username");
        usernameField.setFont(fieldFont);
        usernameField.setForeground(Color.GRAY);
        usernameField.setPreferredSize(new Dimension(300, 40));

        usernameField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (usernameField.getText().equals("Username")) {
                    usernameField.setText("");
                    usernameField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent evt) {
                if (usernameField.getText().isEmpty()) {
                    usernameField.setForeground(Color.GRAY);
                    usernameField.setText("Username");
                }
            }
        });
        panel.add(usernameField, gbc);

        gbc.gridy = 3;
        JPasswordField passwordField = new JPasswordField("Password");
        passwordField.setFont(fieldFont);
        passwordField.setForeground(Color.GRAY);
        passwordField.setPreferredSize(new Dimension(300, 40));
        passwordField.setEchoChar((char) 0);

        passwordField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                String pass = String.valueOf(passwordField.getPassword());
                if (pass.equals("Password")) {
                    passwordField.setText("");
                    passwordField.setForeground(Color.BLACK);
                    passwordField.setEchoChar('\u2022');
                }
            }
            public void focusLost(FocusEvent evt) {
                String pass = String.valueOf(passwordField.getPassword());
                if (pass.isEmpty()) {
                    passwordField.setForeground(Color.GRAY);
                    passwordField.setText("Password");
                    passwordField.setEchoChar((char) 0);
                }
            }
        });
        panel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(10, 40, 40, 40);
        JButton backBtn = new JButton("Back");
        backBtn.setBackground(new Color(100, 100, 100));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.addActionListener(e -> {
            new app.Main().setVisible(true);
            this.dispose();
        });
        backBtn.setPreferredSize(new Dimension(100, 50));

        JButton loginButton = new JButton("Access Portal");
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setPreferredSize(new Dimension(190, 50));
        loginButton.setBackground(new Color(220, 50, 50));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = String.valueOf(passwordField.getPassword());

            if (user.equals(ADMIN_USERNAME) && pass.equals(ADMIN_PASSWORD)) {

                JOptionPane.showMessageDialog(panel, "Access Granted. Welcome, Admin.", "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                AdminDashboard dashboard = new AdminDashboard();
                dashboard.setVisible(true);
                AdminApp.this.dispose();

            } else {
                JOptionPane.showMessageDialog(panel, "Invalid admin credentials.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backBtn);
        buttonPanel.add(loginButton);

        panel.add(buttonPanel, gbc);
        return panel;
    }

    private ImageIcon scaleImage(String imagePath, int targetSize, boolean lockWidth) {
        ImageIcon originalIcon = new ImageIcon(imagePath);
        Image originalImage = originalIcon.getImage();
        if (originalImage == null || originalIcon.getIconWidth() == -1) {
            return originalIcon;
        }
        Image scaledImage;
        if (lockWidth) {
            scaledImage = originalImage.getScaledInstance(targetSize, -1, Image.SCALE_SMOOTH);
        } else {
            scaledImage = originalImage.getScaledInstance(-1, targetSize, Image.SCALE_SMOOTH);
        }
        return new ImageIcon(scaledImage);
    }
}