package com.thedeliveryapp.thedeliveryapp.user.order;
public class OrderData {
    public String category;
    public String description;
    public int imageId;

    public OrderData(String category, String description, int imageId) {
        this.category = category;
        this.description = description;
        this.imageId = imageId;
    }

}