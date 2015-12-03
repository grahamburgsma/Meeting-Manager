package cosc3p97.meetingmanager.views;

import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import cosc3p97.meetingmanager.R;
import cosc3p97.meetingmanager.controllers.MeetingController;
import cosc3p97.meetingmanager.fragments.MeetingFragment;
import cosc3p97.meetingmanager.fragments.MeetingListFragment;
import cosc3p97.meetingmanager.models.Meeting;

public class MainActivity extends AppCompatActivity implements MeetingFragment.Delegate, ViewPager.OnPageChangeListener {

    ViewPager mViewPager;
    SectionsPagerAdapter mSectionsPagerAdapter;
    private FloatingActionButton floatingActionButton;
    private MeetingFragment meetingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        if (savedInstanceState == null) { //keep the same fragment when rotated
            meetingFragment = new MeetingFragment();
            meetingFragment.delegate = this;
        }

        MeetingController.getInstance().setContext(getApplicationContext());
        MeetingController.getInstance().loadMeetings();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMeetingFragment(null);
            }
        });
    }

    @Override
    public void onAttachFragment(android.app.Fragment fragment) {
        if (fragment instanceof MeetingFragment) {
            this.meetingFragment = (MeetingFragment) fragment;
            this.meetingFragment.delegate = this;
        }
    }

    public void showMeetingFragment(Meeting meeting) {
        MeetingListFragment meetingListFragment = (MeetingListFragment) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());

        if (meetingListFragment != null && meetingListFragment.mode != null)
            meetingListFragment.mode.finish();

        if (meeting != null) {
            meetingFragment = MeetingFragment.newInstance(meeting.id);
        } else {
            meetingFragment = new MeetingFragment();
        }

        meetingFragment.delegate = this;

        meetingFragment.show(getFragmentManager(), "dialog");

        floatingActionButton.hide();
    }

    public void hideMeetingFragment() {
        floatingActionButton.show();

        meetingFragment.dismiss();
    }

    @Override
    public void closeFragment() {
        hideMeetingFragment();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        //hides the actionmode if it is shown
        for (int i = 0; i < 3; i++) {
            MeetingListFragment listFragment = (MeetingListFragment) mSectionsPagerAdapter.getItem(i);

            if (listFragment != null && listFragment.mode != null)
                listFragment.mode.finish();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        MeetingListFragment today = MeetingListFragment.newInstance(MeetingListFragment.MeetingListType.MEETING_LIST_TYPE_TODAY);
        MeetingListFragment tomorrow = MeetingListFragment.newInstance(MeetingListFragment.MeetingListType.MEETING_LIST_TYPE_TOMORROW);
        MeetingListFragment all = MeetingListFragment.newInstance(MeetingListFragment.MeetingListType.MEETING_LIST_TYPE_ALL);

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return today;
                case 1:
                    return tomorrow;
                case 2:
                    return all;
                default:
                    return all;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Today";
                case 1:
                    return "Tomorrow";
                case 2:
                    return "All";
            }
            return null;
        }
    }
}
