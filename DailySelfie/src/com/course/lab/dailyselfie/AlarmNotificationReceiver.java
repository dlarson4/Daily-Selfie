package com.course.lab.dailyselfie;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class AlarmNotificationReceiver extends BroadcastReceiver
{
    private static final String CLASSNAME = AlarmNotificationReceiver.class.getSimpleName();
    
    // Notification ID to allow for future updates
    private static final int MY_NOTIFICATION_ID = 1;
    private static final long[] VIBRATE_PATTERN = { 0, 200, 200, 300 };
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
//        if(Logger.isDebugEnabled())
//        {
//            Logger.debug("[%s.%s] AppStatus.getStatus() = '%s'", CLASSNAME, "onReceive", AppStatus.INSTANCE.getStatus());
//        }
        
//        final AppStatus.Status s = AppStatus.INSTANCE.getStatus();
//        if(s == AppStatus.Status.Created ||s == AppStatus.Status.Resumed || s == AppStatus.Status.Started)
//        {
//            if(Logger.isDebugEnabled())
//            {
//                Logger.debug("[%s.%s] AppStatus is '%s', no need for notification reminder.  Returning.", CLASSNAME, "onReceive", AppStatus.INSTANCE.getStatus());
//            }
//            return;
//        }
    
        generateNotification(context);
    }
    
    private void generateNotification(Context context)
    {
        Notification notification = createNotificationWithLayout(context);

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if(Logger.isDebugEnabled())
        {
            Logger.debug("[%s.%s] Creating notification", CLASSNAME, "generateNotification");
        }
        
        notificationManager.notify(MY_NOTIFICATION_ID, notification);
    }

    private Notification createNotificationWithLayout(Context context)
    {
        RemoteViews mContentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
        
        String timeStamp = new SimpleDateFormat("h:mm a").format(new Date());
        mContentView.setTextViewText(R.id.when, timeStamp);
        
        PendingIntent mContentIntent = createPendingIntent(context);

        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setTicker(context.getResources().getString(R.string.time_for_selfie))
                .setSmallIcon(R.drawable.ic_menu_camera)
                .setAutoCancel(true) // hide the notification after its selected
                .setContentIntent(mContentIntent)
                .setVibrate(VIBRATE_PATTERN)
                .setWhen(System.currentTimeMillis())
                .setContent(mContentView);

        return notificationBuilder.build();
    }
    
    private PendingIntent createPendingIntent(Context context)
    {
        Intent mNotificationIntent = new Intent(context, GPhotoGridActivity.class);
        PendingIntent mContentIntent = PendingIntent.getActivity(context, 0, mNotificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
        return mContentIntent;
    }
            
    
//  private Notification createBasicNotification(Context context)
//  {
//      PendingIntent mContentIntent = createPendingIntent(context);
//
//      Notification.Builder notificationBuilder = new Notification.Builder(context)
//              .setTicker(context.getResources().getString(R.string.time_to_take_a_selfie))
//              .setSmallIcon(R.drawable.ic_menu_camera)
//              .setAutoCancel(true) // hide the notification after its selected
//              .setContentIntent(mContentIntent)
//              .setVibrate(VIBRATE_PATTERN)
//              .setContentText(context.getResources().getString(R.string.daily_selfie))
//              .setSubText(context.getResources().getString(R.string.time_to_take_a_selfie))
//              .setWhen(System.currentTimeMillis());
//
//      return notificationBuilder.build();
//  }
    
}
