package com.thedeliveryapp.thedeliveryapp.order_form;

public class DeliveryChargeCalculater {
    public int deliveryCharge;
    public int max_price, total_price;

    DeliveryChargeCalculater(int max_range) {
        deliveryCharge= 10;
        if (max_range>=100 && max_range <500)
            deliveryCharge += 0.1*max_range;
        else if (max_range>=500 && max_range<1000)
            deliveryCharge += 0.15*max_range;
        else if (max_range>=1000 && max_range<5000)
            deliveryCharge += 0.20*max_range;
        else if (max_range>=5000)
            deliveryCharge +=1200.00;

        max_price = max_range;
        total_price = max_price + deliveryCharge;
    }

    DeliveryChargeCalculater () {

    }
}
