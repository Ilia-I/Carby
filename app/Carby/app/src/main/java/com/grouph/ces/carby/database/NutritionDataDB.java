package com.grouph.ces.carby.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.grouph.ces.carby.nutrition_data.INutritionTable;

/**
 * Created by Martin Peev on 18.02.2018 г..
 * Version: 0.4
 */
@Entity(tableName = "NutritionData")
@TypeConverters({NutritionTableConverter.class})
public class NutritionDataDB {
    @PrimaryKey(autoGenerate = true)
    private int key;

    @ColumnInfo(name = "barcode")
    private String barcode;

    @ColumnInfo(name = "nutritionTable")
    private INutritionTable nt;

    public NutritionDataDB(String barcode, INutritionTable nt){
        this.barcode = barcode;
        this.nt = nt;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public INutritionTable getNt() {
        return nt;
    }

    public void setNt(INutritionTable nt) {
        this.nt = nt;
    }
}
