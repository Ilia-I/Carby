package com.grouph.ces.carby.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.grouph.ces.carby.nutrition_data.INutritionTable;

/**
 * Created by Martin Peev on 18.02.2018 г..
 * Version: 0.2
 */
@Entity(tableName = "NutritionData")
public class NutritionDataDB {
    @PrimaryKey(autoGenerate = false)
    private int barcode;

    @ColumnInfo(name = "nutritionTable")
    private INutritionTable nt;

    public NutritionDataDB(int barcode, INutritionTable nt){
        this.barcode = barcode;
        this.nt = nt;
    }

    public int getBarcode() {
        return barcode;
    }

    public void setBarcode(int barcode) {
        this.barcode = barcode;
    }

    public INutritionTable getNt() {
        return nt;
    }

    public void setNt(INutritionTable nt) {
        this.nt = nt;
    }
}
