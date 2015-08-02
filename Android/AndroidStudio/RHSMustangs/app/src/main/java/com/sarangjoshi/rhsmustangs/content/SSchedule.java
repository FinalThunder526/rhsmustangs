package com.sarangjoshi.rhsmustangs.content;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.sarangjoshi.rhsmustangs.schedule.SStatic;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a schedule object.
 *
 * @author Sarang
 */
public class SSchedule {
    // TODO: Decide whether a list of weeks is needed
    //private List<SWeek> mLoadedWeeks;
    private SWeek mCurrentWeek;
    private Calendar mToday;
    private int mGroupN;

    private List<SUpdatedDay> mUpdatedDays;
    private UpdateFinishedListener finishedListener;

    /**
     * Constructs a new {@link SSchedule} object.
     *
     * @param today
     * @param groupN
     * @param l
     */
    public SSchedule(Calendar today, int groupN, UpdateFinishedListener l) {
        mGroupN = groupN;
        mUpdatedDays = new LinkedList<>();
        this.finishedListener = l;

        // Then, set the current day within that week.
        setToday(today);

        // First, update the week to reflect the current day.
        updateWeek(today);
    }

    /**
     * Gets the current day.
     */
    public SDay getToday() {
        return mCurrentWeek.getDay(mToday.get(Calendar.DAY_OF_WEEK));//mToday;
    }

    /**
     * Sets the current group number.
     *
     * @param groupN the group number
     * @throws IllegalArgumentException if the group number is 0 and there are groups in the current
     *                                  day
     * @returns whether the group number was actually updated
     */
    public boolean setGroupN(int groupN) {
        if (getToday().hasGroups()) {
            if (groupN == SPeriod.BASE_GROUPN)
                throw new IllegalArgumentException();
            if (getToday().getClass() == SUpdatedDay.class) {
                return ((SUpdatedDay) getToday()).setGroupN(groupN);
            } else if (this.mGroupN != groupN) {
                this.mGroupN = groupN;
                return true;
            }
        }
        return false;
    }

    /**
     * Gets today's periods with the previously set group number.
     *
     * @return
     */
    public List<SPeriod> getTodayPeriods() {
        return getToday().getPeriods(getGroupN());
    }

    /**
     * Gets today as a {@link Calendar} object.
     *
     * @return the current day
     */
    public Calendar getTodayAsCalendar() {
        return mToday;
    }

    /**
     * Gets a list of updated days.
     *
     * @return a list of updated days
     */
    public List<SUpdatedDay> getUpdatedDays() {
        return mUpdatedDays;
    }

    /**
     * Gets the currently selected group number.
     *
     * @return
     */
    public int getGroupN() {
        if (getToday().getClass() == SUpdatedDay.class)
            return ((SUpdatedDay) getToday()).getGroupN();
        else
            return mGroupN;
    }

    /**
     * @return
     */
    public String getTodayAsString() {
        // TODO: fix
        /*Calendar now = new GregorianCalendar();
        int diff = SStatic.getAbsDay(now) - SStatic.getAbsDay(mToday); //now.compareTo(mToday);
        if (diff == 0)
            return "Today";
        else if (diff == -1)
            return "Tomorrow";
        else if (diff == 1)
            return "Yesterday";*/
        return SStatic.getDisplayString(mToday);
    }

    /**
     * Gets a String representation of today's day of the week.
     *
     * @return String representation of today's day of the week
     */
    public String getTodayDayOfWeekAsString() {
        return getToday().getDayOfWeekAsString();
    }

    /**
     * Sets today for the given day.
     *
     * @param today
     * @return if the week changed
     */
    public void setToday(Calendar today) {
        Calendar oldToday = mToday;
        mToday = today;

        // Check for Saturday/Sunday overflow
        boolean weekChanged = updateDay(true);

        // Check to see if the week has been completely changed
        if (oldToday != null)
            weekChanged |= !SStatic.sameWeek(oldToday, mToday);

        // If week changed, change the damn week
        if (weekChanged) updateWeek(mToday);
    }

    /**
     * Goes through MONDAY to FRIDAY and sets up the current week, overriding days that have
     * updates.
     */
    private void updateWeek(Calendar today) {
        // TODO: make more efficient by not changing week appropriately
        // Based on today, establish MONDAY
        mCurrentWeek = SWeek.getDefaultWeek();
        mCurrentWeek.update(today, mUpdatedDays);
    }

    /**
     * Shifts the current day of the week by a single day, forward or backward.
     *
     * @return whether the week was changed
     */
    public void shiftTodayBy(int n) {
        // Shift the actual date
        mToday.add(Calendar.DAY_OF_MONTH, n);

        if (updateDay(n >= 0))
            updateWeek(mToday);
    }

    /**
     * ALWAYS call this when mToday is changed.
     *
     * @param isForward which direction the day was changed
     * @return if the week was changed
     */
    private boolean updateDay(boolean isForward) {
        // Week changing algorithm
        boolean weekChanged = false;
        // If there's a change in week, update the current day and week
        if (isForward) {
            if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                mToday.add(Calendar.DAY_OF_MONTH, 2);
                weekChanged = true;
            } else if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                mToday.add(Calendar.DAY_OF_MONTH, 1);
                weekChanged = true;
            }
        } else {
            if (mToday.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                mToday.add(Calendar.DAY_OF_MONTH, -2);
                weekChanged = true;
            }
        }

        return weekChanged;
    }

    /**
     * Updates the updated days list.
     */
    public void updateUpdatedDays() {
        mUpdatedDays.clear();

        addUpdatedDay(SUpdatedDay.test(new GregorianCalendar(2015, Calendar.AUGUST, 5),
                new String[]{"Seniors", "Juniors", "Other Lowly Beings"},
                new SPeriod("1", 7, 30, 8, 30, 0),
                new SPeriod("2", 9, 30, 18, 30, 3)));
        addUpdatedDay(SUpdatedDay.test1());
        addUpdatedDay(SUpdatedDay.test2());
        addUpdatedDay(SUpdatedDay.test3());

        updateWeek(mToday);

        finishedListener.updateCompleted();

        /*
        ParseQuery<ParseObject> updatedDaysQuery = ParseQuery.getQuery(SUpdatedDay.UPDATED_DAY_CLASS);
        updatedDaysQuery.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> updatedDays, ParseException e) {
                if(e == null) {
                    setUpdatedDaysFromParse(updatedDays);
                    finishedListener.refreshCompleted();
                } else {
                    // TODO: handle
                }
            }
        });*/
    }

    /**
     * Adds a new {@link SUpdatedDay} in a sorted location.
     *
     * @param day
     */
    private void addUpdatedDay(SUpdatedDay day) {
        int i;
        for (i = 0; i < mUpdatedDays.size(); i++) {
            int d = day.compareTo(mUpdatedDays.get(i));
            if (d < 0) {
                break;
            }
        }
        mUpdatedDays.add(i/*(i - 1 < 0) ? 0 : i - 1*/, day);
    }

    private void setUpdatedDaysFromParse(List<ParseObject> dayObjects) {
        mUpdatedDays.clear();
        //for(ParseObject obj : dayObjects) {
        final ParseObject obj = dayObjects.get(0);
        ParseRelation<ParseObject> periods = obj.getRelation(SUpdatedDay.PERIODS_KEY);
        periods.getQuery().findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> results, ParseException e) {
                if (e == null) {
                    mUpdatedDays.add(SUpdatedDay.newFromParse(obj, results));
                } else {
                    // TODO: handle
                }
            }
        });
        //}
    }

    /**
     * Listener for when updates finish.
     */
    public interface UpdateFinishedListener {
        void updateCompleted();
    }
}
