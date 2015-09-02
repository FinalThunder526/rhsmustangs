package com.sarangjoshi.rhsmustangs.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sarangjoshi.rhsmustangs.MainActivity;
import com.sarangjoshi.rhsmustangs.R;
import com.sarangjoshi.rhsmustangs.content.SDay;
import com.sarangjoshi.rhsmustangs.content.SHoliday;
import com.sarangjoshi.rhsmustangs.content.SPeriod;
import com.sarangjoshi.rhsmustangs.content.SSchedule;
import com.sarangjoshi.rhsmustangs.content.SUpdatedDay;
import com.sarangjoshi.rhsmustangs.helper.SHelper;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Sarang
 */
public class ScheduleFragment extends Fragment implements SSchedule.UpdateFinishedListener,
        SSchedule.BaseDayUpdateFinishedListener {
    private static final String PREF_INITIALIZED = "schedule_initialized";
    private SSchedule mSchedule;

    private ScheduleAdapter mAdapter;

    private ListView mPeriodsList;
    private TextView mTitle, mDayOfWeek;
    private ImageButton mNextDay;
    private Spinner mGroupSpinner;

    private ProgressDialog mDialog;

    /**
     * Whether the schedule has been initialized.
     */
    private boolean mInitialized;

    /**
     * Default empty constructor.
     */
    public ScheduleFragment() {
    }

    public static ScheduleFragment newInstance(int pos) {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putInt(MainActivity.ARG_SECTION_NUMBER, pos);
        fragment.setArguments(args);
        return fragment;
    }

    // FRAGMENT OVERRIDES

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mSchedule = new SSchedule(SHelper.getActualToday(), this, this, getActivity());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mInitialized = sp.getBoolean(PREF_INITIALIZED, false);
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
        ImageButton mHoliday = (ImageButton) v.findViewById(R.id.nextHoliday);

        // UI dynamic setup
        // Change days
        View.OnClickListener dcl = new DayChangeClickListener();
        mPrevDay.setOnClickListener(dcl);
        mNextDay.setOnClickListener(dcl);

        // Jump to holiday
        mHoliday.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSchedule.goToNextHoliday()) {
                            refreshPeriods();
                            updateSpinner();
                        } else {
                            Toast.makeText(getActivity(), "No holidays coming up.", Toast.LENGTH_LONG).show();
                        }
                    }
                }

        );

        // Jump to today
        View.OnClickListener tcl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToday(SHelper.getActualToday());
            }
        };
        mTitle.setOnClickListener(tcl);
        mDayOfWeek.setOnClickListener(tcl);

        // Notes
        mPeriodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showPeriodNote(position);
            }
        });

        // Load all saved data
        new LoadDataAsyncTask(getActivity(), true).execute();

        // Initialize for first use
        if (!mInitialized) {
            fetchBaseDays();
        }

        return v;
    }

    public void onStart() {
        super.onStart();

        refreshPeriods();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.schedule, menu);

        MenuItem item = menu.findItem(R.id.group_spinner);
        mGroupSpinner = (Spinner) MenuItemCompat.getActionView(item);

        updateSpinner();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                return fetchUpdates();
            case R.id.action_see_updated_days:
                return showUpdatedDays();
            case R.id.action_see_holidays:
                return showHolidays();

            /*case R.id.action_save_updated_days:
                new SaveUpdatedDaysAsyncTask(getActivity()).execute();
                return true;
            case R.id.action_clear_updated_days:
                mSchedule.clearUpdates();
                return true;
            case R.id.action_load_data:
                new LoadDataAsyncTask(getActivity()).execute();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(MainActivity.ARG_SECTION_NUMBER));
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
        if (mGroupSpinner != null) {
            // Retrieve data
            String[] spinnerData = mSchedule.getToday().getGroupNames();

            if (spinnerData == null)
                spinnerData = SDay.NO_GROUPS;

            ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, spinnerData);
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mGroupSpinner.setAdapter(spinAdapter);

            mGroupSpinner.setOnItemSelectedListener(new GroupSpinnerListener());

            if (mSchedule.getToday().hasGroups())
                mGroupSpinner.setSelection(mSchedule.getGroupN() - 1);
            else
                mGroupSpinner.setSelection(SSchedule.DEFAULT_GROUP_N - 1);
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
        updateUI();
    }

    /**
     * Updates the UI of the screen.
     */
    private void updateUI() {
        mTitle.setText(mSchedule.getTodayAsString());
        mDayOfWeek.setText(mSchedule.getToday().getDayOfWeekAsString());

        // Makes the title green/yellow and bold
        if (mSchedule.getToday().getClass() == SUpdatedDay.class) {
            SHelper.setTextColor(getResources().getColor(R.color.dark_green), mTitle, mDayOfWeek);
            mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (mSchedule.getHoliday() != null) {
            SHelper.setTextColor(getResources().getColor(R.color.gold), mTitle, mDayOfWeek);
            mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            SHelper.setTextColor(Color.BLACK, mTitle, mDayOfWeek);
            mTitle.setTypeface(Typeface.DEFAULT);
        }
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

    /**
     * Shows the note of a chosen period.
     *
     * @param pos the position of the period in the visible periods
     */
    private void showPeriodNote(int pos) {
        String note = mSchedule.getToday().getPeriod(pos).getNote();
        if (note == null) {
            Toast.makeText(getActivity(), "No note.", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(note)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    });

            builder.create().show();
        }
    }

    // VIEWS

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
            TextView shortNameView, classNameView, startTimeView, endTimeView;

            rowView = inflater.inflate(R.layout.layout_period, parent,
                    false);

            // Individual views
            shortNameView = (TextView) rowView.findViewById(R.id.shortName);
            classNameView = (TextView) rowView.findViewById(R.id.className);
            startTimeView = (TextView) rowView.findViewById(R.id.startTime);
            endTimeView = (TextView) rowView.findViewById(R.id.endTime);

            TextView[] allTextViews = {shortNameView, classNameView, startTimeView, endTimeView};

            //}

            // Setting view data
            SPeriod p = getItem(pos);

            shortNameView.setText(p.getShort());
            classNameView.setText(p.getClassName());

            // Times
            boolean is24hr = false; //PreferenceManager.getDefaultSharedPreferences(SActivity.this).getBoolean(SettingsFragment.IS24HR_KEY,                    true);
            startTimeView.setText(p.getTimeAsString(SPeriod.TimeStyle.START, is24hr));
            endTimeView.setText(p.getTimeAsString(SPeriod.TimeStyle.END, is24hr));

            // Holiday coloring
            if (mSchedule.getHoliday() != null) {
                SHelper.setTextColor(getResources().getColor(R.color.gold), allTextViews);
            } else {
                setRelativeColors(p, allTextViews);
            }

            // Note
            if (p.getNote() != null)
                SHelper.setTextColor(getResources().getColor(android.R.color.holo_blue_bright),
                        shortNameView, classNameView);

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

            int absDiff = SHelper.compareAbsDays(mSchedule.getTodayAsCalendar(),
                    SHelper.getActualToday());
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
                color = getResources().getColor(R.color.gold);
            } else if (relTime > 0) {
                color = Color.BLACK;
            }

            SHelper.setTextColor(color, views);
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
    private boolean fetchUpdates() {
        mDialog = ProgressDialog.show(getActivity(), "",
                "Checking for updates...");

        mSchedule.updateUpdatedDays();
        return true;
    }

    /**
     * This is run when the update is completed.
     */
    @Override
    public void updateFetchCompleted() {
        refreshPeriods();
        updateSpinner();

        mDialog.dismiss();

        if(!mInitialized) {
            mInitialized = true;

            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit().putBoolean(PREF_INITIALIZED, true).commit();
        }

        // Automatically saves downloaded updated days
        new SaveUpdatedDaysAsyncTask(getActivity(), true).execute();
        showUpdatedDays();
    }

    /**
     * Shows the updated days in a dialog
     *
     * @return success
     */
    private boolean showUpdatedDays() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Updated Days");

        if (mSchedule.getUpdatedDays().size() > 0) {
            ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item,
                    mSchedule.getUpdatedDays());
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updatedDaySelected(which);
                }
            });
        } else builder.setMessage(getString(R.string.no_updated_days));

        builder.create().show();

        return true;
    }

    /**
     * Goes to the oliday at the given index in the list of holidays.
     */
    public void updatedDaySelected(int index) {
        SUpdatedDay day = mSchedule.getUpdatedDays().get(index);
        setToday(day.getDate());
    }

    // HOLIDAYS

    /**
     * Shows the holidays in a dialog
     *
     * @return success
     */
    private boolean showHolidays() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                mSchedule.getHolidays());
        builder.setTitle("Updated Days")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        // Goes to selected holiday
                        holidaySelected(index);
                    }
                });

        return true;
    }

    /**
     * Goes to the oliday at the given index in the list of holidays.
     */
    public void holidaySelected(int index) {
        SHoliday day = mSchedule.getHolidays().get(index);
        setToday(day.getStart());
    }

    // BASE DAYS

    private boolean fetchBaseDays() {
        mDialog = ProgressDialog.show(getActivity(), "", "Checking for base day updates...");

        mSchedule.updateBaseDays();

        return true;
    }

    @Override
    public void baseDayFetchCompleted() {
        mSchedule.clearBaseDays();
        mSchedule.saveBaseDays();

        mDialog.dismiss();
        if (!mInitialized) {
            fetchUpdates();
        } else {
            mSchedule.refreshWeek(mSchedule.getTodayAsCalendar());

            refreshPeriods();
            updateSpinner();
        }
    }

    // ASYNC TASKS

    /**
     * Personal implementation of Async Task for simple background tasks.
     *
     * @author Sarang
     */
    private abstract class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        protected Context mCtx;
        protected ProgressDialog mPd;
        protected boolean mShowLoading;

        public MyAsyncTask(Context ctx, boolean showLoading) {
            mCtx = ctx;
            mShowLoading = showLoading;
        }

        @Override
        protected void onPreExecute() {
            mPd = ProgressDialog.show(mCtx, "", "Loading...");
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mShowLoading)
                mPd.dismiss();
        }
    }

    private class LoadDataAsyncTask extends MyAsyncTask {

        public LoadDataAsyncTask(Context ctx, boolean showLoading) {
            super(ctx, showLoading);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mSchedule.loadUpdates();
            mSchedule.loadBaseDays();
            mSchedule.refreshWeek(mSchedule.getTodayAsCalendar());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            refreshPeriods();
            updateSpinner();
        }
    }

    private class SaveUpdatedDaysAsyncTask extends MyAsyncTask {
        public SaveUpdatedDaysAsyncTask(Context ctx, boolean showLoading) {
            super(ctx, showLoading);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Clears database before saving
            mSchedule.clearUpdates();
            mSchedule.saveUpdates();
            return null;
        }
    }
}