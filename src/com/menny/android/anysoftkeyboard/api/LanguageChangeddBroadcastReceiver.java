package com.menny.android.anysoftkeyboard.api;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * @author lado
 * 
 *
 */
public abstract class LanguageChangeddBroadcastReceiver extends BroadcastReceiver {
 
    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public final void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if(action.equals(".NOTIFY_ON")){
           sendNotification(context, intent);
        } else if(action.equals(".NOTIFY_OFF")){
           cancelNotification(context, intent);
        }
        //        if(action.equals("notifylanguage")){
//            Notification notification = new Notification();
//
//            Intent notificationIntent = new Intent();
//            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                    notificationIntent, 0);
//
//            notification.setLatestEventInfo(getApplicationContext(),
//                    getText("ka"), "ka",
//                    contentIntent);
//            //this will not work. Need to find a way to show notification as a different package.
//            //notification.icon = current.getKeyboardIconResId();
//            notification.icon = R.drawable.ka;
//
//            notification.defaults = 0;// no sound, vibrate, etc.
//            // notifying
//            NotificationManager    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); 
//            mNotificationManager.notify(KEYBOARD_NOTIFICATION_ID, notification);
            
        //}
    }

    private void sendNotification(Context ctx, Intent i)
    {  
       int icon =  i.getExtras().getInt("current_layout_resid");
       String name = i.getExtras().getString("current_layout_name");
       String ns = Context.NOTIFICATION_SERVICE;
       NotificationManager nm = 
          (NotificationManager)ctx.getSystemService(ns);
       long when = System.currentTimeMillis();
       Notification notification = 
          new Notification(icon, name, when);
       Intent intent = new Intent(Intent.ACTION_DEFAULT);
       //intent.setData(Uri.parse("http://www.google.com"));
       PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, 0);
       notification.setLatestEventInfo(ctx, "AnySoftKeyboard", name , pi);
       nm.notify(1, notification);
    }

    private void cancelNotification(Context ctx, Intent i)
    {  
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = 
           (NotificationManager)ctx.getSystemService(ns);
        nm.cancel(1);
    }
    
}
