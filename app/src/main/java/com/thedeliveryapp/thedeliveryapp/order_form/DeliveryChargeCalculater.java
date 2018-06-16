package com.thedeliveryapp.thedeliveryapp.order_form;

public class DeliveryChargeCalculater {
    public float deliveryCharge;

    DeliveryChargeCalculater(int max_range) {
        deliveryCharge= (float) 50.0;
        if(max_range<100)
            deliveryCharge = (float) 50.0;
        else if (max_range>=100 && max_range <500)
            deliveryCharge += 0.1*max_range;
        else if (max_range>=500 && max_range<1000)
            deliveryCharge += 0.15*max_range;
        else if (max_range>=1000 && max_range<5000)
            deliveryCharge += 0.20*max_range;
        else if (max_range>=5000)
            deliveryCharge +=1200.00;
    }
}
