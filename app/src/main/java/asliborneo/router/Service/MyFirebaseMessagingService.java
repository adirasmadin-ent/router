package asliborneo.router.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import asliborneo.router.Model.Token;
import asliborneo.router.NotificationHelper;
import asliborneo.router.R;
import asliborneo.router.Rate_Driver;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        UpdateserverToken(s);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {
            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            final String message = data.get("message");
            if (title != null)
                if (title.equals("Cancel")) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MyFirebaseMessagingService.this, message, Toast.LENGTH_LONG).show();
                        }
                    });

                } else if (title.equals("Arrived Notification")) {
                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        showArrivedNotificationAPI26(message);
                       else
                       show_arrived_notification(message);

                } else if (title.equals("Drop Off")) {
                    open_rating_activity(message);
                }

        }






    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String body) {
        PendingIntent content_intent=PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getRouteNotification("Arrived Notification",body, content_intent,defaultSound);

        notificationHelper.getManager().notify(1,builder.build());
    }

    private void open_rating_activity(String body) {
        Intent intent=new Intent(this, Rate_Driver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void show_arrived_notification(String body) {
        PendingIntent content_intent=PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(getBaseContext()).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon( R.drawable.cardisabled)
                .setContentTitle("Arrived Notification")
                .setContentText(body)
                .setContentIntent(content_intent);
        NotificationManager manager= (NotificationManager) getBaseContext().getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());

    }
    private void UpdateserverToken(final String refreshedToken) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference tokens = db.getReference("Tokens");
        final Token token = new Token(refreshedToken);

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                tokens.child(account.getId())
                        .setValue(token);
            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.d("ERROR ACCOUNTKIT", ""+accountKitError.getUserFacingMessage());
            }
        });
    }






}
