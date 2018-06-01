package com.thedeliveryapp.thedeliveryapp.recyclerview;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thedeliveryapp.thedeliveryapp.R;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    CardView cv;
    LinearLayout swipeLayout;
    TextView category;
    TextView description;
    ImageView imageView;
    TextView undo;
    public boolean isClickable = true;
    
    OrderViewHolder(View itemView) {
        super(itemView);
        cv = itemView.findViewById(R.id.cardView);
        swipeLayout = itemView.findViewById(R.id.swipeLayout);
        undo = itemView.findViewById(R.id.undo);
        category = itemView.findViewById(R.id.category);
        description = itemView.findViewById(R.id.description);
        imageView = itemView.findViewById(R.id.imageView);
    }
}
