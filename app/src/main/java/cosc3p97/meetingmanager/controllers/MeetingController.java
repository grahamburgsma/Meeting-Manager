package cosc3p97.meetingmanager.controllers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.MutableDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cosc3p97.meetingmanager.models.Meeting;

/**
 * MeetingManager
 * Created by grahamburgsma on 15-11-10.
 * Singleton controller that manages all the meetings. Handles all operations to do with meetings
 */
public class MeetingController {
    private static MeetingController ourInstance = new MeetingController();
    public HashMap<Long, Meeting> meetings;
    private Context context;

    private MeetingController() {
        meetings = new HashMap<>();
    }

    public static MeetingController getInstance() {
        return ourInstance;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //Loads meetings from the database on a new thread (Will make app load times slow if on UI thread)
    //Also, meetings are only loaded from the db once, when the app is opened, because db calls are slow
    public void loadMeetings() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                meetings.putAll(DatabaseHelper.getInstance(context).loadMeetings());
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("meeting_created"));
            }
        }).start();
    }

    //Used solely for notifications
    public void loadMeetingsSynchronous() {
        meetings.putAll(DatabaseHelper.getInstance(context).loadMeetings());
    }

    public List<Meeting> getMeetingsToday() {
        List<Meeting> meetingsToday = new ArrayList<>();

        for (Meeting meeting : meetings.values()) {
            if (DateTimeComparator.getDateOnlyInstance().compare(meeting.startDateTime, new DateTime()) == 0)
                meetingsToday.add(meeting);
        }

        return meetingsToday;
    }

    public List<Meeting> getMeetingsTomorrow() {
        List<Meeting> meetingsToday = new ArrayList<>();

        for (Meeting meeting : meetings.values()) {
            MutableDateTime tomorrow = new MutableDateTime();
            tomorrow.addDays(1);
            if (DateTimeComparator.getDateOnlyInstance().compare(meeting.startDateTime, tomorrow) == 0)
                meetingsToday.add(meeting);
        }

        return meetingsToday;
    }

    public void createMeeting(Meeting meeting) {
        if (meeting.id != -1) {
            DatabaseHelper.getInstance(context).updateMeeting(meeting);
            meetings.put(meeting.id, meeting);
        } else {
            meeting.id = DatabaseHelper.getInstance(context).createMeeting(meeting);
            meetings.put(meeting.id, meeting);
        }
        //Broadcasting event on meeting created, updates listViews
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("meeting_created"));

        if (meeting.notification > 0)
            createNotification(meeting);
    }

    public void deleteMeeting(Meeting meeting) {
        DatabaseHelper.getInstance(context).deleteMeeting(meeting);

        meetings.remove(meeting.id);

        //Cancel notification
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("meeting_id", meeting.id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) meeting.id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("meeting_created"));
    }

    //sets up the alarm for the notification
    private void createNotification(Meeting meeting) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("meeting_id", meeting.id);
        alarmIntent.putExtra("minutes_away", meeting.notification);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) meeting.id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, meeting.startDateTime.toDate().getTime() - meeting.notification * 60000, pendingIntent);
    }
}
