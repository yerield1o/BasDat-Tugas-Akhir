package model;

public class CartItem {
    private int variantId;
    private String productName;
    private String variantInfo;
    private double price;
    private int quantity;
    private int maxStock;

    public CartItem(int variantId, String productName, String variantInfo, double price, int quantity, int maxStock) {
        this.variantId = variantId;
        this.productName = productName;
        this.variantInfo = variantInfo;
        this.price = price;
        this.quantity = quantity;
        this.maxStock = maxStock;
    }

    public int getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public String getVariantInfo() { return variantInfo; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getMaxStock() { return maxStock; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}