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
 * Version: 0.5
 */
@Dao
@TypeConverters({NutritionTableConverter.class})
public interface NutritionDataDao {
    @Query("SELECT * FROM NutritionData")
    List<NutritionDataDB> getAll();

    @Query("SELECT * FROM NutritionData WHERE source = :source")
    List<NutritionDataDB> getAllFromSource(@NutritionDataDB.DataSource int source);

    @Query("SELECT * FROM NutritionData WHERE barcode = :barcode LIMIT 1")
    NutritionDataDB findByBarcode(String barcode);

    @Query("SELECT * FROM NutritionData WHERE name = :name LIMIT 1")
    NutritionDataDB findByName(String name);

    @Query("SELECT * FROM NutritionData WHERE key = :key LIMIT 1")
    NutritionDataDB findByID(int key);

    @Query("SELECT * FROM NutritionData WHERE name = :name AND barcode = :barcode AND nutritionTable = :nt LIMIT 1")
    NutritionDataDB findByData(String name, String barcode, INutritionTable nt);

    @Query("UPDATE NutritionData SET nutritionTable = :nt WHERE barcode = :barcode")
    void update(String barcode, INutritionTable nt);

    @Insert
    void insertAll(NutritionDataDB... goals);

    @Delete
    void delete(NutritionDataDB goal);

    @Query("DELETE FROM NutritionData")
    void nukeTable();
}
