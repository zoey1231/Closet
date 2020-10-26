package com.example.frontend;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.widget.Toast.makeText;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private static final String EMPTY_STRING = "";
    private String message = EMPTY_STRING;
    String userToken;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
        }


    }

    /**
     * Called if FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve
     * the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        MainActivity activity = new MainActivity();
        sendRegistrationToServer(userToken,token);
    }

    public void getTokenNSendToServer(final String userToken) {
        Log.d(TAG,"userToken: "+userToken);
        this.userToken = userToken;

        // [START log_reg_token]
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());

                        }
                        // Get new FCM registration token
                        String token = task.getResult();

                        //send token to server
                        sendRegistrationToServer(userToken,token);

                    }
                });
        // [END log_reg_token]

    }




    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any
     * server-side account maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String userToken, String token) {
        JSONObject postData = new JSONObject();
        JSONObject message = new JSONObject();
        JSONObject messageBody = new JSONObject();

        try {
            messageBody.put("title","test");
            messageBody.put("body","Welcome to our app: CLOSET!");
            message.put("notification",messageBody);
            postData.put("registrationToken", token);
            postData.put("message", message);

            Log.d(TAG,"prepared to send firebase token to server with token: "+token);
            sendDataToServer(postData,userToken,"http://closet.westus.cloudapp.azure.com/api/notifications/");

        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    private void sendDataToServer(final JSONObject userData,String userToken, String url) {
        ServerCommunicationAsync serverCommunication = new ServerCommunicationAsync();
        final String data = userData.toString();
        Log.d(TAG,"usertoken in sendDataToServer is: "+userToken);

        serverCommunication.postWithAuthentication(url, data, userToken, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Fail to send request to server");
                Log.d(TAG, String.valueOf(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                //retrieve user data from server's response
                JSONObject responseJson = null;
                try {
                    responseJson = new JSONObject(responseStr);
                    if(responseJson.has("message"))
                        message = responseJson.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (response.isSuccessful()) {

                    if(responseJson.has("message")){
                        Log.d(TAG,message);

                    }
                    else{
                        Log.d(TAG,"send firebase token to server successfully");
                    }

                } else {
                    // Request not successful
                    Log.d(TAG,"Request not successful");
                    if(responseJson.has("message")){
                        Log.d(TAG,message);
                    }
                }
            }
        });
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
