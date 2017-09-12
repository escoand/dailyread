/*
 * Copyright (c) 2017 escoand.
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

import android.content.Context;
import android.database.Cursor;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Observable;

public class DatePersistence extends Observable {
    private static DatePersistence instance = null;
    private GregorianCalendar calendar = new GregorianCalendar();

    private DatePersistence() {
        super();
    }

    public static DatePersistence getInstance() {
        if (instance == null)
            instance = new DatePersistence();
        return instance;
    }

    public void setDateOffset(final int offset) {
        calendar.add(GregorianCalendar.DATE, offset);
        setChanged();
        notifyObservers();
    }

    public void prevDate() {
        setDateOffset(-1);
    }

    public void nextDate() {
        setDateOffset(-1);
    }

    /* date getter */
    public Date getDate() {
        return calendar.getTime();
    }

    /* date setters */
    public void setDate(final Date date) {
        calendar.setTime(date);
        setChanged();
        notifyObservers();
    }

    /* data getter */
    public Cursor getData(final Context context, final String condition, final String[] values) {
        return Database.getInstance(context).getDay(calendar.getTime(), condition, values);
    }

    public Cursor getData(final Context context, final String condition, final String[] values, final int dayOffset) {
        final GregorianCalendar prevCalendar = (GregorianCalendar) calendar.clone();
        prevCalendar.add(GregorianCalendar.DATE, dayOffset);
        return Database.getInstance(context).getDay(prevCalendar.getTime(), condition, values);
    }
}
