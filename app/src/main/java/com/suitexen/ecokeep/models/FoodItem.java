package com.suitexen.ecokeep.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_items")
public class FoodItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String category;
    private double totalQuantity;
    private double consumedQuantity;
    private double remainingQuantity;
    private String unit;
    private long expiryDate;
    private String imageUri;
    private String notes;
    private boolean reminderEnabled;
    private double price;
    private String status; // Active, Finished, Wasted
    private long createdAt;

    public FoodItem() {
    }

    public FoodItem(String name, String category, double totalQuantity, String unit, long expiryDate, String imageUri, String notes, boolean reminderEnabled, double price, long createdAt) {
        this.name = name;
        this.category = category;
        this.totalQuantity = totalQuantity;
        this.remainingQuantity = totalQuantity;
        this.consumedQuantity = 0;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.imageUri = imageUri;
        this.notes = notes;
        this.reminderEnabled = reminderEnabled;
        this.price = price;
        this.status = "Active";
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(double totalQuantity) { this.totalQuantity = totalQuantity; }

    public double getConsumedQuantity() { return consumedQuantity; }
    public void setConsumedQuantity(double consumedQuantity) { this.consumedQuantity = consumedQuantity; }

    public double getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(double remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
