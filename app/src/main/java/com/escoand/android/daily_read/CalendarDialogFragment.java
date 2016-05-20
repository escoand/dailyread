/*
 * Copyright (C) 2016  escoand
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

package com.escoand.android.daily_read;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;

public class CalendarDialogFragment extends DialogFragment implements com.prolificinteractive.materialcalendarview.OnDateSelectedListener {
    OnDateSelectedListener listener;

    HashSet<Integer> datesAvailable = new HashSet<>();
    HashSet<Integer> datesRead = new HashSet<>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // calendar
        MaterialCalendarView cal = new MaterialCalendarView(getContext());
        cal.addDecorator(new AvailableDecorator());
        cal.addDecorator(new ReadDecorator());
        cal.setOnDateChangedListener(this);

        // read dates
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        Cursor cursor = new Database(getContext()).getList();
        while (cursor.moveToNext()) {

            // available
            int date = cursor.getInt(cursor.getColumnIndex(Database.COLUMN_DATE));
            datesAvailable.add(date);

            // read
            if (cursor.getInt(cursor.getColumnIndex(Database.COLUMN_READ)) != 0)
                datesRead.add(date);

            // min and max
            if (date < min)
                min = date;
            if (date > max)
                max = date;
        }

        // min and max
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            cal.setMinimumDate(df.parse(String.valueOf(min)));
            cal.setMaximumDate(df.parse(String.valueOf(max)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // dialog
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.navigation_calendar)
                .setView(cal)
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
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
}
