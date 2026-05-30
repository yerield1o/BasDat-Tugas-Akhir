package view.auth;

import controller.AuthController;
import util.ImageLoader;
import view.dashboard.DashboardFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {
    private AuthController authController;

    public LoginPanel(JFrame parentFrame, CardLayout cardLayout, JPanel parentContainer) {
        this.authController = new AuthController();
        setLayout(new GridBagLayout());
        setBackground(new Color(0, 0, 0, 210));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0;
        gbc.insets = new Insets(40, 40, 10, 40);
        JLabel logoLabel = new JLabel(ImageLoader.scaleImage("pictures/Logo.png", 200, true));
        add(logoLabel, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 40, 15, 40);
        JLabel welcomeText = new JLabel("Welcome back!", SwingConstants.CENTER);
        welcomeText.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeText.setForeground(Color.LIGHT_GRAY);
        add(welcomeText, gbc);

        gbc.gridy = 2;
        JTextField usernameField = new JTextField("Username");
        setupPlaceholder(usernameField, "Username");
        add(usernameField, gbc);

        gbc.gridy = 3;
        JPasswordField passwordField = new JPasswordField("Password");
        passwordField.setEchoChar((char) 0);
        setupPasswordPlaceholder(passwordField, "Password");
        add(passwordField, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(10, 40, 15, 40);
        JButton loginButton = new JButton("Log In");
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setPreferredSize(new Dimension(300, 50));
        loginButton.setBackground(new Color(50, 150, 250));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        loginButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = String.valueOf(passwordField.getPassword());

            if (user.isEmpty() || user.equals("Username") || pass.isEmpty() || pass.equals("Password")) {
                JOptionPane.showMessageDialog(this, "Please enter both a username and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (authController.authenticateUser(user, pass)) {
                JOptionPane.showMessageDialog(this, "Welcome back, " + user + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                // Transisi ke Dashboard
                DashboardFrame dashboard = new DashboardFrame(user);
                dashboard.setVisible(true);
                parentFrame.dispose();

            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        add(loginButton, gbc);

        gbc.gridy = 5; gbc.insets = new Insets(0, 40, 40, 40);
        JLabel signUpLabel = new JLabel("<html><u>Don't have an account? Sign up!</u></html>", SwingConstants.CENTER);
        signUpLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        signUpLabel.setForeground(new Color(100, 200, 255));
        signUpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { cardLayout.show(parentContainer, "SIGNUP"); }
            public void mouseEntered(MouseEvent e) { signUpLabel.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { signUpLabel.setForeground(new Color(100, 200, 255)); }
        });
        add(signUpLabel, gbc);
    }

    private void setupPlaceholder(JTextField field, String placeholder) {
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setForeground(Color.GRAY);
        field.setPreferredSize(new Dimension(300, 40));
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (field.getText().equals(placeholder)) { field.setText(""); field.setForeground(Color.BLACK); }
            }
            public void focusLost(FocusEvent evt) {
                if (field.getText().isEmpty()) { field.setForeground(Color.GRAY); field.setText(placeholder); }
            }
        });
    }

    private void setupPasswordPlaceholder(JPasswordField field, String placeholder) {
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setForeground(Color.GRAY);
        field.setPreferredSize(new Dimension(300, 40));
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText(""); field.setForeground(Color.BLACK); field.setEchoChar('\u2022');
                }
            }
            public void focusLost(FocusEvent evt) {
                if (String.valueOf(field.getPassword()).isEmpty()) {
                    field.setForeground(Color.GRAY); field.setText(placeholder); field.setEchoChar((char) 0);
                }
            }
        });
    }
}