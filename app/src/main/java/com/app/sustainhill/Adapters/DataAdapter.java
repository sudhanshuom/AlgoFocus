package com.app.sustainhill.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sustainhill.Profile;
import com.app.sustainhill.R;
import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private Context context;
    public ArrayList<String> name;
    public ArrayList<String> amount;
    public ArrayList<String> date;

    public DataAdapter(Context cart, ArrayList name, ArrayList amount, ArrayList<String> date) {
        this.context = cart;
        this.name = name;
        this.amount = amount;
        this.date = date;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_for_data, parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.dname.setText(name.get(position));
        holder.damount.setText(amount.get(position));
        holder.ddate.setText(date.get(position));

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile.editData(position);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return name.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dname, damount, ddate;
        ImageView edit;
        ViewHolder(View view) {
            super(view);
            dname = view.findViewById(R.id.dname);
            damount = view.findViewById(R.id.damount);
            ddate = view.findViewById(R.id.ddate);
            edit = view.findViewById(R.id.edit);
        }
    }
}