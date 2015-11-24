package cosc3p97.meetingmanager.models;

import org.joda.time.MutableDateTime;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * MeetingManager
 * Created by grahamburgsma on 15-11-09.
 */
public class Meeting implements Serializable {

    public long id = -1;
    public MutableDateTime startDateTime, endDateTime; //Joda time - super handy
    public Set<String> contacts = new HashSet<>();
    public String note, title, location;
    public int notification = 0;

    public Meeting() {
    }
}
