package view.dashboard;

import model.CartItem;
import util.ImageLoader;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardFrame extends JFrame {
    private String loggedInUser;
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContentPanel = new JPanel(cardLayout);
    private List<CartItem> floatingCart = new ArrayList<>();

    private StorePanel storePanel;
    private AccountPanel accountPanel;
    private CartPanel cartPanel;
    private PurchasedPanel purchasedPanel;

    public DashboardFrame(String username) {
        this.loggedInUser = username;
        setTitle("NIG Clothing - Dashboard (" + username + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        storePanel = new StorePanel(this);
        accountPanel = new AccountPanel(this);
        cartPanel = new CartPanel(this);
        purchasedPanel = new PurchasedPanel(this);

        mainContentPanel.add(storePanel, "STORE");
        mainContentPanel.add(cartPanel, "CART");
        mainContentPanel.add(purchasedPanel, "PURCHASED");
        mainContentPanel.add(accountPanel, "ACCOUNT");

        add(createNavBar(), BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        cardLayout.show(mainContentPanel, "STORE");
    }

    public String getLoggedInUser() { return loggedInUser; }
    public List<CartItem> getFloatingCart() { return floatingCart; }
    public void showStore() {
        storePanel.refreshStoreData();
        cardLayout.show(mainContentPanel, "STORE");
    }
    private JPanel createNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(30, 30, 30));
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel logoLabel = new JLabel(ImageLoader.scaleImage("pictures/Logo.png", 50, true));
        logoLabel.setText("  NIG Clothing");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        navBar.add(logoLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        String[] tabs = {"Store", "Cart", "Purchased", "Account"};
        String[] cardNames = {"STORE", "CART", "PURCHASED", "ACCOUNT"};

        for (int i = 0; i < tabs.length; i++) {
            JButton tabButton = new JButton(tabs[i]);
            tabButton.setFont(new Font("Arial", Font.BOLD, 16));
            tabButton.setForeground(Color.WHITE);
            tabButton.setBackground(new Color(50, 50, 50));
            tabButton.setFocusPainted(false);

            final String targetCard = cardNames[i];
            tabButton.addActionListener(e -> {
                // Refresh data sebelum halaman ditampilkan
                if (targetCard.equals("CART")) {
                    cartPanel.refreshCartData();
                } else if (targetCard.equals("PURCHASED")) {
                    purchasedPanel.refreshPurchasedData();
                }
                cardLayout.show(mainContentPanel, targetCard);
            });
            buttonPanel.add(tabButton);
        }
        navBar.add(buttonPanel, BorderLayout.EAST);
        return navBar;
    }
}