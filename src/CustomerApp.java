import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerApp extends JFrame {

    private CardLayout cardLayout = new CardLayout();
    private JPanel islandContainer = new JPanel(cardLayout);

    public CustomerApp() {
        // UPDATED: Generic Template Store Title
        setTitle("Customer App - NIG Clothing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        // ==========================================
        // THE OCEAN
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
        // THE ISLAND CONTAINER
        // ==========================================
        islandContainer.setOpaque(false);
        islandContainer.add(createLoginPanel(), "LOGIN");
        islandContainer.add(createSignUpPanel(), "SIGNUP");

        backgroundPanel.add(islandContainer);
        add(backgroundPanel);
    }

    // ==========================================
    // CARD 1: THE LOGIN PANEL
    // ==========================================
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0, 0, 0, 210));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font fieldFont = new Font("Arial", Font.PLAIN, 18);

        // 1. Logo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(40, 40, 10, 40);
        JLabel logoLabel = new JLabel(scaleImage("pictures/Logo.png", 200, true));
        panel.add(logoLabel, gbc);

        // 2. Welcome Text
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 15, 40);
        JLabel welcomeText = new JLabel("Welcome back!", SwingConstants.CENTER);
        welcomeText.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeText.setForeground(Color.LIGHT_GRAY);
        panel.add(welcomeText, gbc);

        // 3. Username Field
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

        // 4. Password Field
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

        // 5. Login Button
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 40, 15, 40);
        JButton loginButton = new JButton("Log In");
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setPreferredSize(new Dimension(300, 50));
        loginButton.setBackground(new Color(50, 150, 250));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> {
            String user = usernameField.getText();
            String pass = String.valueOf(passwordField.getPassword());

            // Prevent blank submissions or accidental placeholder submissions
            if (user.isEmpty() || user.equals("Username") ||
                    pass.isEmpty() || pass.equals("Password")) {
                JOptionPane.showMessageDialog(panel, "Please enter both a username and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Call the database method
            if (authenticateUser(user, pass)) {
                JOptionPane.showMessageDialog(panel, "Welcome back, " + user + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                CustomerDashboard dashboard = new CustomerDashboard(user);
                dashboard.setVisible(true);
                CustomerApp.this.dispose();

            } else {
                JOptionPane.showMessageDialog(panel, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(loginButton, gbc);

        // 6. Clickable Sign Up Text
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 40, 40, 40);
        JLabel signUpLabel = new JLabel("<html><u>Don't have an account? Sign up!</u></html>", SwingConstants.CENTER);
        signUpLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        signUpLabel.setForeground(new Color(100, 200, 255));
        signUpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        signUpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(islandContainer, "SIGNUP");
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                signUpLabel.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                signUpLabel.setForeground(new Color(100, 200, 255));
            }
        });
        panel.add(signUpLabel, gbc);

        return panel;
    }

    // ==========================================
    // CARD 2: THE SIGN UP PANEL
    // ==========================================
    private JPanel createSignUpPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0, 0, 0, 210));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font fieldFont = new Font("Arial", Font.PLAIN, 18);

        // 1. Title
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 40, 25, 40);
        JLabel titleLabel = new JLabel("SIGN UP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, gbc);

        // 2. New Username Field
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 15, 40);
        JTextField newUsernameField = new JTextField();
        newUsernameField.setFont(fieldFont);
        newUsernameField.setPreferredSize(new Dimension(300, 40));
        newUsernameField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                if (newUsernameField.getText().equals("Username")) {
                    newUsernameField.setText("");
                    newUsernameField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent evt) {
                if (newUsernameField.getText().isEmpty()) {
                    newUsernameField.setForeground(Color.GRAY);
                    newUsernameField.setText("Username");
                }
            }
        });
        panel.add(newUsernameField, gbc);

        // 3. New Password Field
        gbc.gridy = 2;
        JPasswordField newPasswordField = new JPasswordField();
        newPasswordField.setFont(fieldFont);
        newPasswordField.setPreferredSize(new Dimension(300, 40));
        newPasswordField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                String pass = String.valueOf(newPasswordField.getPassword());
                if (pass.equals("Password")) {
                    newPasswordField.setText("");
                    newPasswordField.setForeground(Color.BLACK);
                    newPasswordField.setEchoChar('\u2022');
                }
            }
            public void focusLost(FocusEvent evt) {
                String pass = String.valueOf(newPasswordField.getPassword());
                if (pass.isEmpty()) {
                    newPasswordField.setForeground(Color.GRAY);
                    newPasswordField.setText("Password");
                    newPasswordField.setEchoChar((char) 0);
                }
            }
        });
        panel.add(newPasswordField, gbc);

        // 4. Register Button
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 40, 15, 40);
        JButton registerButton = new JButton("Create Account");
        registerButton.setFont(new Font("Arial", Font.BOLD, 20));
        registerButton.setPreferredSize(new Dimension(300, 50));
        registerButton.setBackground(new Color(50, 200, 100));
        registerButton.setForeground(Color.WHITE);

        registerButton.addActionListener(e -> {
            String user = newUsernameField.getText();
            String pass = String.valueOf(newPasswordField.getPassword());

            if (registerNewUser(user, pass)) {
                JOptionPane.showMessageDialog(this, "Account created! You can now log in.");
                cardLayout.show(islandContainer, "LOGIN");
            }
        });
        panel.add(registerButton, gbc);

        // 5. Back to Login Link
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 40, 40, 40);
        JLabel backLabel = new JLabel("<html><u>Already have an account? Log in.</u></html>", SwingConstants.CENTER);
        backLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        backLabel.setForeground(new Color(100, 200, 255));
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(islandContainer, "LOGIN");
            }
        });
        panel.add(backLabel, gbc);

        return panel;
    }

    // ==========================================
    // FIXED: DATABASE REGISTRATION LOGIC
    // ==========================================
    private boolean registerNewUser(String username, String rawPassword) {
        if (username.isEmpty() || rawPassword.isEmpty()) return false;

        String dbURL = "jdbc:sqlserver://localhost:1433;databaseName=NIG_Clothing;encrypt=true;trustServerCertificate=true;";
        String dbUser = "sa";
        String dbPass = "password";

        // FIX 1: Insert into Pelanggan.
        // FIX 2: Add placeholder values for the NOT NULL columns!
        String sqlQuery = "INSERT INTO Pelanggan (Username, Password, nama_pelanggan, alamat_lengkap) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            pstmt.setString(1, username);
            pstmt.setString(2, rawPassword);
            pstmt.setString(3, "New Customer"); // Placeholder Name
            pstmt.setString(4, "Please update your address in Account Settings"); // Placeholder Address

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: Username might already exist.");
            return false;
        }
    }

    // Helper method to load and resize an image
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

    private boolean authenticateUser(String enteredUsername, String enteredPassword) {
        // 1. Update the connection URL specifically for the NIG_Clothing database
        String dbURL = "jdbc:sqlserver://localhost:1433;databaseName=NIG_Clothing;encrypt=true;trustServerCertificate=true;";

        // Note: Update these with your actual SSMS login credentials if they are different
        String dbUser = "sa";
        String dbPass = "password";

        // 2. The SQL Query matches your exact table name (Pelanggan) and column names
        String sqlQuery = "SELECT * FROM Pelanggan WHERE Username = ? AND Password = ?";

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            // 3. Bind the user input to the query to prevent SQL injection
            pstmt.setString(1, enteredUsername);
            pstmt.setString(2, enteredPassword);

            // 4. Execute the query
            ResultSet rs = pstmt.executeQuery();

            // 5. If a match is found (rs.next() is true), the login is successful
            return rs.next();

        } catch (Exception e) {
            System.out.println("Database connection error!");
            e.printStackTrace();
            return false;
        }
    }
}