package com.thedeliveryapp.thedeliveryapp.login.user_details;

public class UserDetails {
    private String name;
    private String Mobile;
    private String Alt_Mobile;
    private String Email;
    private int last_order;

    public UserDetails(String name, String Mobile, String Alt_Mobile, String Email, int last_order) {
        this.name=name;
        this.Mobile=Mobile;
        this.Alt_Mobile= Alt_Mobile;
        this.Email=Email;
        this.last_order=last_order;
    }

}
