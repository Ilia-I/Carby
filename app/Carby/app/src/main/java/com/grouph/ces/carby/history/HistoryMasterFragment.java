package com.grouph.ces.carby.history;

import android.app.Fragment;
import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.database.AppDatabase;
import com.grouph.ces.carby.database.ConsumptionDB;
import com.grouph.ces.carby.database.ConsumptionDao;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryMasterFragment extends Fragment {

    private HistoryActivity activity;
    private AppDatabase db;
    private ConsumptionDao consumptionDao;

    private MaterialCalendarView cv;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.history_master_layout, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        cv.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);
        this.activity.getSupportActionBar().setTitle("History");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = (HistoryActivity) getActivity();
        this.activity.getSupportActionBar().setTitle("History");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        db = Room.databaseBuilder(activity ,AppDatabase.class,"myDB").allowMainThreadQueries().build();
        consumptionDao = db.consumptionDataDao();
        List<ConsumptionDB> list = consumptionDao.getAll();
        setCalendar(list);
    }

    private void setCalendar(List<ConsumptionDB> entries) {
        cv = activity.findViewById(R.id.calendarView);

        if(entries.isEmpty())
            cv.state().edit()
                    .setMaximumDate(cv.getCurrentDate())
                    .setMinimumDate(cv.getCurrentDate())
                    .commit();
        else
            cv.state().edit()
                    .setMaximumDate(new Date())
                    .setMinimumDate(entries.get(entries.size()-1).getTime())
                    .commit();

        cv.removeDecorators();
        for(ConsumptionDB entry : entries)
            cv.addDecorator(new DayDecorator(entry.getTime()));

        cv.setOnDateChangedListener((widget, date, selected) -> {
            Bundle b = new Bundle();
            b.putString("date", date.getDate().toString());
            activity.setFragmentHistoryDetail(b);
            cv.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);
        });
    }

}
