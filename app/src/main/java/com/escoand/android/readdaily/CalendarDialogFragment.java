/*
 * Copyright (c) 2016 escoand.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.escoand.android.readdaily;

import android.app.Dialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;

public class CalendarDialogFragment extends DialogFragment implements com.prolificinteractive.materialcalendarview.OnDateSelectedListener {
    private final HashSet<Integer> datesAvailable = new HashSet<>();
    private final HashSet<Integer> datesRead = new HashSet<>();
    private OnDateSelectedListener listener;
    private ProgressBar progress;
    private MaterialCalendarView cal;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FrameLayout v = new FrameLayout(getContext());
        progress = new ProgressBar(new ContextThemeWrapper(getContext(), R.style.Progress_Circular));
        cal = new MaterialCalendarView(getContext());

        v.addView(progress);
        v.addView(cal);

        progress.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));

        cal.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        cal.addDecorator(new AvailableDecorator());
        cal.addDecorator(new ReadDecorator());
        cal.setOnDateChangedListener(this);

        new DataLoader().execute();

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.navigation_calendar)
                .setView(v)
                .setNegativeButton(R.string.button_cancel, null)
                .create();
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        if (listener != null)
            listener.onDateSelected(date.getDate());
        dismiss();
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    private class AvailableDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !datesAvailable.contains(Database.getIntFromDate(day.getDate()));
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(true);
        }
    }

    private class ReadDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return datesRead.contains(Database.getIntFromDate(day.getDate()));
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(getResources().getDrawable(R.drawable.calendar_read));
        }
    }

    private class DataLoader extends AsyncTask<Void, Void, Integer[]> {
        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            cal.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Integer[] doInBackground(Void... params) {
            int date;
            Cursor cursor = new Database(getContext()).getCalendar();
            while (cursor.moveToNext()) {
                date = cursor.getInt(cursor.getColumnIndex(Database.COLUMN_DATE));
                datesAvailable.add(date);
                if (cursor.getInt(cursor.getColumnIndex(Database.COLUMN_READ)) != 0)
                    datesRead.add(date);
            }
            cursor.close();
            try {
                return new Integer[]{Collections.min(datesAvailable), Collections.max(datesAvailable)};
            } catch (NoSuchElementException e) {
                return new Integer[]{};
            }
        }

        @Override
        protected void onPostExecute(Integer[] integers) {
            if (integers.length >= 2) {
                cal.setMinimumDate(Database.getDateFromInt(integers[0]));
                cal.setMaximumDate(Database.getDateFromInt(integers[1]));
            }
            cal.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        }
    }
}
