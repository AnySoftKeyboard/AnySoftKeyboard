package com.anysoftkeyboard.api;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;


/**
 * @author lado
 *         if NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_PACKAGE param name is not same as getClass().getPackage().getName();
 *         The notifcation is removed.
 */
public abstract class LayoutChangedBroadcastReceiver extends BroadcastReceiver {
    //api
    //private static final String PACKAGE = 
    private static final String NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_RESID = "current_layout_resid";
    private static final String NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_NAME = "current_layout_name";
    private static final String NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_PACKAGE = "current_layout_package";
    private static final String NOTIFY_LAYOUT_SWITCH_NOTIFICATION_FLAGS = "notification_flags";
    private static final String NOTIFY_LAYOUT_SWITCH_NOTIFICATION_TITLE = "notification_title";


    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public final void onReceive(Context ctx, Intent intent) {
        String currentPackage = getClass().getPackage().getName();
        String pack = intent.getExtras().getString(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_PACKAGE);
        if (!currentPackage.equals(pack)) {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nm = (NotificationManager) ctx.getSystemService(ns);
            nm.cancel(1);
            return;
        }

        int icon = intent.getExtras().getInt(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_RESID);
        String name = intent.getExtras().getString(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_NAME);
        String title = intent.getExtras().getString(NOTIFY_LAYOUT_SWITCH_NOTIFICATION_TITLE);
        int flags = intent.getExtras().getInt(NOTIFY_LAYOUT_SWITCH_NOTIFICATION_FLAGS);

        //prepare notification
        Intent i = new Intent(ctx, InfoActivity.class);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, i, 0);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nm = (NotificationManager) ctx.getSystemService(ns);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        builder.setSmallIcon(icon);
        builder.setContentTitle(title);
        builder.setContentText(name);
        builder.setDefaults(0);
        builder.setContentIntent(pi);
        Notification notification = builder.build();
        notification.flags = flags;

        nm.notify(1, notification);
    }

}
