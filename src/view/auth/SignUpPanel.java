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

        // Title
        gbc.gridy = 0; gbc.insets = new Insets(40, 40, 25, 40);
        JLabel titleLabel = new JLabel("SIGN UP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, gbc);

        // Username Field dengan Placeholder
        gbc.gridy = 1; gbc.insets = new Insets(0, 40, 15, 40);
        JTextField newUsernameField = new JTextField("Username");
        setupPlaceholder(newUsernameField, "Username");
        add(newUsernameField, gbc);

        // Password Field dengan Placeholder
        gbc.gridy = 2;
        JPasswordField newPasswordField = new JPasswordField("Password");
        newPasswordField.setEchoChar((char) 0); // Menampilkan teks agar tidak jadi titik-titik di awal
        setupPasswordPlaceholder(newPasswordField, "Password");
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

            // Mencegah placeholder atau field kosong didaftarkan
            if (user.isEmpty() || user.equals("Username") || pass.isEmpty() || pass.equals("Password")) {
                JOptionPane.showMessageDialog(this, "Please enter both a username and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (authController.registerNewUser(user, pass)) {
                JOptionPane.showMessageDialog(this, "Account created! You can now log in.");
                // Kembalikan form ke kondisi awal setelah sukses
                newUsernameField.setText("Username");
                newUsernameField.setForeground(Color.GRAY);
                newPasswordField.setText("Password");
                newPasswordField.setForeground(Color.GRAY);
                newPasswordField.setEchoChar((char) 0);

                cardLayout.show(parentContainer, "LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. Username might already exist.");
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
            public void mouseEntered(MouseEvent e) { backLabel.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e) { backLabel.setForeground(new Color(100, 200, 255)); }
        });
        add(backLabel, gbc);
    }

    // Fungsi pembantu untuk mengatur Placeholder Username
    private void setupPlaceholder(JTextField field, String placeholder) {
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setForeground(Color.GRAY);
        field.setPreferredSize(new Dimension(300, 40));
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }

    // Fungsi pembantu untuk mengatur Placeholder Password
    private void setupPasswordPlaceholder(JPasswordField field, String placeholder) {
        field.setFont(new Font("Arial", Font.PLAIN, 18));
        field.setForeground(Color.GRAY);
        field.setPreferredSize(new Dimension(300, 40));
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setEchoChar('\u2022'); // Mengubah teks menjadi titik-titik (sensor)
                }
            }
            public void focusLost(FocusEvent evt) {
                if (String.valueOf(field.getPassword()).isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                    field.setEchoChar((char) 0); // Menampilkan teks placeholder
                }
            }
        });
    }
}