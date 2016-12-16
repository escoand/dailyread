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

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.Date;

public abstract class AbstractContentFragment extends Fragment implements OnDateSelectedListener, SimpleCursorAdapter.ViewBinder {
    protected final String[] from = new String[]{Database.COLUMN_TITLE, Database.COLUMN_TEXT, Database.COLUMN_SOURCE};
    protected final int[] to = new int[]{R.id.daily_title, R.id.daily_text, R.id.daily_source};
    protected SimpleCursorAdapter adapter = null;
    protected String condition = null;
    protected String[] values = null;
    protected Date date = null;
    protected Cursor cursor = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adapter = new SimpleCursorAdapter(getContext(), R.layout.item_content, null, from, to, 0);
        adapter.setViewBinder(this);
        adapter.changeCursor(cursor);

        ListView list = new ListView(getContext());
        list.setAdapter(adapter);

        return list;
    }

    @Override
    public void onDateSelected(@NonNull Date date) {
        this.date = date;
        this.cursor = Database.getInstance(getContext()).getDay(date, condition, values);
    }

    @Override
    public void onDateSelected(@NonNull Date date, @Nullable String condition, @Nullable String[] values) {
        onDateSelected(date);
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        View source = ((ViewGroup) view.getParent()).findViewById(R.id.daily_source);
        switch (cursor.getColumnName(columnIndex)) {

            // title
            case Database.COLUMN_TITLE:
                switch (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE))) {
                    case Database.TYPE_YEAR:
                        ((TextView) view).setText(getContext().getString(R.string.type_voty));
                        return true;
                    case Database.TYPE_MONTH:
                        ((TextView) view).setText(getContext().getString(R.string.type_votm));
                        return true;
                    case Database.TYPE_WEEK:
                        ((TextView) view).setText(getContext().getString(R.string.type_votw));
                        return true;
                    case Database.TYPE_DAY:
                        ((TextView) view).setText(getContext().getString(R.string.type_votd));
                        source.setVisibility(View.GONE);
                        return true;
                }
                break;

            // source
            case Database.COLUMN_SOURCE:
                if (cursor.isNull(columnIndex)) {
                    view.setVisibility(View.GONE);
                    return true;
                }
                break;

        }

        // text view
        if (view instanceof TextView) {
            ((TextView) view).setText(Html.fromHtml(cursor.getString(columnIndex)));
            return true;
        }

        return false;
    }
}
