package com.thedeliveryapp.thedeliveryapp.user.order;
public class OrderData {
    public String category;
    public String description;
    public int imageId;
    public int min_range;
    public int max_range;


    // Default constructor required for calls to
    // DataSnapshot.getValue(OrderDetails.class)

    public OrderData(){

    }


    public OrderData(String category, String description, int imageId, int min_range, int max_range) {
        this.category = category;
        this.description = description;
        this.imageId = imageId;
        this.min_range = min_range;
        this.max_range = max_range;
    }

}
