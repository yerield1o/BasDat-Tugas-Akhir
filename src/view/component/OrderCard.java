package view.component;

import controller.OrderController;
import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderCard extends JPanel {

    public OrderCard(String idPesananStr, String date, String status, String total, JFrame parentFrame, OrderController orderController) {
        int idPesanan = Integer.parseInt(idPesananStr);

        setLayout(new BorderLayout(15, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        setMaximumSize(new Dimension(800, 130));

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        infoPanel.setOpaque(false);

        JLabel dateLabel = new JLabel("Order Date: " + (date != null && date.length() >= 10 ? date.substring(0, 10) : "N/A"));
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dateLabel.setForeground(Color.DARK_GRAY);

        JLabel statusLabel = new JLabel("Status: " + status);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        double totalDouble = Double.parseDouble(total);
        NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        JLabel priceLabel = new JLabel("Total: " + rpFormat.format(totalDouble));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(new Color(0, 150, 0));

        infoPanel.add(dateLabel);
        infoPanel.add(statusLabel);
        infoPanel.add(priceLabel);
        add(infoPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        btnPanel.setOpaque(false);

        JButton trackBtn = new JButton("Check Delivery");
        trackBtn.setBackground(new Color(50, 150, 250));
        trackBtn.setForeground(Color.WHITE);
        trackBtn.setFocusPainted(false);
        trackBtn.setFont(new Font("Arial", Font.BOLD, 12));
        trackBtn.addActionListener(e -> showDeliveryPopUp(parentFrame, idPesanan, orderController));

        JButton billBtn = new JButton("View Bill Details");
        billBtn.setBackground(new Color(100, 100, 100));
        billBtn.setForeground(Color.WHITE);
        billBtn.setFocusPainted(false);
        billBtn.setFont(new Font("Arial", Font.BOLD, 12));
        billBtn.addActionListener(e -> showPaymentPopUp(parentFrame, idPesanan, orderController));

        btnPanel.add(trackBtn);
        btnPanel.add(billBtn);

        // NEW: Delete/Cancel Button if status is Pending
        if (status.equalsIgnoreCase("Pending")) {
            JButton cancelBtn = new JButton("Cancel Order");
            cancelBtn.setBackground(new Color(220, 50, 50));
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setFocusPainted(false);
            cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));

            cancelBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(parentFrame,
                        "Are you sure you want to completely delete this order?",
                        "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    if (orderController.deletePendingOrder(idPesanan)) {
                        JOptionPane.showMessageDialog(parentFrame, "Order has been successfully deleted.");
                        // This triggers a UI refresh on the parent panel
                        ((view.dashboard.PurchasedPanel) this.getParent().getParent().getParent()).refreshPurchasedData();
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, "Error deleting order.");
                    }
                }
            });
            btnPanel.add(cancelBtn);
        }

        add(btnPanel, BorderLayout.EAST);
    }

    private void showDeliveryPopUp(JFrame parentFrame, int idPesanan, OrderController orderController) {
        String[] deliveryData = orderController.getDeliveryDetails(idPesanan);

        if (deliveryData != null) {
            JDialog dialog = new JDialog(parentFrame, "Delivery Details (Order #" + idPesanan + ")", true);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(parentFrame);

            JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            double fee = Double.parseDouble(deliveryData[5]);

            panel.add(new JLabel("<html><b>Courier:</b> " + deliveryData[0] + "</html>"));
            panel.add(new JLabel("<html><b>Contact:</b> " + (deliveryData[1] != null ? deliveryData[1] : "N/A") + "</html>"));
            panel.add(new JLabel("<html><b>Date Sent:</b> " + (deliveryData[2] != null ? deliveryData[2].substring(0, 10) : "Pending") + "</html>"));
            panel.add(new JLabel("<html><b>Tracking Number:</b> " + (deliveryData[3] != null ? deliveryData[3] : "Awaiting Info") + "</html>"));
            panel.add(new JLabel("<html><b>Status:</b> " + (deliveryData[4] != null ? deliveryData[4] : "N/A") + "</html>"));
            panel.add(new JLabel("<html><b>Delivery Fee:</b> " + rpFormat.format(fee) + "</html>"));

            dialog.add(panel);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(parentFrame,
                    "Your order is currently being processed by our team.\nDelivery details will be available once the package is handed to the courier.",
                    "Tracking Unavailable",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showPaymentPopUp(JFrame parentFrame, int idPesanan, OrderController orderController) {
        JDialog dialog = new JDialog(parentFrame, "Order Receipt (#" + idPesanan + ")", true);
        dialog.setSize(400, 550);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new BorderLayout());

        JTextArea receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        receiptArea.setMargin(new Insets(15, 15, 15, 15));

        StringBuilder receiptText = new StringBuilder();
        receiptText.append("========================================\n");
        receiptText.append("          NIG CLOTHING RECEIPT          \n");
        receiptText.append("========================================\n\n");

        String[] headerData = orderController.getReceiptHeader(idPesanan);
        if (headerData != null) {
            String payDate = headerData[1];
            String displayDate = (payDate != null && payDate.length() >= 19) ? payDate.substring(0, 19) : payDate;
            receiptText.append("Receipt No : #").append(headerData[0]).append("\n");
            receiptText.append("Date       : ").append(displayDate).append("\n");
            receiptText.append("Method     : ").append(headerData[2]).append("\n");
            receiptText.append("Status     : ").append(headerData[3]).append("\n\n");
        }

        receiptText.append("----------------------------------------\n");
        receiptText.append("PURCHASED ITEMS:\n");
        receiptText.append("----------------------------------------\n");

        NumberFormat rpFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        double grandTotal = 0;
        List<String[]> items = orderController.getReceiptItems(idPesanan);

        for (String[] item : items) {
            String name = item[0];
            String variant = item[1] + " | " + item[2];
            int qty = Integer.parseInt(item[3]);
            double price = Double.parseDouble(item[4]);
            double subtotal = qty * price;
            grandTotal += subtotal;

            receiptText.append(name).append(" (").append(variant).append(")\n");
            receiptText.append(qty).append(" x ").append(rpFormat.format(price)).append("\n");
            receiptText.append("Subtotal: ").append(rpFormat.format(subtotal)).append("\n\n");
        }

        receiptText.append("----------------------------------------\n");
        receiptText.append("GRAND TOTAL: ").append(rpFormat.format(grandTotal)).append("\n");
        receiptText.append("========================================\n");
        receiptText.append("        Thank you for shopping!         \n");

        receiptArea.setText(receiptText.toString());
        dialog.add(new JScrollPane(receiptArea), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close Receipt");
        closeBtn.setBackground(new Color(220, 50, 50));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(closeBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}