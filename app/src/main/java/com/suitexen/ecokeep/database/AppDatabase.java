package com.suitexen.ecokeep.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.suitexen.ecokeep.models.ConsumptionLog;
import com.suitexen.ecokeep.models.FoodItem;

@Database(entities = {FoodItem.class, ConsumptionLog.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract FoodDao foodDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "ecokeep_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // For simplicity in this demo, use main thread. In production, use background threads.
                    .build();
        }
        return instance;
    }
}
