package com.sarangjoshi.rhsmustangs.schedule;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sarangjoshi.rhsmustangs.R;
import com.sarangjoshi.rhsmustangs.content.*;
import com.sarangjoshi.rhsmustangs.helper.SHelper;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Sarang
 */
public class ScheduleFragment extends Fragment implements SSchedule.UpdateFinishedListener,
        UpdatedDaysFragment.UpdatedDaySelectedListener {
    private static final String UPDATED_DAYS_TAG = "UpdatedDaysFragment";

    private SSchedule mSchedule;

    private ScheduleAdapter mAdapter;

    private ListView mPeriodsList;
    private TextView mTitle, mDayOfWeek;
    private ImageButton mNextDay;
    private Spinner groupSpin;

    private ProgressDialog dialog;

    /**
     * Default empty constructor.
     */
    public ScheduleFragment() {
    }

    /**
     * Initializes a new instance of ScheduleFragment.
     */
    public static ScheduleFragment newInstance() {
        return new ScheduleFragment();
    }

    // FRAGMENT OVERRIDES

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mSchedule = new SSchedule(SHelper.getActualToday(), this, getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_schedule, container, false);

        // Initialize views
        mPeriodsList = (ListView) v.findViewById(R.id.periodsListView);
        mTitle = (TextView) v.findViewById(R.id.title);
        mDayOfWeek = (TextView) v.findViewById(R.id.scheduleDayOfWeek);
        ImageButton mPrevDay = (ImageButton) v.findViewById(R.id.previousDay);
        mNextDay = (ImageButton) v.findViewById(R.id.nextDay);

        // UI dynamic setup
        View.OnClickListener dcl = new DayChangeClickListener();
        mPrevDay.setOnClickListener(dcl);
        mNextDay.setOnClickListener(dcl);

        View.OnClickListener tcl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToday(SHelper.getActualToday());
            }
        };
        mTitle.setOnClickListener(tcl);
        mDayOfWeek.setOnClickListener(tcl);

        // And finally actually show data
        //refreshPeriods();
        //updateSpinner();

        // Load updated days
        new LoadUpdatedDaysAsyncTask(getActivity()).execute();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.schedule, menu);

        MenuItem item = menu.findItem(R.id.group_spinner);
        groupSpin = (Spinner) MenuItemCompat.getActionView(item);

        updateSpinner();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                return refreshUpdatedDays();
            case R.id.action_see_updated_days:
                return showUpdatedDays();
            case R.id.action_save_updated_days:
                new SaveUpdatedDaysAsyncTask(getActivity()).execute();
                return true;
            case R.id.action_clear_updated_days:
                mSchedule.clearDatabase();
                return true;
            case R.id.action_load_data:
                new LoadUpdatedDaysAsyncTask(getActivity()).execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // PERIODS

    /**
     * Sets the current day to the given Calendar date.
     *
     * @param today
     */
    private void setToday(Calendar today) {
        mSchedule.setToday(today);
        refreshPeriods();
        updateSpinner();
    }

    /**
     * Updates the spinner.
     */
    private void updateSpinner() {
        if (groupSpin != null) {
            // Retrieve data
            String[] spinnerData = mSchedule.getToday().getGroupNames();

            if (spinnerData == null)
                spinnerData = SDay.NO_GROUPS;

            ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, spinnerData);
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            groupSpin.setAdapter(spinAdapter);

            groupSpin.setOnItemSelectedListener(new GroupSpinnerListener());

            if (mSchedule.getToday().hasGroups())
                groupSpin.setSelection(mSchedule.getGroupN() - 1);
            else
                groupSpin.setSelection(SSchedule.DEFAULT_GROUP_N - 1);
        }
    }

    private class GroupSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (mSchedule.setGroupN(position + 1)) {
                //Toast.makeText(getActivity(), "" + (position + 1), Toast.LENGTH_SHORT).show();
                mSchedule.saveGroupN();
                refreshPeriods();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // do nothing!
        }
    }

    /**
     * Refreshes the periods.
     */
    private void refreshPeriods() {
        // Updates adapter to reflect changes
        if (mAdapter == null)
            mAdapter = new ScheduleAdapter(getActivity());
        mAdapter.updateData();
        mPeriodsList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        // Update other UI
        mTitle.setText(mSchedule.getTodayAsString());
        mDayOfWeek.setText(mSchedule.getToday().getDayOfWeekAsString());
    }

    private class DayChangeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Actual shifting - result can be captured to note week shift
            mSchedule.shiftTodayBy((v.getId() == mNextDay.getId()) ? 1 : -1);

            // Updating
            refreshPeriods();
            updateSpinner();
        }
    }

    // VIEWS

    /**
     * Sets the text color of the given views.
     *
     * @param c the color
     */
    private void setTextColor(int c, TextView... views) {
        for (TextView v : views) {
            v.setTextColor(c);
        }
    }

    /**
     * Private class to adapt a list of periods.
     */
    private class ScheduleAdapter extends ArrayAdapter<SPeriod> {
        private final Context mContext;

        public ScheduleAdapter(Context context) {
            super(context, R.layout.layout_period);
            mContext = context;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView;
            TextView periodNumView, classNameView, startTimeView, endTimeView;

            rowView = inflater.inflate(R.layout.layout_period, parent,
                    false);

            // Individual views
            periodNumView = (TextView) rowView.findViewById(R.id.periodNum);
            classNameView = (TextView) rowView.findViewById(R.id.className);
            startTimeView = (TextView) rowView.findViewById(R.id.startTime);
            endTimeView = (TextView) rowView.findViewById(R.id.endTime);

            //}

            // Setting view data
            SPeriod p = getItem(pos);

            periodNumView.setText(new String(p.getShort()));
            classNameView.setText(p.getClassName());

            boolean is24hr = false; //PreferenceManager.getDefaultSharedPreferences(SActivity.this).getBoolean(SettingsFragment.IS24HR_KEY,                    true);
            startTimeView.setText(p.getTimeAsString(SPeriod.TimeStyle.START, is24hr));
            endTimeView.setText(p.getTimeAsString(SPeriod.TimeStyle.END, is24hr));

            setRelativeColors(p, periodNumView, classNameView, startTimeView, endTimeView);

            return rowView;
        }

        /**
         * Given the current period, sets the correct colors for the given views.
         *
         * @param p
         * @param views
         */
        private void setRelativeColors(SPeriod p, TextView... views) {
            int relTime = -1;

            Calendar now = new GregorianCalendar();

            int day = mSchedule.getToday().getDayOfWeek();

            int absDiff = SHelper.getAbsDifference(mSchedule.getTodayAsCalendar(), new GregorianCalendar());
            if (absDiff != 0)
                relTime = absDiff;
                // Present day
            else if (day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
                //if (p.getEnd().compareTo(schedNow) < 0) {
                //    relTime = -1;
                //} else
                if ((p.getStart().compareTo(now) <= 0)
                        && (p.getEnd().compareTo(now) >= 0)) {
                    relTime = 0;
                } else if (p.getStart().compareTo(now) > 0) {
                    relTime = 1;
                }
            }

            // Colors stuff based on time
            int color = Color.GRAY; // relTime < 0
            if (relTime == 0) {
                color = Color.rgb(255, 215, 0);
            } else if (relTime > 0) {
                color = Color.BLACK;
            }

            setTextColor(color, views);
        }

        public void updateData() {
            super.clear();
            super.addAll(mSchedule.getTodayPeriods());
        }
    }

    // UPDATED DAYS

    /**
     * Refreshes updated days.
     *
     * @return success
     */
    private boolean refreshUpdatedDays() {
        dialog = ProgressDialog.show(getActivity(), "",
                "Checking for updates...");

        mSchedule.updateUpdatedDays();
        return true;
    }

    /**
     * Shows the updated days in a dialog
     *
     * @return success
     */
    private boolean showUpdatedDays() {
        UpdatedDaysFragment dialog =
                new UpdatedDaysFragment(mSchedule.getUpdatedDays(), this);
        dialog.show(getFragmentManager(), UPDATED_DAYS_TAG);
        return true;
    }

    /**
     * This is run when the update is completed.
     */
    @Override
    public void updateCompleted() {
        dialog.dismiss();

        refreshPeriods();
        updateSpinner();

        // Automatically saves downloaded updated days
        new SaveUpdatedDaysAsyncTask(getActivity()).execute();
        showUpdatedDays();
    }

    @Override
    public void updatedDaySelected(int index) {
        //Toast.makeText(getActivity(), "" + index, Toast.LENGTH_SHORT).show();

        SUpdatedDay day = mSchedule.getUpdatedDays().get(index);
        setToday(day.getDate());
    }

    private class LoadUpdatedDaysAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context mCtx;
        private ProgressDialog pd;

        public LoadUpdatedDaysAsyncTask(Context ctx) {
            mCtx = ctx;
        }

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(mCtx, "",
                    "Loading...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            mSchedule.loadDataFromDatabase();
            mSchedule.refreshWeek(mSchedule.getTodayAsCalendar());

            refreshPeriods();
            updateSpinner();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
        }
    }

    private class SaveUpdatedDaysAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context mCtx;
        private ProgressDialog pd;

        public SaveUpdatedDaysAsyncTask(Context ctx) {
            mCtx = ctx;
        }

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(mCtx, "",
                    "Saving to database...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Clears database before saving
            mSchedule.clearDatabase();
            mSchedule.saveUpdatedDays();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
        }
    }
}
