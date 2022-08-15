package com.makepe.curiosityhubls.Notifications;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.makepe.curiosityhubls.MessageActivity;
import com.makepe.curiosityhubls.PostDetailsActivity;
import com.makepe.curiosityhubls.R;

import java.util.Random;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class FirebaseMessaging extends FirebaseMessagingService {

    public static final String ADMIN_CHANNEL_ID = "admin_channel";
    public static final String KEY_TEXT_REPLY = "key_text_reply";

    int SUMMARY_ID = 0;
    String GROUP_KEY_POSTS = "com.makepe.curiosityls";

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        //update user token
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //String newToken = FirebaseInstanceId.getInstance().getToken();
        if (user != null) {
            //signed in, update token
            updateToken(s);
        }
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        assert user != null;
        ref.child(user.getUid()).setValue(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");

        String notificationType = remoteMessage.getData().get("notificationType");
        if(notificationType.equals("PostNotification")){
            //for post notifications
            String sender = remoteMessage.getData().get("sender");
            String postID = remoteMessage.getData().get("postID");
            String title = remoteMessage.getData().get("title");
            String caption = remoteMessage.getData().get("caption");
            
            //if user if same that has posted dont show notification
            if(!sender.equals(savedCurrentUser)){
                showPostNotification("" + postID, "" + title, "" +caption);
            }

        } else if (notificationType.equals("ChatNotification")) {
            //for sending chat notifications
            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if(firebaseUser != null && sent.equals(firebaseUser.getUid())){
                if(!savedCurrentUser.equals(user)){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        sendOreoNotification(remoteMessage);
                    }else{
                        sendNormalNotification(remoteMessage);
                    }
                }
            }
        }
    }

    private void showPostNotification(String postID, String title, String caption) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(3000);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setupPostNotificationChannel(notificationManager);
        }

        //show post detail activity using post id when notification is clicked
        Intent intent = new Intent(this, PostDetailsActivity.class);
        intent.putExtra("postID", postID);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //large icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ch_logo1);

        //sound for notification
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ch_logo1)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(caption)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent)
                .setGroup(GROUP_KEY_POSTS)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);

        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPostNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "New Notification";
        String channelDescription = "Device to device post notification";

        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);

        if(notificationManager != null){
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("receiverID", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoNotification notification1 = new OreoNotification(this);
        Notification.Builder builder = notification1.getOreoNotification(title, body, pendingIntent,defaultSound, icon);

        int i = 0;
        if(j > 0){
            i = j;
        }
        notification1.getManager().notify(j, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void sendOreoNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("receiverID", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if(j > 0){
            i = j;
        }

        notificationManager.notify(j, builder.build());

    }

}
