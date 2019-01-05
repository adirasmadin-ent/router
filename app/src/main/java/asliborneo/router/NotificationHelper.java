package asliborneo.router;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;



public class NotificationHelper extends ContextWrapper {

    public static final String NOTIFICATION_CHANNEL_ID = "asliborneo.router";
    public  static final String NOTIFICATION_CHANNEL_NAME = "Route Notification";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            setupChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannel() {

        NotificationChannel routeChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,NOTIFICATION_CHANNEL_NAME
        ,NotificationManager.IMPORTANCE_DEFAULT);
        routeChannel.enableLights(true);
        routeChannel.enableVibration(true);
        routeChannel.setLightColor(Color.BLUE);
        routeChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);
        
        getManager().createNotificationChannel(routeChannel);
                
    }

    public NotificationManager getManager() {
        if (manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public android.app.Notification.Builder getRouteNotification(String title,String content, PendingIntent contentIntent, Uri soundUri)
    {
        return new android.app.Notification.Builder(getApplicationContext(),NOTIFICATION_CHANNEL_ID)
                .setContentText(content)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.cardisabled);
    }
}
