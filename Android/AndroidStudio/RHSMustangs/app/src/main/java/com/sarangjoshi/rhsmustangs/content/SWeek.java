package com.sarangjoshi.rhsmustangs.content;

import android.text.format.Time;

import java.util.ArrayList;
import java.util.List;

/**
 * This object keeps track of a collection of days, representing a full schedule.
 * Created by Sarang on 4/6/2015.
 */
public class SWeek {
    private List<SDay> mDays;
    //private int currentDay;

    public SWeek() {
        mDays = new ArrayList<>();
    }

    /**
     * Returns a deep copy of this schedule's days.
     *
     * @return a deep copy of this schedule's days.
     */
    public List<SDay> getDays() {
        List<SDay> days = new ArrayList<SDay>();
        for (SDay d : mDays) {
            days.add(d);
        }
        return days;
    }

    /**
     * Gets the day from the schedule given a day of the week.
     *
     * @param dayOfWeek Monday = 1
     */
    public SDay getDay(int dayOfWeek) {
        return mDays.get(getIndex(dayOfWeek));
    }

    /**
     * Given a day-of-week (1-5) gives the corresponding index in the day list. Automatically
     * rounds up for Saturday and Sunday.
     *
     * @param dayOfWeek the day of the week, Monday = 1
     * @return
     */
    private int getIndex(int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 5) {
            return 0;
        }
        return dayOfWeek - 1;
    }

    /**
     * Loads the default schedule based on the default periods as per the {@link SDay} default
     * periods.
     */
    public static SWeek getDefaultWeek() {
        SWeek week = new SWeek();
        for (int i = Time.MONDAY; i <= Time.FRIDAY; i++) {
            week.mDays.add(SDay.getDefaultDay(i));
        }
        return week;
    }
}