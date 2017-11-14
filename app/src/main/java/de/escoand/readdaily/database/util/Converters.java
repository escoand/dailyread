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

package de.escoand.readdaily.database.util;

import android.arch.persistence.room.TypeConverter;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Converters {

    @TypeConverter
    public static Calendar intToCalendar(int value) {
        int year = value / 10000;
        int month = value / 100 - year * 100;
        int day = value - year * 10000 - month * 100;
        return new GregorianCalendar(year, month - 1, day);
    }

    @TypeConverter
    public static int calendarToInt(Calendar date) {
        return date.get(Calendar.YEAR) * 10000 +
                (date.get(Calendar.MONTH) + 1) * 100 +
                date.get(Calendar.DAY_OF_MONTH);
    }
}