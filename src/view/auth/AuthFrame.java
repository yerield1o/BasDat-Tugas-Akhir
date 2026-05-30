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

        // --- Icon Aplikasi (Tetap Dipertahankan) ---
        try {
            ImageIcon appIcon = new ImageIcon("pictures/img.png");
            setIconImage(appIcon.getImage());
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon aplikasi: " + e.getMessage());
        }
        // -------------------------------------------

        // --- Perbaikan Background: Rata Kiri-Atas (No Stretch, Anchored Top-Left) ---
        JPanel backgroundPanel = new JPanel(new GridBagLayout()) {
            private final Image backgroundImage = new ImageIcon("pictures/loginbackground.jpg").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    // Aktifkan rendering kualitas tinggi
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imgWidth = backgroundImage.getWidth(this);
                    int imgHeight = backgroundImage.getHeight(this);

                    // Validasi ukuran gambar
                    if (imgWidth <= 0 || imgHeight <= 0) return;

                    double panelRatio = (double) panelWidth / (double) panelHeight;
                    double imgRatio = (double) imgWidth / (double) imgHeight;

                    int drawWidth, drawHeight;

                    // Logika kalkulasi kalkulasi ukuran (tetap Cover agar layar penuh)
                    if (panelRatio > imgRatio) {
                        // Potong vertikal (panel lebih lebar proporsinya dibanding gambar)
                        drawWidth = panelWidth;
                        drawHeight = (int) (panelWidth / imgRatio); // Zoom vertikal agar lebar penuh
                    } else {
                        // Potong horizontal (panel lebih tinggi proporsinya dibanding gambar)
                        drawHeight = panelHeight;
                        drawWidth = (int) (panelHeight * imgRatio); // Zoom horizontal agar tinggi penuh
                    }

                    // --- PERBAIKAN DI SINI: ACUAN KIRI-ATAS ---
                    // Posisikan gambar mentok di pojok kiri (x=0) dan pojok atas (y=0).
                    // Bagian gambar yang kelebihan akan terpotong di kanan atau bawah.
                    int x = 0;
                    int y = 0;
                    // ------------------------------------------

                    // Gambar hasil kalkulasi
                    g2d.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
                }
            }
        };
        // ----------------------------------------------------------------------------

        islandContainer.setOpaque(false);
        islandContainer.add(new LoginPanel(this, cardLayout, islandContainer), "LOGIN");
        islandContainer.add(new SignUpPanel(cardLayout, islandContainer), "SIGNUP");
        backgroundPanel.add(islandContainer);
        add(backgroundPanel);
    }
}