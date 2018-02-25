package com.grouph.ces.carby.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Martin Peev on 18.02.2018 г..
 * Version: 0.1
 */

@Database(entities = {NutritionDataDB.class}, version=1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NutritionDataDao nutritionDataDao();
}
