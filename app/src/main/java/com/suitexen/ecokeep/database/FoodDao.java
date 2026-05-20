package com.suitexen.ecokeep.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.suitexen.ecokeep.models.ConsumptionLog;
import com.suitexen.ecokeep.models.FoodItem;

import java.util.List;

@Dao
public interface FoodDao {
    @Query("SELECT * FROM food_items WHERE status != 'Finished' ORDER BY createdAt DESC")
    List<FoodItem> getAllFoodItems();

    @Query("SELECT * FROM food_items WHERE id = :id")
    FoodItem getFoodItemById(int id);

    @Insert
    void insert(FoodItem foodItem);

    @Update
    void update(FoodItem foodItem);

    @Delete
    void delete(FoodItem foodItem);

    // Consumption Logs
    @Insert
    void insertLog(ConsumptionLog log);

    @Query("SELECT * FROM consumption_logs WHERE foodItemId = :foodItemId ORDER BY timestamp DESC")
    List<ConsumptionLog> getLogsByFoodId(int foodItemId);
}
