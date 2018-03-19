package com.grouph.ces.carby.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import com.grouph.ces.carby.nutrition_data.INutritionTable;

import java.util.Calendar;
import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by Martin Peev on 19.03.2018 г..
 * Version: 0.1
 */
@Entity(tableName = "ConsumptionData",
        foreignKeys = @ForeignKey(entity = NutritionDataDB.class,
        parentColumns = "key",
        childColumns = "ref",
        onUpdate = CASCADE,
        onDelete = CASCADE))
@TypeConverters({DateConverter.class})
public class ConsumptionDB {
    @PrimaryKey(autoGenerate = true)
    private int key;

    @ColumnInfo(name = "time")
    private Date time;

    @ColumnInfo(name = "ref")
    private int ref;

    @ColumnInfo(name = "quantity")
    private int quantity;

    public ConsumptionDB(int ref, int quantity){
        this(Calendar.getInstance().getTime(),ref,quantity);
    }

    public ConsumptionDB(Date time, int ref, int quantity){
        this.time=time;
        this.ref=ref;
        this.quantity=quantity;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getRef() {
        return ref;
    }

    public void setRef(int ref) {
        this.ref = ref;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
