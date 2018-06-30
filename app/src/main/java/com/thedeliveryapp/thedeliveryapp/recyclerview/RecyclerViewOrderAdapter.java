package com.thedeliveryapp.thedeliveryapp.recyclerview;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchUIUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.deliverer.DelivererViewActivity;
import com.thedeliveryapp.thedeliveryapp.login.LoginActivity;
import com.thedeliveryapp.thedeliveryapp.login.MainActivity;
import com.thedeliveryapp.thedeliveryapp.order_form.OrderForm;
import com.thedeliveryapp.thedeliveryapp.user.UserViewActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.ExpiryDate;
import com.thedeliveryapp.thedeliveryapp.user.order.ExpiryTime;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.zip.CheckedOutputStream;

public class RecyclerViewOrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

    List<OrderData> list;
    Context context;
    String status;
    String price;
    String charge;
    Calendar calendar = Calendar.getInstance();
    List<OrderData> pendingRemovalList;
    ColorGenerator generator = ColorGenerator.MATERIAL;

    private static  int PENDING_REMOVAL_TIMEOUT = 1500; // 3sec
    private Handler handler = new Handler(); // handler for running delayed runnables
    private HashMap<OrderData, Runnable> pendingRunables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be

    public RecyclerViewOrderAdapter(List<OrderData> list, Context context) {
        this.list = list;
        this.context = context;
        pendingRemovalList = new ArrayList<>();
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

        
        final OrderData data = list.get(position);
 
        if (pendingRemovalList.contains(data)) {
            /** {show swipe layout} and {hide regular layout} */
            holder.cv.setVisibility(View.GONE);
            holder.isClickable = false;
            holder.swipeLayout.setVisibility(View.VISIBLE);
            holder.undo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    undoOpt(data);
                }
            });
        } else {
            /** {show regular layout} and {hide swipe layout} */
            holder.cv.setVisibility(View.VISIBLE);
            holder.isClickable = true;
            holder.swipeLayout.setVisibility(View.GONE);
            OrderData order = list.get(position);
            //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
            status = String.valueOf(order.status.charAt(0));
            price = Integer.toString(order.max_range);
            charge = Float.toString(order.deliveryCharge);
            holder.category.setText(order.category);
            setExpiry(order.expiryDate,order.expiryTime);
            String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
            String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.getTime());

            holder.expiry.setText(date.substring(0,date.length()-4) + " " + time);
            if(order.description.length() > 40)
                holder.description.setText(order.description.substring(0,39) + "...");
            else
                holder.description.setText(order.description);

            TextDrawable drawable = TextDrawable.builder().buildRound(status,Color.parseColor(getColor(status)));
            holder.imageView.setImageDrawable(drawable);
            //primary color
            drawable = TextDrawable.builder().buildRoundRect("₹"+price,Color.parseColor("#01AAD5"),20);
            holder.displayPrice.setImageDrawable(drawable);
            drawable = TextDrawable.builder().beginConfig().textColor(Color.WHITE).endConfig().buildRound("₹"+charge,Color.BLACK);
            holder.displayCharge.setImageDrawable(drawable);


        }

    }

    void setExpiry(ExpiryDate expiryDate, ExpiryTime expiryTime) {

        calendar.set(Calendar.YEAR, expiryDate.year);
        calendar.set(Calendar.MONTH, expiryDate.month);
        calendar.set(Calendar.DAY_OF_MONTH, expiryDate.day);
        calendar.set(Calendar.HOUR_OF_DAY, expiryTime.hour);
        calendar.set(Calendar.MINUTE, expiryTime.minute);

    }
    private void undoOpt(OrderData delOrder) {
        Runnable pendingRemovalRunnable = pendingRunables.get(delOrder);
        pendingRunables.remove(delOrder);
        if (pendingRemovalRunnable != null)
            handler.removeCallbacks(pendingRemovalRunnable);
        pendingRemovalList.remove(delOrder);
        // this will rebind the row in "normal" state
        notifyItemChanged(list.indexOf(delOrder));
    }

    public void pendingRemoval(int position) {
 
        final OrderData data = list.get(position);
        if (!pendingRemovalList.contains(data)) {
            pendingRemovalList.add(data);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the data
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    setStatusCancelled(data.orderId);
                    remove(list.indexOf(data));
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunables.put(data, pendingRemovalRunnable);
        }
    }

    void setStatusCancelled(final int orderId) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = user.getUid();

        final DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        DatabaseReference stat_ref = root.child("deliveryApp").child("orders").child(userId).child(Integer.toString(orderId)).child("status");
        stat_ref.keepSynced(true);
        stat_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String curr_status = dataSnapshot.getValue(String.class);
                if(curr_status.equals("PENDING"))
                    root.child("deliveryApp").child("orders").child(userId).child(Integer.toString(orderId)).child("status").setValue("CANCELLED");
                else {
                    Toast.makeText(context, "Can not cancel , this order is active", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        if(position != -1) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void remove(int position) {
        if(list.isEmpty())
            return;
        OrderData data = list.get(position);
        if (pendingRemovalList.contains(data)) {
            pendingRemovalList.remove(data);
        }
        if (list.contains(data)) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public boolean isPendingRemoval(int position) {
        OrderData data = list.get(position);
        return pendingRemovalList.contains(data);
    }

    String getColor(String st) {

        if(st.equals("P"))
            return "#ffa000";
        else if(st.equals("A"))
            return "#8bc34a";
        else if(st.equals("E"))
            return "#f44336";
        else if(st.equals("F"))
            return "#673AB7";
        else
            return "#9e9e9e";

    }

 


}
