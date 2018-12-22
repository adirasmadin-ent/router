package asliborneo.router.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import asliborneo.router.R;
import asliborneo.router.Rate_Driver;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (Objects.equals(Objects.requireNonNull(remoteMessage.getNotification()).getTitle(), "Cancel")) {
            Handler handler = new Handler(getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessagingService.this, remoteMessage.getNotification().getBody(), Toast.LENGTH_LONG).show();
                }
            });
        }else if(remoteMessage.getNotification().getTitle().equals("Arrived")) {
            show_arrived_notification(remoteMessage.getNotification().getBody());
        }else if(remoteMessage.getNotification().getTitle().equals("Drop Off")) {
            open_rating_activity(remoteMessage.getNotification().getBody());
        }
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
                .setSmallIcon( R.mipmap.ic_launcher_round)
                .setContentTitle(body)
                .setContentIntent(content_intent);
        NotificationManager manager= (NotificationManager) getBaseContext().getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0,builder.build());

    }
}

