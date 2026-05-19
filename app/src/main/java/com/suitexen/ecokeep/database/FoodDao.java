package com.suitexen.ecokeep.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.suitexen.ecokeep.models.FoodItem;

import java.util.List;

@Dao
public interface FoodDao {
    @Query("SELECT * FROM food_items ORDER BY createdAt DESC")
    List<FoodItem> getAllFoodItems();

    @Insert
    void insert(FoodItem foodItem);

    @Update
    void update(FoodItem foodItem);

    @Delete
    void delete(FoodItem foodItem);
}
