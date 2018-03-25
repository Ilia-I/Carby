package com.grouph.ces.carby.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by Martin Peev on 19.03.2018 г..
 * Version: 0.1
 */
public class DateConverter {
    @TypeConverter
    public static Long fromDate(Date date){
        if(date == null) return null;
        return date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long millisSinceEpoch){
        if(millisSinceEpoch == null) return null;
        return new Date(millisSinceEpoch);
    }
}
