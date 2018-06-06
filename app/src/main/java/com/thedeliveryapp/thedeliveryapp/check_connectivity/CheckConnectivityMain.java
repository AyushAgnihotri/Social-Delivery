package com.thedeliveryapp.thedeliveryapp.check_connectivity;

import android.app.Application;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.thedeliveryapp.thedeliveryapp.deliverer.DelivererOrderDetailActivity;
import com.thedeliveryapp.thedeliveryapp.login.LoginActivity;
import com.thedeliveryapp.thedeliveryapp.user.UserOrderDetailActivity;
import com.thedeliveryapp.thedeliveryapp.user.order.OrderData;

import org.json.JSONObject;

public class CheckConnectivityMain extends Application {
    private static CheckConnectivityMain mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        setUpOneSignal();
        mInstance = this;
    }

    public static synchronized CheckConnectivityMain getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
    void setUpOneSignal() {

        OneSignal.startInit(this)
                .setNotificationReceivedHandler(new CheckConnectivityMain.MyNotificationReceivedHandler())
                .setNotificationOpenedHandler(new CheckConnectivityMain.MyNotificationOpenedHandler())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

    }

    private class MyNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(OSNotification notification) {
            JSONObject data = notification.payload.additionalData;
            String notificationID = notification.payload.notificationID;
            String title = notification.payload.title;
            String body = notification.payload.body;
            String smallIcon = notification.payload.smallIcon;
            String largeIcon = notification.payload.largeIcon;
            String bigPicture = notification.payload.bigPicture;
            String smallIconAccentColor = notification.payload.smallIconAccentColor;
            String sound = notification.payload.sound;
            String ledColor = notification.payload.ledColor;
            int lockScreenVisibility = notification.payload.lockScreenVisibility;
            String groupKey = notification.payload.groupKey;
            String groupMessage = notification.payload.groupMessage;
            String fromProjectNumber = notification.payload.fromProjectNumber;
            String rawPayload = notification.payload.rawPayload;

            String customKey;

            Log.i("OneSignalExample", "NotificationID received: " + notificationID);

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null)
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
            }
        }
    }

    private class MyNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            OSNotificationAction.ActionType actionType = result.action.type;
            JSONObject data = result.notification.payload.additionalData;

            if (data != null) {
                String userId = data.optString("userId");
                String orderId = data.optString("orderId");
                DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                root.child("deliveryApp").child("orders").child(userId).child(orderId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        OrderData myOrder = dataSnapshot.getValue(OrderData.class);
                        Intent intent = new Intent(getApplicationContext(),UserOrderDetailActivity.class);
                        intent.putExtra("MyOrder",(Parcelable) myOrder);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }


            // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
            //   if you are calling startActivity above.
        /*
           <application ...>
             <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
           </application>
        */
        }
    }
}
