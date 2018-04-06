package com.grouph.ces.carby;

import android.app.Activity;
import android.app.Fragment;
import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.grouph.ces.carby.database.AppDatabase;

public class HistoryFragment extends Fragment {

    private HistoryActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.history_layout, container, false);
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

        AppDatabase db = Room.databaseBuilder(activity ,AppDatabase.class,"myDB").allowMainThreadQueries().build();
        LinearLayoutManager lm = new LinearLayoutManager(activity);
        RecyclerView recyclerView = activity.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(new HistoryRvAdapter(db));
    }
}
