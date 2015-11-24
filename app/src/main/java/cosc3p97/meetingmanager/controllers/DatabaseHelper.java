package cosc3p97.meetingmanager.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.joda.time.MutableDateTime;

import java.util.HashMap;

import cosc3p97.meetingmanager.models.Meeting;

/**
 * MeetingManager
 * Created by grahamburgsma on 15-11-10.
 * This is a Singleton database helper, all methods insert and delete are done in this class
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database
    private static final String DATABASE_NAME = "meetings";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    private static final String TABLE_MEETINGS = "meetings";
    private static final String TABLE_CONTACTS = "contacts";

    // Key IDs
    private static final String KEY_ID = "id";
    private static final String KEY_MEETING_ID = "meeting_id";
    private static final String KEY_CONTACT_ID = "contact_id";
    private static final String KEY_START_TIME = "starttime";
    private static final String KEY_END_TIME = "endtime";
    private static final String KEY_TITLE = "title";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_NOTE = "note";
    private static final String KEY_NOTIFICATION = "notification";

    private static final String CREATE_TABLE_MEETINGS = "CREATE TABLE "
            + TABLE_MEETINGS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_TITLE + " TEXT,"
            + KEY_LOCATION + " TEXT,"
            + KEY_NOTE + " TEXT,"
            + KEY_NOTIFICATION + " INTEGER,"
            + KEY_START_TIME + " DATETIME,"
            + KEY_END_TIME + " DATETIME" + ")";

    private static final String CREATE_TABLE_CONTACTS = "CREATE TABLE "
            + TABLE_CONTACTS + "(" + KEY_MEETING_ID + " INTEGER,"
            + KEY_CONTACT_ID + " TEXT" + ")";

    private static DatabaseHelper ourInstance;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new DatabaseHelper(context);

        return ourInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEETINGS);
        db.execSQL(CREATE_TABLE_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEETINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    //This loads all the meetings from the DB and returns them as a hashmap
    //Called when the app is opened
    public HashMap<Long, Meeting> loadMeetings() {
        HashMap<Long, Meeting> meetings = new HashMap<>();
        String selectQueryGroups = "SELECT  * FROM " + TABLE_MEETINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQueryGroups, null);

        if (c.moveToFirst()) {
            do {
                Meeting meeting = new Meeting();
                meeting.id = c.getInt(c.getColumnIndex(KEY_ID));
                meeting.notification = c.getInt(c.getColumnIndex(KEY_NOTIFICATION));
                meeting.title = c.getString(c.getColumnIndex(KEY_TITLE));
                meeting.location = c.getString(c.getColumnIndex(KEY_LOCATION));
                meeting.note = c.getString(c.getColumnIndex(KEY_NOTE));
                meeting.startDateTime = new MutableDateTime(c.getLong(c.getColumnIndex(KEY_START_TIME)));
                meeting.endDateTime = new MutableDateTime(c.getLong(c.getColumnIndex(KEY_END_TIME)));

                String selectQueryContacts = "SELECT * FROM " + TABLE_CONTACTS + " WHERE " + KEY_MEETING_ID + " = " + meeting.id;

                Cursor cContact = db.rawQuery(selectQueryContacts, null);

                if (cContact.moveToFirst()) {
                    do {
                        String contactString = cContact.getString(cContact.getColumnIndex(KEY_CONTACT_ID));
                        meeting.contacts.add(contactString);
                    } while (cContact.moveToNext());
                }
                cContact.close();

                meetings.put(meeting.id, meeting);
            } while (c.moveToNext());
        }
        c.close();

        return meetings;
    }

    //Creates a meeting in the db
    //Inserts into the meetings table and contacts table
    public Long createMeeting(Meeting meeting) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_TITLE, meeting.title);
        values.put(KEY_LOCATION, meeting.location);
        values.put(KEY_NOTE, meeting.note);
        values.put(KEY_NOTIFICATION, meeting.notification);
        values.put(KEY_START_TIME, meeting.startDateTime.toDate().getTime());
        values.put(KEY_END_TIME, meeting.endDateTime.toDate().getTime());

        Long meetingId = db.insertWithOnConflict(TABLE_MEETINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        for (String user : meeting.contacts) {
            ContentValues contactValues = new ContentValues();
            contactValues.put(KEY_MEETING_ID, meetingId);
            contactValues.put(KEY_CONTACT_ID, user);
            db.insertWithOnConflict(TABLE_CONTACTS, null, contactValues, SQLiteDatabase.CONFLICT_IGNORE);
        }

        return meetingId;
    }

    //Updates a meeting in the db
    public void updateMeeting(Meeting meeting) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_ID, meeting.id);
        values.put(KEY_TITLE, meeting.title);
        values.put(KEY_LOCATION, meeting.location);
        values.put(KEY_NOTE, meeting.note);
        values.put(KEY_NOTIFICATION, meeting.notification);
        values.put(KEY_START_TIME, meeting.startDateTime.toDate().getTime());
        values.put(KEY_END_TIME, meeting.endDateTime.toDate().getTime());

        Long meetingId = db.insertWithOnConflict(TABLE_MEETINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.delete(TABLE_CONTACTS, KEY_MEETING_ID + " = ?", new String[]{String.valueOf(meeting.id)}); //delete old contacts

        for (String user : meeting.contacts) {
            ContentValues contactValues = new ContentValues();
            contactValues.put(KEY_MEETING_ID, meetingId);
            contactValues.put(KEY_CONTACT_ID, user);
            db.insertWithOnConflict(TABLE_CONTACTS, null, contactValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void deleteMeeting(Meeting meeting) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_MEETINGS, KEY_ID + " = ?", new String[]{String.valueOf(meeting.id)});

        db.delete(TABLE_CONTACTS, KEY_MEETING_ID + " = ?", new String[]{String.valueOf(meeting.id)});
    }
}
