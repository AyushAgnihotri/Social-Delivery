package com.thedeliveryapp.thedeliveryapp.recyclerview;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.thedeliveryapp.thedeliveryapp.R;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    CardView cv;
    TextView category;
    TextView description;
    ImageView imageView;
    OrderViewHolder(View itemView) {
        super(itemView);
        cv = itemView.findViewById(R.id.cardView);
        category = itemView.findViewById(R.id.category);
        description = itemView.findViewById(R.id.description);
        imageView = itemView.findViewById(R.id.imageView);
    }
}
