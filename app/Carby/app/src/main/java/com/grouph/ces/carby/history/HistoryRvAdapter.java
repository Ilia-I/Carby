package com.grouph.ces.carby.history;

import android.arch.persistence.room.Room;
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
import com.grouph.ces.carby.database.NutritionDataDB;
import com.grouph.ces.carby.nutrition_data.INutritionTable;
import com.grouph.ces.carby.nutrition_data.NutritionResultActivity;
import com.grouph.ces.carby.nutrition_data.NutritionTable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryRvAdapter extends RecyclerView.Adapter<HistoryRvAdapter.HistoryViewHolder> {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mma", Locale.ENGLISH);

    private Context context;
    private List<ConsumptionDB> entries;
    private AppDatabase db;

    public HistoryRvAdapter(Context context, Date currentDate) {
        this.context = context;
        this.db = Room.databaseBuilder(context, AppDatabase.class,"myDB").allowMainThreadQueries().build();
        this.entries = getEntriesForDate(db.consumptionDataDao().getAll(), currentDate);
    }


    private List<ConsumptionDB> getEntriesForDate(List<ConsumptionDB> all, Date date) {
        List<ConsumptionDB> entriesForDate = new ArrayList<>();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        for(ConsumptionDB entry : all) {
            cal1.setTime(entry.getTime());
            cal2.setTime(date);
            boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
            if (sameDay)
                entriesForDate.add(entry);
        }
        return entriesForDate;
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
        holder.name.setText(String.format(Locale.ENGLISH, "Source: %s", NutritionDataDB.sourceName(db.nutritionDataDao().findByID(entries.get(position).getRef()).getSource())));
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
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_UP:
                    int ref = entries.get(position).getRef();
                    INutritionTable nt = db.nutritionDataDao().findByID(ref).getNt();
                    Intent result = new Intent(context, NutritionResultActivity.class);
                    result.putExtra("jsonNutritionTable",calculateValues(nt,entries.get(position).getQuantity()).toJasonObject().toString());
                    result.putExtra("id",-1);
                    result.putExtra("per100g",false);
                    result.putExtra("mass",entries.get(position).getQuantity());
                    context.startActivity(result);
                    return false;
                default: return false;
            }
        }

        private INutritionTable calculateValues(INutritionTable nt, double quantity) {
            INutritionTable result = new NutritionTable();
            for(String component: nt.listOfContents()){
                Double val = nt.getComponentValue(component);
                if(val!=null){
                    result.setComponent(component,val*quantity/100);
                }
            }
            return result;
        }
    }
}
