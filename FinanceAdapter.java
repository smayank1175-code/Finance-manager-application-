package com.example.financemanager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FinanceAdapter extends RecyclerView.Adapter<FinanceAdapter.MyViewHolder>{
    private Context context;
    private List<FinanceTable> financeTableList;
    private ClickEvent clickEvent;

    public FinanceAdapter(Context context, ClickEvent clickEvent) {
        this.context = context;
        financeTableList = new ArrayList<>();
        this.clickEvent=clickEvent;
    }

    public void add(FinanceTable financeTable){
        financeTableList.add(financeTable);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FinanceTable financeTable = financeTableList.get(position);


        holder.title.setText(financeTable.getTitle());
        holder.description.setText(financeTable.getDescription());

        // Amount AND Status
        holder.amount.setText("Rs " + financeTable.getAmount());
        if(financeTable.isIncome()){
            holder.status.setText("Income");
            holder.status.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.amount.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.status.setText("Expense");
            holder.status.setTextColor(Color.parseColor("#F44336")); // Red
            holder.amount.setTextColor(Color.parseColor("#F44336"));
        }

        // ✅ Date and Category
        String dateAndCategory = financeTable.getDate() + " | " + financeTable.getCategory();
        holder.date.setText(dateAndCategory);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickEvent.OnClick(position);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clickEvent.OnLongPress(position);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return financeTableList.size();
    }

    // --- Data Getters ---

    public int getId(int pos) {
        return financeTableList.get(pos).getId();
    }

    public boolean isIncome(int pos) {
        return financeTableList.get(pos).isIncome();
    }

    // ✅ Title new method
    public String title(int pos) {
        return financeTableList.get(pos).getTitle();
    }

    // ✅ Category new method
    public String category(int pos) {
        return financeTableList.get(pos).getCategory();
    }

    // ✅ Date new method
    public String date(int pos) {
        return financeTableList.get(pos).getDate();
    }

    public long amount(int pos) {
        return financeTableList.get(pos).getAmount();
    }

    public String desc(int pos) {
        return financeTableList.get(pos).getDescription();
    }

    public void delete(int pos){
        financeTableList.remove(pos);
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        // ✅ Date TextView
        TextView status, title, description, amount, date;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            status= itemView.findViewById(R.id.isIncome);
            title= itemView.findViewById(R.id.title);
            description= itemView.findViewById(R.id.description);
            amount= itemView.findViewById(R.id.amount);
            date = itemView.findViewById(R.id.date); // ✅ New ID
        }
    }
}