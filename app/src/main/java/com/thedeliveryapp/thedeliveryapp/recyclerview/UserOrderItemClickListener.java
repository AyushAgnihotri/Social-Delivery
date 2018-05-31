package com.thedeliveryapp.thedeliveryapp.recyclerview;

import android.view.View;

public interface UserOrderItemClickListener {
    public void onClick(View view, int position);

    public void onLongClick(View view, int position);
}
