package view.auth;

import controller.AuthController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SignUpPanel extends JPanel {
    private AuthController authController;

    public SignUpPanel(CardLayout cardLayout, JPanel parentContainer) {
        this.authController = new AuthController();

        setLayout(new GridBagLayout());
        setBackground(new Color(0, 0, 0, 210));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font fieldFont = new Font("Arial", Font.PLAIN, 18);

        // Title
        gbc.gridy = 0; gbc.insets = new Insets(40, 40, 25, 40);
        JLabel titleLabel = new JLabel("SIGN UP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, gbc);

        // Username Field
        gbc.gridy = 1; gbc.insets = new Insets(0, 40, 15, 40);
        JTextField newUsernameField = new JTextField();
        newUsernameField.setFont(fieldFont);
        newUsernameField.setPreferredSize(new Dimension(300, 40));
        add(newUsernameField, gbc);

        // Password Field
        gbc.gridy = 2;
        JPasswordField newPasswordField = new JPasswordField();
        newPasswordField.setFont(fieldFont);
        newPasswordField.setPreferredSize(new Dimension(300, 40));
        add(newPasswordField, gbc);

        // Register Button
        gbc.gridy = 3; gbc.insets = new Insets(10, 40, 15, 40);
        JButton registerButton = new JButton("Create Account");
        registerButton.setFont(new Font("Arial", Font.BOLD, 20));
        registerButton.setPreferredSize(new Dimension(300, 50));
        registerButton.setBackground(new Color(50, 200, 100));
        registerButton.setForeground(Color.WHITE);

        registerButton.addActionListener(e -> {
            String user = newUsernameField.getText();
            String pass = String.valueOf(newPasswordField.getPassword());

            // Memanggil Controller (Tidak ada SQL di sini)
            if (authController.registerNewUser(user, pass)) {
                JOptionPane.showMessageDialog(this, "Account created! You can now log in.");
                cardLayout.show(parentContainer, "LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. Username might already exist or fields are empty.");
            }
        });
        add(registerButton, gbc);

        // Back to Login
        gbc.gridy = 4; gbc.insets = new Insets(0, 40, 40, 40);
        JLabel backLabel = new JLabel("<html><u>Already have an account? Log in.</u></html>", SwingConstants.CENTER);
        backLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        backLabel.setForeground(new Color(100, 200, 255));
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { cardLayout.show(parentContainer, "LOGIN"); }
        });
        add(backLabel, gbc);
    }
}