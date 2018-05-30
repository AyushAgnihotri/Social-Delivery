package com.thedeliveryapp.thedeliveryapp.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thedeliveryapp.thedeliveryapp.R;
import com.thedeliveryapp.thedeliveryapp.order_form.OrderForm;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    public static RecyclerViewOrderAdapter adapter;
    List <OrderData> orderList = new ArrayList<OrderData>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);


        mDrawerLayout = findViewById(R.id.drawer_layout);


        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });


        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO : improve OrderData
                Intent intent = new Intent(ItemListActivity.this, OrderForm.class);
                startActivity(intent);
                //finish();

            }
        });

        fill_with_data();

        RecyclerView recyclerView = findViewById(R.id.item_list);
        adapter = new RecyclerViewOrderAdapter(orderList, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // animation
        RecyclerView.ItemAnimator itemAnimator = new
                DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        recyclerView.setItemAnimator(itemAnimator);

        recyclerView.addOnItemTouchListener(new UserOrderTouchListener(this, recyclerView, new UserOrderItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                OrderData clickedOrder = orderList.get(position);
                startActivity(new Intent(ItemListActivity.this,ItemDetailActivity.class));
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    void fill_with_data() {

        //TODO : FETCH FROM DATABASE

       orderList.add(new OrderData("Batman vs Superman", "Following the destruction of Metropolis, Batman embarks on a personal vendetta against Superman ", R.drawable.ic_action_movie, 100, 200));
       orderList.add(new OrderData("X-Men: Apocalypse", "X-Men: Apocalypse is an upcoming American superhero film based on the X-Men characters that appear in Marvel Comics ", R.drawable.ic_action_movie, 100, 200));
       orderList.add(new OrderData("Captain America: Civil War", "A feud between Captain America and Iron Man leaves the Avengers in turmoil.  ", R.drawable.ic_action_movie, 100 , 200));
       orderList.add(new OrderData("Kung Fu Panda 3", "After reuniting with his long-lost father, Po  must train a village of pandas", R.drawable.ic_action_movie, 100, 200));
       orderList.add(new OrderData("Warcraft", "Fleeing their dying home to colonize another, fearsome orc warriors invade the peaceful realm of Azeroth. ", R.drawable.ic_action_movie, 100 ,200));
       orderList.add(new OrderData("Alice in Wonderland", "Alice in Wonderland: Through the Looking Glass ", R.drawable.ic_action_movie, 100, 200));

    }


    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class RecyclerViewOrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

        List<OrderData> list = orderList;
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
            holder.imageView.setImageResource(list.get(position).imageId);

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
    public interface UserOrderItemClickListener {
        public void onClick(View view, int position);

        public void onLongClick(View view, int position);
    }
    
    public class UserOrderTouchListener implements RecyclerView.OnItemTouchListener {

        //GestureDetector to intercept touch events
        GestureDetector gestureDetector;
        private UserOrderItemClickListener clickListener;

        public UserOrderTouchListener(Context context, final RecyclerView recyclerView, final UserOrderItemClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    //find the long pressed view
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildLayoutPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e) {

            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, recyclerView.getChildLayoutPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
    

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
}



