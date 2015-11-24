package cosc3p97.meetingmanager.fragments;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cosc3p97.meetingmanager.R;
import cosc3p97.meetingmanager.adapters.MeetingListAdapter;
import cosc3p97.meetingmanager.controllers.MeetingController;
import cosc3p97.meetingmanager.models.Meeting;
import cosc3p97.meetingmanager.views.MainActivity;

/**
 * MeetingManager
 * Created by grahamburgsma on 15-11-09.
 */


public class MeetingListFragment extends Fragment implements AbsListView.MultiChoiceModeListener, AdapterView.OnItemClickListener {

    public ActionMode mode;
    List<Meeting> meetings;
    MeetingListAdapter adapter;
    MeetingListType listType;
    ListView listView;
    TextView textViewNoMeeting;
    Button buttonShowPrevious;
    Boolean showAll = false;
    ArrayList<Integer> selectedIndex = new ArrayList<>();

    public MeetingListFragment() {
    }

    public static MeetingListFragment newInstance(MeetingListType listType) {
        MeetingListFragment fragment = new MeetingListFragment();
        Bundle args = new Bundle();
        args.putSerializable("list_type", listType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (checked)
            selectedIndex.add(position);
        else
            selectedIndex.remove(Integer.valueOf(position));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.main_action_menu, menu);
        this.mode = mode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                deleteSelected(mode);
                return true;
            case R.id.action_select_all:
                selectedIndex.clear();
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, true);
                }
                return true;
            case R.id.action_move:
                moveToDate(mode);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        selectedIndex.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        listType = (MeetingListType) getArguments().getSerializable("list_type");

        meetings = new ArrayList<>();
        adapter = new MeetingListAdapter(getActivity(), 0, meetings, listType);

        textViewNoMeeting = (TextView) view.findViewById(R.id.textViewNoMeetings);
        buttonShowPrevious = (Button) view.findViewById(R.id.buttonShowPreviousMeetings);
        buttonShowPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAll = !showAll;
                updateList();
            }
        });

        listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
        listView.setOnItemClickListener(this);
        updateList();

        //Register for the event of a meeting being created, then refresh list
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateList();
            }
        }, new IntentFilter("meeting_created"));

        return view;
    }

    @UiThread
    private void updateList() {
        meetings.clear();

        switch (listType) {
            case MEETING_LIST_TYPE_TODAY:
                textViewNoMeeting.setText("No Meetings Today");
                meetings.addAll(MeetingController.getInstance().getMeetingsToday());
                break;
            case MEETING_LIST_TYPE_TOMORROW:
                textViewNoMeeting.setText("No Meetings Tomorrow");
                meetings.addAll(MeetingController.getInstance().getMeetingsTomorrow());
                break;
            case MEETING_LIST_TYPE_ALL:
                textViewNoMeeting.setText("No Meetings");
                meetings.addAll(MeetingController.getInstance().meetings.values());
                break;
        }

        List<Meeting> deleteList = new ArrayList<>();

        for (Meeting meeting : meetings) {
            if (meeting.endDateTime.isBeforeNow())
                deleteList.add(meeting);
        }


        if (!showAll) {
            meetings.removeAll(deleteList);

            if (deleteList.size() == 0)
                buttonShowPrevious.setVisibility(View.GONE);
            else {
                buttonShowPrevious.setVisibility(View.VISIBLE);
                buttonShowPrevious.setText("Show Previous Meetings");
            }
        } else {
            if (deleteList.size() == 0)
                buttonShowPrevious.setVisibility(View.GONE);
            else {
                buttonShowPrevious.setVisibility(View.VISIBLE);
                buttonShowPrevious.setText("Hide Previous Meetings");
            }
        }


        Collections.sort(meetings, new Comparator<Meeting>() {
            @Override
            public int compare(Meeting lhs, Meeting rhs) {
                return lhs.startDateTime.compareTo(rhs.startDateTime);
            }
        });

        adapter.notifyDataSetChanged();

        if (meetings.size() == 0)
            textViewNoMeeting.setVisibility(View.VISIBLE);
        else
            textViewNoMeeting.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.showMeetingFragment(adapter.getItem(position));
    }

    private void moveToDate(final ActionMode mode) {
        Meeting meetingTemp = adapter.getItem(selectedIndex.get(0));
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                for (int i = 0; i < selectedIndex.size(); i++) {
                    Meeting meeting = adapter.getItem(i);
                    meeting.startDateTime.setYear(year);
                    meeting.startDateTime.setMonthOfYear(monthOfYear + 1);
                    meeting.startDateTime.setDayOfMonth(dayOfMonth);

                    meeting.endDateTime.setYear(year);
                    meeting.endDateTime.setMonthOfYear(monthOfYear + 1);
                    meeting.endDateTime.setDayOfMonth(dayOfMonth);

                    MeetingController.getInstance().createMeeting(meeting);
                }
                mode.finish();
            }
        }, meetingTemp.startDateTime.getYear(), meetingTemp.startDateTime.getMonthOfYear() - 1, meetingTemp.startDateTime.getDayOfMonth()).show();
    }

    private void deleteSelected(final ActionMode mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete Selected Meetings?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (Integer index : selectedIndex) {
                    MeetingController.getInstance().deleteMeeting(adapter.getItem(index));
                }
                mode.finish();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public enum MeetingListType implements Serializable {
        MEETING_LIST_TYPE_TODAY, MEETING_LIST_TYPE_TOMORROW, MEETING_LIST_TYPE_ALL
    }
}
