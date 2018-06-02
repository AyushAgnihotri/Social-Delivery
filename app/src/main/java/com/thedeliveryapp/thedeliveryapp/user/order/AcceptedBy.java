package com.thedeliveryapp.thedeliveryapp.user.order;

import com.thedeliveryapp.thedeliveryapp.login.user_details.UserDetails;

public class AcceptedBy extends UserDetails{
    public String delivererID;

    public AcceptedBy() {

    }

    public  AcceptedBy(String name, String email, String delivererID, String mobile, String alt_mobile) {
        this.name = name;
        this.Email = email;
        this.delivererID = delivererID;
        this.Mobile = mobile;
        this.Alt_Mobile = alt_mobile;
    }
}