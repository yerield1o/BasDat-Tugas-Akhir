package view.dashboard;

import controller.UserController;
import javax.swing.*;
import java.awt.*;

public class AccountPanel extends JPanel {
    private DashboardFrame parentFrame;
    private UserController userController;

    private JTextField nameField, usernameField, phoneField;
    private JPasswordField passwordField;
    private JTextArea addressArea;

    public AccountPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.userController = new UserController();

        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 245));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Account Settings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        formPanel.add(titleLabel, gbc);

        // Setup Fields
        nameField = new JTextField(20);
        usernameField = new JTextField(20);
        usernameField.setEditable(false);
        usernameField.setBackground(new Color(230, 230, 230));
        passwordField = new JPasswordField(20);
        phoneField = new JTextField(20);
        addressArea = new JTextArea(4, 20);
        addressArea.setLineWrap(true);
        addressArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        Font inputFont = new Font("Arial", Font.PLAIN, 14);
        nameField.setFont(inputFont); usernameField.setFont(inputFont);
        passwordField.setFont(inputFont); phoneField.setFont(inputFont);
        addressArea.setFont(inputFont);

        // Add to Form
        gbc.gridwidth = 1; gbc.gridy++;
        gbc.gridx = 0; formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; formPanel.add(nameField, gbc);

        gbc.gridy++;
        gbc.gridx = 0; formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; formPanel.add(usernameField, gbc);

        gbc.gridy++;
        gbc.gridx = 0; formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; formPanel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.gridx = 0; formPanel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1; formPanel.add(phoneField, gbc);

        gbc.gridy++;
        gbc.gridx = 0; formPanel.add(new JLabel("Full Address:"), gbc);
        gbc.gridx = 1; formPanel.add(new JScrollPane(addressArea), gbc);

        // Load Data menggunakan Controller
        loadCurrentData();

        // Save Button
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(50, 200, 100));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Arial", Font.BOLD, 16));
        saveBtn.setFocusPainted(false);

        saveBtn.addActionListener(e -> {
            boolean success = userController.updateUserProfile(
                    parentFrame.getLoggedInUser(),
                    nameField.getText(),
                    new String(passwordField.getPassword()),
                    phoneField.getText(),
                    addressArea.getText()
            );

            if (success) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error updating profile.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formPanel.add(saveBtn, gbc);
        add(formPanel);
    }

    private void loadCurrentData() {
        String[] data = userController.getUserData(parentFrame.getLoggedInUser());
        if (data != null) {
            nameField.setText(data[0]);
            usernameField.setText(data[1]);
            passwordField.setText(data[2]);
            phoneField.setText(data[3]);
            addressArea.setText(data[4]);
        }
    }
}