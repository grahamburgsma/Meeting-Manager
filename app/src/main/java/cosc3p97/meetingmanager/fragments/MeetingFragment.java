package cosc3p97.meetingmanager.fragments;

/**
 * MeetingManager
 * Created by grahamburgsma on 15-11-09.
 * This is the fragment for creating, viewing and editing a meeting
 */


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.lang.reflect.Method;
import java.util.Calendar;

import cosc3p97.meetingmanager.R;
import cosc3p97.meetingmanager.controllers.MeetingController;
import cosc3p97.meetingmanager.models.Meeting;

public class MeetingFragment extends Fragment implements View.OnClickListener {

    public Delegate delegate;
    EditText editTextTitle, editTextLocation, editTextNote;
    Meeting meeting;
    MutableDateTime startDateTime, endDateTime;
    LinearLayout linearLayoutContacts;
    LayoutInflater inflater;
    private Button buttonStartDate, buttonStartTime, buttonEndDate, buttonEndTime, buttonSave, buttonAddContact, buttonNotification;

    public MeetingFragment() {
    }

    public static MeetingFragment newInstance(Long meetingId) {
        MeetingFragment fragment = new MeetingFragment();
        Bundle args = new Bundle();
        args.putSerializable("meeting_id", meetingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("meeting", meeting);
        outState.putLong("start_time", startDateTime.toDate().getTime());
        outState.putLong("end_time", endDateTime.toDate().getTime());
    }

    //Reset the time
    private void resetMeeting() {
        meeting = new Meeting();

        Calendar calendar = Calendar.getInstance();

        //If time is great that 10pm, set it to the next morning
        if (Integer.parseInt(DateTime.now().hourOfDay().getAsString()) > 21) {
            startDateTime = new MutableDateTime();
            endDateTime = new MutableDateTime();

            startDateTime.addDays(1);
            endDateTime.addDays(1);
            startDateTime.setHourOfDay(8);
            endDateTime.setHourOfDay(9);
            startDateTime.setMinuteOfHour(0);
            endDateTime.setMinuteOfHour(0);
        } else {
            startDateTime = new MutableDateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY) + 1, 0, 0, 0);
            endDateTime = new MutableDateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY) + 2, 0, 0, 0);
        }

        //months are 1 based with Joda Time
        startDateTime.addMonths(1);
        endDateTime.addMonths(1);
    }

    //show the updated dates
    public void refreshDates() {
        buttonStartDate.setText(startDateTime.toString("d MMMM, yyyy"));
        buttonEndDate.setText(endDateTime.toString("d MMMM, yyyy"));
        buttonStartTime.setText(startDateTime.toString("h:mm a"));
        buttonEndTime.setText(endDateTime.toString("h:mm a"));

        //If notification time is sooner than what is possible, set to No Notification
        if (meeting.notification > 0) {
            DateTime nowPlusNotification = new DateTime().plusMinutes(meeting.notification);

            if (startDateTime.isBefore(nowPlusNotification)) {
                meeting.notification = 0;
                refreshNotification();
            }
        }

        //If time is invalid, make red
        if (startDateTime.isBeforeNow()) {
            buttonStartTime.setTextColor(Color.RED);
        } else {
            buttonStartTime.setTextColor(Color.BLACK);
        }
        if (endDateTime.isBefore(startDateTime) || !endDateTime.isAfter(startDateTime)) {
            buttonEndTime.setTextColor(Color.RED);
        } else {
            buttonEndTime.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_notification, menu);

        //Disable notification times what are not possible with current time
        if (new DateTime(startDateTime).plusMinutes(-60).isBeforeNow())
            menu.findItem(R.id.action_notification_60).setEnabled(false);
        if (new DateTime(startDateTime).plusMinutes(-30).isBeforeNow())
            menu.findItem(R.id.action_notification_30).setEnabled(false);
        if (new DateTime(startDateTime).plusMinutes(-15).isBeforeNow())
            menu.findItem(R.id.action_notification_15).setEnabled(false);
    }

    //Notification time floating context menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_notification_none:
                meeting.notification = 0;
                buttonNotification.setText(item.getTitle());
                return true;
            case R.id.action_notification_15:
                meeting.notification = 15;
                buttonNotification.setText(item.getTitle());
                return true;
            case R.id.action_notification_30:
                meeting.notification = 30;
                buttonNotification.setText(item.getTitle());
                return true;
            case R.id.action_notification_60:
                meeting.notification = 60;
                buttonNotification.setText(item.getTitle());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meeting, container, false);
        this.inflater = inflater;

        linearLayoutContacts = (LinearLayout) view.findViewById(R.id.listViewContacts);

        if (getArguments() != null) {
            this.meeting = MeetingController.getInstance().meetings.get(getArguments().getLong("meeting_id"));
            startDateTime = meeting.startDateTime.copy();
            endDateTime = meeting.endDateTime.copy();
        } else if (savedInstanceState != null) { //save on rotation
            this.meeting = (Meeting)savedInstanceState.getSerializable("meeting");
            this.startDateTime = new MutableDateTime(savedInstanceState.getLong("start_time"));
            this.endDateTime = new MutableDateTime(savedInstanceState.getLong("end_time"));

            for (String contact : meeting.contacts) {
                addContactToLinearLayout(contact); //add all contacts
            }
        } else {
            resetMeeting();
        }

        buttonStartDate = (Button) view.findViewById(R.id.buttonStartDate);
        buttonEndDate = (Button) view.findViewById(R.id.buttonEndDate);
        buttonStartTime = (Button) view.findViewById(R.id.buttonStartTime);
        buttonEndTime = (Button) view.findViewById(R.id.buttonEndTime);
        ImageButton buttonExit = (ImageButton) view.findViewById(R.id.buttonCloseFragment);
        buttonSave = (Button) view.findViewById(R.id.buttonSaveMeeting);
        buttonAddContact = (Button) view.findViewById(R.id.buttonAddContact);
        buttonNotification = (Button) view.findViewById(R.id.buttonNotification);

        buttonStartDate.setOnClickListener(this);
        buttonEndDate.setOnClickListener(this);
        buttonStartTime.setOnClickListener(this);
        buttonEndTime.setOnClickListener(this);
        buttonExit.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonAddContact.setOnClickListener(this);
        buttonNotification.setOnClickListener(this);

        registerForContextMenu(buttonNotification);

        editTextTitle = (EditText) view.findViewById(R.id.editTextMeetingTitle);
        editTextLocation = (EditText) view.findViewById(R.id.editTextMeetingLocation);
        editTextNote = (EditText) view.findViewById(R.id.editTextMeetingNote);

        if (this.meeting.id != -1)
            fillForm();

        refreshDates();
        refreshNotification();

        return view;
    }

    //If editing, fill form
    private void fillForm() {
        editTextTitle.setText(meeting.title);
        editTextLocation.setText(meeting.location);
        editTextNote.setText(meeting.note);

        refreshNotification();

        for (String contact : meeting.contacts) {
            addContactToLinearLayout(contact);
        }
    }

    //Set Notification button text
    private void refreshNotification() {
        String notificationString = "";
        if (meeting.notification == 60)
            notificationString = "1 hour before";
        else if (meeting.notification == 30)
            notificationString = "30 minutes before";
        else if (meeting.notification == 15)
            notificationString = "15 minutes before";
        else if (meeting.notification == 0)
            notificationString = "No notification";
        buttonNotification.setText(notificationString);
    }

    //Users linear layout
    private void addContactToLinearLayout(String name) {
        final View viewUser = inflater.inflate(R.layout.list_item_user, null);
        ImageButton buttonRemoveContact = (ImageButton) viewUser.findViewById(R.id.buttonRemoveContact);
        buttonRemoveContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textViewUserName = (TextView) viewUser.findViewById(R.id.textViewUserDisplayName);
                meeting.contacts.remove(textViewUserName.getText().toString());
                linearLayoutContacts.removeView(viewUser);
            }
        });
        TextView textViewUserName = (TextView) viewUser.findViewById(R.id.textViewUserDisplayName);
        textViewUserName.setText(name);
        linearLayoutContacts.addView(viewUser);
    }

    //Save and close
    private void saveMeeting() {
        String title = editTextTitle.getText().toString().trim();
        if (title.length() == 0) {
            editTextTitle.setHint("Title is Required");
            editTextTitle.requestFocus();
        } else if (endDateTime.isAfter(startDateTime) && startDateTime.isAfterNow()) {
            meeting.title = title;
            meeting.location = editTextLocation.getText().toString().trim();
            meeting.note = editTextNote.getText().toString().trim();
            meeting.startDateTime = startDateTime.copy();
            meeting.endDateTime = endDateTime.copy();

            MeetingController.getInstance().createMeeting(meeting);

            closeFragment();
        }
    }

    //Close fragment and cleanup
    public void closeFragment() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        linearLayoutContacts.removeAllViews();
        editTextTitle.getText().clear();
        editTextLocation.getText().clear();
        editTextNote.getText().clear();



                new MutableDateTime(startDateTime.toDate().getTime());


        if (delegate != null)
            delegate.closeFragment();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonAddContact:
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 1);
                break;
            case R.id.buttonSaveMeeting:
                saveMeeting();
                break;
            case R.id.buttonCloseFragment:
                closeFragment();
                break;
            case R.id.buttonNotification:
                view.showContextMenu();
                break;
            case R.id.buttonStartDate:
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        startDateTime.year().set(year);
                        startDateTime.monthOfYear().set(monthOfYear + 1);
                        startDateTime.dayOfMonth().set(dayOfMonth);

                        endDateTime.year().set(year);
                        endDateTime.monthOfYear().set(monthOfYear + 1);
                        endDateTime.dayOfMonth().set(dayOfMonth);
                        refreshDates();
                    }
                }, startDateTime.getYear(), startDateTime.getMonthOfYear() - 1, startDateTime.getDayOfMonth()).show();
                break;
            case R.id.buttonStartTime:
                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startDateTime.hourOfDay().set(hourOfDay);
                        startDateTime.minuteOfHour().set(minute);
                        refreshDates();
                    }
                }, startDateTime.getHourOfDay(), startDateTime.getMinuteOfHour(), false).show();
                break;
            case R.id.buttonEndTime:
                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endDateTime.hourOfDay().set(hourOfDay);
                        endDateTime.minuteOfHour().set(minute);
                        refreshDates();
                    }
                }, endDateTime.getHourOfDay(), endDateTime.getMinuteOfHour(), false).show();
                break;
        }
    }

    //Gets the result from the contacts app
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getActivity().getContentResolver().query(contactData, null, null, null, null);
                    assert c != null;
                    if (c.moveToFirst()) {
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        if (!meeting.contacts.contains(name)) {
                            meeting.contacts.add(name);

                            addContactToLinearLayout(name);
                        }
                    }
                    c.close();
                }
                break;
        }
    }

    public interface Delegate {
        void closeFragment();
    }
}
