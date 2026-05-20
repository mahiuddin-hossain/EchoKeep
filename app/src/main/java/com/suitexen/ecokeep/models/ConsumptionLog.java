package com.suitexen.ecokeep.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "consumption_logs")
public class ConsumptionLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int foodItemId;
    private double quantityUsed;
    private String type; // Consumed, Wasted
    private long timestamp;

    public ConsumptionLog() {
    }

    public ConsumptionLog(int foodItemId, double quantityUsed, String type, long timestamp) {
        this.foodItemId = foodItemId;
        this.quantityUsed = quantityUsed;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFoodItemId() { return foodItemId; }
    public void setFoodItemId(int foodItemId) { this.foodItemId = foodItemId; }

    public double getQuantityUsed() { return quantityUsed; }
    public void setQuantityUsed(double quantityUsed) { this.quantityUsed = quantityUsed; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
