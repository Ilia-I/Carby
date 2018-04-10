package com.grouph.ces.carby.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.IntDef;

import com.grouph.ces.carby.nutrition_data.INutritionTable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Martin Peev on 18.02.2018 г..
 * Version: 0.6
 */
@Entity(tableName = "NutritionData")
@TypeConverters({NutritionTableConverter.class})
public class NutritionDataDB {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            SOURCE_OPEN_FOOD_FACTS,
            SOURCE_NUTRITION_TABLE_SCANNER,
            SOURCE_VOLUME_ESTIMATION
    })
    public @interface DataSource {}
    public static final int SOURCE_OPEN_FOOD_FACTS = 0;
    public static final int SOURCE_NUTRITION_TABLE_SCANNER = 1;
    public static final int SOURCE_VOLUME_ESTIMATION = 2;

    @PrimaryKey(autoGenerate = true)
    private int key;

    @ColumnInfo(name = "barcode")
    private String barcode;

    @ColumnInfo(name = "nutritionTable")
    private INutritionTable nt;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "source")
    private @DataSource int source;

    @Ignore
    public NutritionDataDB(String barcode, INutritionTable nt, @DataSource int source){
        this("", barcode, nt, source);
    }

    public NutritionDataDB(String name, String barcode, INutritionTable nt, @DataSource int source){
        this.name = name;
        this.barcode = barcode;
        this.nt = nt;
        this.source = source;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public static String sourceName(@DataSource int source){
        switch (source){
            case SOURCE_OPEN_FOOD_FACTS: return "Open Food Facts";
            case SOURCE_NUTRITION_TABLE_SCANNER: return "Nutrition Table Scanner";
            case SOURCE_VOLUME_ESTIMATION: return "Volume Estimation";
            default: return "";
        }
    }
}
