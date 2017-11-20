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
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class DatePersistence extends Observable {
    private static DatePersistence instance = new DatePersistence();
    private GregorianCalendar calendar = new GregorianCalendar();
    private Vector<Observer> observers = new Vector<>();

    private DatePersistence() {
        super();
    }

    public synchronized static DatePersistence getInstance() {
        return instance;
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

    public void setDateOffset(final int offset) {
        calendar.add(GregorianCalendar.DATE, offset);
        setChanged();
        notifyObservers();
    }

    @Override
    public synchronized void addObserver(final Observer o) {
        super.addObserver(o);
        observers.add(o);
    }

    @Override
    public synchronized void deleteObserver(final Observer o) {
        super.deleteObserver(o);
        observers.remove(o);
    }

    public synchronized void restoreObservers() {
        final Iterator<Observer> i = observers.iterator();
        while (i.hasNext())
            super.addObserver(i.next());
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
