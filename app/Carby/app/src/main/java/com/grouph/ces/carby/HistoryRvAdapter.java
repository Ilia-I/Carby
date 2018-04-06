package com.grouph.ces.carby;

import android.arch.persistence.room.Room;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grouph.ces.carby.database.AppDatabase;
import com.grouph.ces.carby.database.ConsumptionDB;
import com.grouph.ces.carby.database.ConsumptionDao;

import java.util.List;
import java.util.Locale;

public class HistoryRvAdapter extends RecyclerView.Adapter<HistoryRvAdapter.HistoryViewHolder> {

    private List<ConsumptionDB> entries;
    private ConsumptionDao consumptionDao;

    public HistoryRvAdapter(AppDatabase db) {
        consumptionDao = db.consumptionDataDao();
        entries = consumptionDao.getAll();
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_card, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        holder.date.setText(entries.get(position).getTime().toString());
        holder.quantity.setText(String.format(Locale.ENGLISH, "%f", entries.get(position).getQuantity()));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {

        private TextView date;
        private TextView name;
        private TextView quantity;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            this.date = itemView.findViewById(R.id.history_date);
            this.name = itemView.findViewById(R.id.history_name);
            this.quantity = itemView.findViewById(R.id.history_quantity);
        }
    }
}
