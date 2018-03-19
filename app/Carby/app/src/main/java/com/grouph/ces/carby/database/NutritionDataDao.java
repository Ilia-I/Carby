package com.grouph.ces.carby.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;

import com.grouph.ces.carby.nutrition_data.INutritionTable;

import java.util.List;

/**
 * Created by Martin Peev on 18.02.2018 г..
 * Version: 0.4
 */
@Dao
@TypeConverters({NutritionTableConverter.class})
public interface NutritionDataDao {
    @Query("SELECT * FROM NutritionData")
    List<NutritionDataDB> getAll();

    @Query("SELECT * FROM NutritionData WHERE barcode = :barcode LIMIT 1")
    NutritionDataDB findByBarcode(String barcode);

    @Query("UPDATE NutritionData SET nutritionTable = :nt WHERE barcode = :barcode")
    void update(String barcode, INutritionTable nt);

    @Insert
    void insertAll(NutritionDataDB... goals);

    @Delete
    void delete(NutritionDataDB goal);

    @Query("DELETE FROM NutritionData")
    void nukeTable();
}
