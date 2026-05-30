package model;

public class Variant {
    private int id;
    private String size;
    private String color;
    private int stock;

    public Variant(int id, String size, String color, int stock) {
        this.id = id;
        this.size = size;
        this.color = color;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public int getStock() { return stock; }

    @Override
    public String toString() {
        return size + " | " + color + " (Stock: " + stock + ")";
    }
}