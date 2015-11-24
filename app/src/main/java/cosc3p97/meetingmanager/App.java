package cosc3p97.meetingmanager;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * MeetingManager
 * Created by grahamburgsma on 15-11-10.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
    }
}
