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

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class DailyViewBinder implements SimpleCursorAdapter.ViewBinder {
    private static Context context;

    public DailyViewBinder(Context context) {
        DailyViewBinder.context = context;
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (columnIndex == cursor.getColumnIndex(Database.COLUMN_TITLE) && view instanceof TextView)
            switch (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE))) {
                case Database.TYPE_YEAR:
                    ((TextView) view).setText(context.getString(R.string.type_voty));
                    return true;
                case Database.TYPE_MONTH:
                    ((TextView) view).setText(context.getString(R.string.type_votm));
                    return true;
                case Database.TYPE_WEEK:
                    ((TextView) view).setText(context.getString(R.string.type_votw));
                    return true;
                case Database.TYPE_DAY:
                    ((TextView) view).setText(context.getString(R.string.type_votd));
                    return true;
            }
        return false;
    }
}
