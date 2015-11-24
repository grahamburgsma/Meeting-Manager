package cosc3p97.meetingmanager.controllers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import cosc3p97.meetingmanager.R;
import cosc3p97.meetingmanager.models.Meeting;
import cosc3p97.meetingmanager.views.MainActivity;

/**
 * MeetingManager
 * Created by grahamburgsma on 15-11-16.
 * This is a receiver for the alarm, it then creates and sends a notification
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (MeetingController.getInstance().getContext() == null) { //If app was closed
            MeetingController.getInstance().setContext(context);
            MeetingController.getInstance().loadMeetingsSynchronous();
        }

        Meeting meeting = MeetingController.getInstance().meetings.get(intent.getLongExtra("meeting_id", -1));

        if (meeting != null) {
            Intent resultIntent = new Intent(context, MainActivity.class);
            resultIntent.putExtra("meeting_id", meeting.id);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            int minutesAway = intent.getIntExtra("minutes_away", 15);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Meeting in " + (minutesAway == 60 ? "1 hour" : minutesAway + " minutes"))
                    .setContentText(meeting.title)
                    .setContentIntent(resultPendingIntent)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setCategory(Notification.CATEGORY_REMINDER)
                    .setWhen(meeting.startDateTime.toDate().getTime());

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(((int) meeting.id), mBuilder.build());
        }
    }
}
