package com.grouph.ces.carby.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

/**
 * Created by Martin Peev on 19.03.2018 г..
 * Version: 0.1
 */
@Dao
@TypeConverters({DateConverter.class})
public interface ConsumptionDao {
    @Insert
    void insert(ConsumptionDB repo);

    @Update
    void update(ConsumptionDB... repos);

    @Delete
    void delete(ConsumptionDB... repos);

    @Query("SELECT * FROM ConsumptionData")
    List<ConsumptionDB> getAll();

    @Query("SELECT * FROM ConsumptionData WHERE time>=:time")
    List<ConsumptionDB> getAllAfter(Date time);

    @Query("DELETE FROM ConsumptionData")
    void nukeTable();
}
