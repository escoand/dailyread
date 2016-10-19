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

package de.escoand.readdaily;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

public class ReminderDialogFragment extends DialogFragment implements TimePicker.OnTimeChangedListener, DialogInterface.OnClickListener {
    private final static String SETTINGS_ACTIVE = "reminder_active";
    private final static String SETTINGS_HOUR = "reminder_hour";
    private final static String SETTINGS_MINUTE = "reminder_minute";

    SharedPreferences settings;
    int hour;
    int minute;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePicker v = new TimePicker(getContext());

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        hour = settings.getInt(SETTINGS_HOUR, 9);
        minute = settings.getInt(SETTINGS_MINUTE, 0);

        v.setIs24HourView(true);
        v.setOnTimeChangedListener(this);
        v.setCurrentHour(hour);
        v.setCurrentMinute(minute);

        return new AlertDialog.Builder(getContext())
                .setView(v)
                .setPositiveButton(R.string.button_ok, this)
                .setNegativeButton(R.string.button_cancel, this)
                .create();
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {

            // activate
            case DialogInterface.BUTTON_POSITIVE:
                settings.edit()
                        .putBoolean(SETTINGS_ACTIVE, true)
                        .putInt(SETTINGS_HOUR, hour)
                        .putInt(SETTINGS_MINUTE, minute)
                        .apply();
                ReminderHandler.startReminder(getContext(), hour, minute);
                break;


            // deactivate
            case DialogInterface.BUTTON_NEGATIVE:
                settings.edit()
                        .putBoolean(SETTINGS_ACTIVE, false)
                        .apply();
                ReminderHandler.endReminder(getContext());
                break;

        }
    }
}
