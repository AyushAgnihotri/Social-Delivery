package com.thedeliveryapp.thedeliveryapp.user.order;

import com.thedeliveryapp.thedeliveryapp.login.user_details.UserDetails;

public class AcceptedBy {
    public String name, mobile, alt_mobile, email, delivererID;

    public AcceptedBy() {

    }

    public AcceptedBy(String name, String mobile, String alt_mobile, String email, String delivererID) {
        this.name = name;
        this.mobile = mobile;
        this.alt_mobile = alt_mobile;
        this.email = email;
        this.delivererID = delivererID;
    }
}