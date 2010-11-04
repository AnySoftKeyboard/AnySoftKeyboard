package com.menny.android.anysoftkeyboard.api;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * @author lado
 * if NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_PACKAGE param name is not same as getClass().getPackage().getName();
 * The notifcation is removed.
 *
 */
public abstract class LayoutChangedBroadcastReceiver extends BroadcastReceiver {
    //api
    //private static final String PACKAGE = 
    private static final String NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_RESID = "current_layout_resid";
    private static final String NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_NAME = "current_layout_name";
    private static final String NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_PACKAGE = "current_layout_package";
    private static final String NOTIFY_LAYOUT_SWITCH_NOTIFICATION_FLAGS = "notification_flags";
    private static final String NOTIFY_LAYOUT_SWITCH_NOTIFICATION_TITLE = "notification_title";
   
    private static final String TAG = "ASK-LCBR";
    private static final boolean DEBUG = false;
    
    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public final void onReceive(Context ctx, Intent intent) {
        String currentPackage = getClass().getPackage().getName();
        if(DEBUG)
        Log.d(TAG, "Calling  onReceive for package: " + currentPackage);
        String pack  = intent.getExtras().getString(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_PACKAGE);
        if(DEBUG)
        Log.d(TAG, "onReceive got package " + pack);
        if(!currentPackage.equals(pack)){
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nm = 
               (NotificationManager)ctx.getSystemService(ns);
            nm.cancel(1);
            return;
        }

       int icon =  intent.getExtras().getInt(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_RESID);
       String name = intent.getExtras().getString(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_NAME);
       int flags = intent.getExtras().getInt(NOTIFY_LAYOUT_SWITCH_NOTIFICATION_FLAGS);
       
       //prepare notification
       String ns = Context.NOTIFICATION_SERVICE;
       NotificationManager nm = 
          (NotificationManager)ctx.getSystemService(ns);
       long when = System.currentTimeMillis();
       Notification notification = 
          new Notification(icon, name, when);
       notification.defaults = 0;
       notification.flags = flags;
    
       
       Intent i = new Intent(ctx, InfoActivity.class);
       PendingIntent pi = PendingIntent.getActivity(ctx, 0, i, 0);
       notification.setLatestEventInfo(ctx, intent.getExtras().getString(NOTIFY_LAYOUT_SWITCH_NOTIFICATION_TITLE), name , pi);
       nm.notify(1, notification);
    }
    
}
