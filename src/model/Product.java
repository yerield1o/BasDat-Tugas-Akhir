package model;

public class Product {
    private int id;
    private String name;
    private double price;
    private String imageName;
    private String brandName;

    public Product(int id, String name, double price, String imageName, String brandName) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageName = imageName;
        this.brandName = brandName;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImageName() { return imageName; }
    public String getBrandName() { return brandName; }
}