package cosc3p97.meetingmanager.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cosc3p97.meetingmanager.R;
import cosc3p97.meetingmanager.fragments.MeetingListFragment;
import cosc3p97.meetingmanager.models.Meeting;

/**
 * MeetingManager
 * Created by grahamburgsma on 15-11-09.
 * ArrayAdapter for the main list of meetings (MainListFragment)
 */
public class MeetingListAdapter extends ArrayAdapter<Meeting> {

    CardView cardView;
    TextView textViewTitle, textViewLocation, textViewStartTime, textViewTimeHour, textViewTimeHalf;
    MeetingListFragment.MeetingListType listType;

    public MeetingListAdapter(Context context, int resource, List<Meeting> objects, MeetingListFragment.MeetingListType listType) {
        super(context, resource, objects);
        this.listType = listType;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Meeting meeting = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_meeting, parent, false);
        }

        cardView = (CardView) convertView.findViewById(R.id.cardViewMeeting);
        textViewTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
        textViewLocation = (TextView) convertView.findViewById(R.id.textViewLocation);
        textViewStartTime = (TextView) convertView.findViewById(R.id.textViewStartTime);
        textViewTimeHour = (TextView) convertView.findViewById(R.id.textViewTimeHour);
        textViewTimeHalf = (TextView) convertView.findViewById(R.id.textViewTimeHalf);

        textViewTitle.setText(meeting.title);

        if (meeting.location.length() > 0) {
            textViewLocation.setText(meeting.location);
            textViewLocation.setVisibility(View.VISIBLE);
        } else
            textViewLocation.setVisibility(View.GONE);

        String timeString = meeting.startDateTime.toString("h:mm a") + " - " + meeting.endDateTime.toString("h:mm a");
        textViewStartTime.setText(timeString);

        if (listType != MeetingListFragment.MeetingListType.MEETING_LIST_TYPE_ALL) {
            textViewTimeHour.setText(meeting.startDateTime.toString("h"));
            textViewTimeHalf.setText(meeting.startDateTime.toString("a"));
        } else {
            textViewTimeHour.setText(meeting.startDateTime.toString("d"));
            textViewTimeHalf.setText(meeting.startDateTime.toString("MMM"));
        }


        return convertView;
    }
}
