package HelperClass;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import Const.AppConst;
import ca.zhuoliupei.observationapp.LoginActivity;
import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 3/16/2016.
 */
public class NotificationUtil {
    public static void showNotification(Context context,int NOTIFICATION_ID){
        int iconSrcID = R.drawable.icon_upload;
        String title="";
        String text="";
        Intent resultIntent=null;
        switch (NOTIFICATION_ID){
            case AppConst.SESSION_EXPIRED_NOTIFICATION_ID:{
                iconSrcID=R.drawable.icon_expired;
                title=context.getString(R.string.sessionExpiredNotificationTitle);
                text=context.getString(R.string.sessionExpiredNotificationText);
                resultIntent=new Intent(context,LoginActivity.class);
                break;
            }
            case AppConst.UPLOAD_FAILED_NOTIFICATION_ID:{
                iconSrcID=R.drawable.icon_failed;
                title=context.getString(R.string.uploadFailedNotificationTitle);
                text=context.getString(R.string.uploadFailedNotificationText);
                break;
            }
            case AppConst.UPLOADING_NOTIFICATION_ID:{
                iconSrcID=R.drawable.icon_upload;
                title=context.getString(R.string.uploadingNotificationTitle);
                text=context.getString(R.string.uploadingNotificationText);
                break;
            }
        }
        NotificationCompat.Builder mBuilder=
                new NotificationCompat.Builder(context) .
                        setSmallIcon(iconSrcID).
                        setContentTitle(title).
                        setContentText(text);

        if (resultIntent!=null) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
        }
        NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID,mBuilder.build());
    }

    public static void removeNotification(Context context,int notificationID){
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) context.getApplicationContext().getSystemService(ns);
        nMgr.cancel(notificationID);
    }
}
