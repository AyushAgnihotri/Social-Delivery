package com.thedeliveryapp.thedeliveryapp.login.user_details;

public class UserDetails {
    public String name;
    public String Mobile;
    public String Alt_Mobile;
    public String Email;
    public int wallet;
    public String playerId;

    public UserDetails() {

    }

    public UserDetails(String name, String Mobile, String Alt_Mobile, String Email, int wallet,String playerId) {
        this.name=name;
        this.Mobile=Mobile;
        this.Alt_Mobile= Alt_Mobile;
        this.Email=Email;
        this.wallet = wallet;
        this.playerId = playerId;
    }

}
