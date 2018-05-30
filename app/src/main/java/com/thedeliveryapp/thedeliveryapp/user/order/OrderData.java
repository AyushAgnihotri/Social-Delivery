package com.thedeliveryapp.thedeliveryapp.user.order;

import android.os.LocaleList;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;

public class OrderData implements Parcelable {
    public String category;
    public String description;
    public int imageId;
    public int min_range;
    public int max_range;
    public UserLocation userLocation = new UserLocation();

    public OrderData() {
        //For DataSnapshot.getValue()
        //Don't ever try to delete it.
    }

    public  OrderData(Parcel in) {
        category = in.readString();
        description = in.readString();
        imageId = in.readInt();
        min_range = in.readInt();
        max_range = in.readInt();
        userLocation.Name = in.readString();
        userLocation.Location = in.readString();
        userLocation.PhoneNumber = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(description);
        dest.writeInt(imageId);
        dest.writeInt(min_range);
        dest.writeInt(max_range);
        dest.writeString(userLocation.Name);
        dest.writeString( userLocation.Location);
        dest.writeString(userLocation.PhoneNumber);
    }

    public static  final Parcelable.Creator<OrderData> CREATOR = new Parcelable.Creator<OrderData>() {
        public OrderData createFromParcel(Parcel in) {
            return new OrderData(in);
        }

        public OrderData[] newArray(int size) {
            return new OrderData[size];
        }
    };


    public OrderData(String category, String description, int imageId, int min_range, int max_range,UserLocation location) {
        this.category = category;
        this.description = description;
        this.imageId = imageId;
        this.min_range = min_range;
        this.max_range = max_range;
        this.userLocation = location;
    }


}
