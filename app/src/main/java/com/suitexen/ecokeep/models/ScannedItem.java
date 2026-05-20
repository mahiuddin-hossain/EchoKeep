package com.suitexen.ecokeep.models;

public class ScannedItem {
    private String name;
    private String category;
    private double totalQuantity;
    private String unit;
    private double price;
    private String emoji;

    public ScannedItem() {}

    public ScannedItem(String name, String category, double totalQuantity, String unit, double price, String emoji) {
        this.name = name;
        this.category = category;
        this.totalQuantity = totalQuantity;
        this.unit = unit;
        this.price = price;
        this.emoji = emoji;
    }

    // Getters
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getTotalQuantity() { return totalQuantity; }
    public String getUnit() { return unit; }
    public double getPrice() { return price; }
    public String getEmoji() { return emoji; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setTotalQuantity(double totalQuantity) { this.totalQuantity = totalQuantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setPrice(double price) { this.price = price; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    // Display helper
    public String getQuantity() {
        if (totalQuantity == (long) totalQuantity) {
            return String.format("%d %s", (long) totalQuantity, unit);
        }
        return String.format("%.1f %s", totalQuantity, unit);
    }
}