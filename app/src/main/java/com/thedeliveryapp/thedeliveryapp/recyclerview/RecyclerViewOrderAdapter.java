package com.thedeliveryapp.thedeliveryapp.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.order_form.OrderForm;
import com.thedeliveryapp.thedeliveryapp.user.ItemListActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

import java.util.List;

public class RecyclerViewOrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

    List<OrderData> list;
    Context context;

    public RecyclerViewOrderAdapter(List<OrderData> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_content, parent, false);
        OrderViewHolder holder = new OrderViewHolder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.category.setText(list.get(position).category);
        holder.description.setText(list.get(position).description);
        holder.imageView.setImageResource(OrderForm.getImageId(list.get(position).category));

    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Insert a new item to the RecyclerView on a predefined position
    public void insert(int position, OrderData OrderData) {
        list.add(position, OrderData);
        notifyItemInserted(position);
    }

    // Remove a RecyclerView item containing a specified OrderData object
    public void remove(OrderData OrderData) {
        int position = list.indexOf(OrderData);
        list.remove(position);
        notifyItemRemoved(position);
    }


}
