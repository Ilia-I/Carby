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
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryDetailFragment extends Fragment {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);

    private HistoryActivity activity;
    private AppDatabase db;
    private ConsumptionDao consumptionDao;
    private Date currentDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.history_detail_layout, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.activity = (HistoryActivity) this.getActivity();
        this.db = Room.databaseBuilder(activity ,AppDatabase.class,"myDB").allowMainThreadQueries().build();
        this.consumptionDao = db.consumptionDataDao();

        Bundle bundle = getArguments();
        if (bundle != null) {
            try {
                currentDate = sdf.parse(bundle.getString("date"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            this.activity.getSupportActionBar()
                    .setTitle(String.format(Locale.ENGLISH
                            , "%s/%d/%d"
                            , cal.get(Calendar.DAY_OF_MONTH)
                            , cal.get(Calendar.MONTH)
                            , cal.get(Calendar.YEAR)));
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        RecyclerView recyclerView = activity.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new HistoryRvAdapter(db, getEntriesForDate(), activity));
    }

    private List<ConsumptionDB> getEntriesForDate() {
        List<ConsumptionDB> entries = consumptionDao.getAll();
        List<ConsumptionDB> entriesForDate = new ArrayList<>();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        for(ConsumptionDB entry : entries) {
            cal1.setTime(entry.getTime());
            cal2.setTime(currentDate);
            boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
            if (sameDay)
                entriesForDate.add(entry);
        }
        return entriesForDate;
    }
}
