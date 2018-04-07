package com.grouph.ces.carby.history;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.database.AppDatabase;
import com.grouph.ces.carby.database.ConsumptionDB;
import com.grouph.ces.carby.database.ConsumptionDao;
import com.grouph.ces.carby.database.NutritionDataDB;
import com.grouph.ces.carby.database.NutritionDataDao;
import com.grouph.ces.carby.nutrition_data.INutritionTable;
import com.grouph.ces.carby.nutrition_data.NutritionResultActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryRvAdapter extends RecyclerView.Adapter<HistoryRvAdapter.HistoryViewHolder> {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mma", Locale.ENGLISH);

    private Context context;
    private List<ConsumptionDB> entries;
    private ConsumptionDao consumptionDao;
    private NutritionDataDao nutritionDataDao;

    public HistoryRvAdapter(AppDatabase db, Context context) {
        this.context = context;
        this.consumptionDao = db.consumptionDataDao();
        this.nutritionDataDao = db.nutritionDataDao();
        this.entries = consumptionDao.getAll();
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_card, parent, false);
        return new HistoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        holder.date.setText(dateFormat.format(entries.get(position).getTime()));
        holder.quantity.setText(String.format(Locale.ENGLISH, "%.1fg", entries.get(position).getQuantity()));
        holder.name.setText(NutritionDataDB.sourceName(nutritionDataDao.findByID(entries.get(position).getKey()).getSource()));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

        private TextView date;
        private TextView name;
        private TextView quantity;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            this.date = itemView.findViewById(R.id.history_date);
            this.name = itemView.findViewById(R.id.history_name);
            this.quantity = itemView.findViewById(R.id.history_quantity);
            this.itemView.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int position = getLayoutPosition();

            int key = entries.get(position).getKey();
            INutritionTable nt = nutritionDataDao.findByID(key).getNt();
            Intent result = new Intent(context, NutritionResultActivity.class);
            result.putExtra("jsonNutritionTable",nt.toJasonObject().toString());
            result.putExtra("id",-1);
            context.startActivity(result);
            return false;
        }
    }
}
