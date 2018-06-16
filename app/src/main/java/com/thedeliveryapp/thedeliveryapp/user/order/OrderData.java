package com.thedeliveryapp.thedeliveryapp.user.order;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderData implements Parcelable {
    public String category, description, userId, status, otp, mode_of_payment;
    public int orderId, min_range, max_range, final_price;
    public float deliveryCharge;
    public UserLocation userLocation = new UserLocation();
    public ExpiryDate expiryDate = new ExpiryDate();
    public ExpiryTime expiryTime = new ExpiryTime();
    public AcceptedBy acceptedBy = new AcceptedBy();

    public OrderData() {
        //For DataSnapshot.getValue()
        //Don't ever try to delete it.
    }

    public OrderData(Parcel in) {
        category = in.readString();
        description = in.readString();
        orderId = in.readInt();
        min_range = in.readInt();
        max_range = in.readInt();
        status = in.readString();
        deliveryCharge = in.readFloat();
        userId = in.readString();

        userLocation.Name = in.readString();
        userLocation.Location = in.readString();
        userLocation.PhoneNumber = in.readString();

        expiryTime.hour = in.readInt();
        expiryTime.minute = in.readInt();

        expiryDate.year = in.readInt();
        expiryDate.month = in.readInt();
        expiryDate.day = in.readInt();

        acceptedBy.name = in.readString();
        acceptedBy.mobile = in.readString();
        acceptedBy.alt_mobile = in.readString();
        acceptedBy.email = in.readString();
        acceptedBy.delivererID = in.readString();

        otp = in.readString();

        mode_of_payment = in.readString();

        final_price = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(description);
        dest.writeInt(orderId);
        dest.writeInt(min_range);
        dest.writeInt(max_range);
        dest.writeString(status);
        dest.writeFloat(deliveryCharge);
        dest.writeString(userId);

        dest.writeString(userLocation.Name);
        dest.writeString(userLocation.Location);
        dest.writeString(userLocation.PhoneNumber);

        dest.writeInt(expiryTime.hour);
        dest.writeInt(expiryTime.minute);

        dest.writeInt(expiryDate.year);
        dest.writeInt(expiryDate.month);
        dest.writeInt(expiryDate.day);

        dest.writeString(acceptedBy.name);
        dest.writeString(acceptedBy.mobile);
        dest.writeString(acceptedBy.alt_mobile);
        dest.writeString(acceptedBy.email);
        dest.writeString(acceptedBy.delivererID);

        dest.writeString(otp);

        dest.writeString(mode_of_payment);

        dest.writeInt(final_price);
    }

    public static  final Parcelable.Creator<OrderData> CREATOR = new Parcelable.Creator<OrderData>() {
        public OrderData createFromParcel(Parcel in) {
            return new OrderData(in);
        }

        public OrderData[] newArray(int size) {
            return new OrderData[size];
        }
    };


    public OrderData(String category, String description, int orderId, int max_range, int min_range,
                     UserLocation location, ExpiryDate expiryDate, ExpiryTime expiryTime, String status,
                     Float deliveryCharge, AcceptedBy acceptedBy, String userId, String otp, String mode_of_payment, int final_price) {
        this.category = category;
        this.description = description;
        this.orderId = orderId;
        this.min_range = min_range;
        this.max_range = max_range;
        this.userLocation = location;
        this.expiryDate = expiryDate;
        this.expiryTime = expiryTime;
        this.status = status;
        this.deliveryCharge = deliveryCharge;
        this.acceptedBy = acceptedBy;
        this.userId = userId;
        this.otp = otp;
        this.mode_of_payment = mode_of_payment;
        this.final_price = final_price;

    }

}
