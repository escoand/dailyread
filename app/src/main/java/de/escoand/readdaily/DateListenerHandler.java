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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

public class DateListenerHandler implements OnDateSelectedListener {
    private static DateListenerHandler handler;
    private ArrayList<OnDateSelectedListener> listeners = new ArrayList<>();

    private DateListenerHandler() {
        super();
    }

    public static DateListenerHandler getInstance() {
        if (handler == null)
            handler = new DateListenerHandler();
        return handler;
    }

    public void addDateListener(@Nullable final OnDateSelectedListener listener) {
        if (listener != null)
            listeners.add(listener);
    }

    public void removeDateListener(@NonNull final OnDateSelectedListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onDateSelected(@NonNull Date date) {
        onDateSelected(date, null);
    }

    public void onDateSelected(@NonNull final Date date, @Nullable final OnDateSelectedListener except) {
        for (OnDateSelectedListener tmp : listeners)
            if (date != null && tmp != except)
                tmp.onDateSelected(date);
    }
}
