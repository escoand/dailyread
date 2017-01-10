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

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ListDialogFragment extends DialogFragment implements SimpleCursorAdapter.ViewBinder, DialogInterface.OnClickListener {
    private String title = null;
    private String condition = null;
    private String[] values = null;
    private String order = Database.COLUMN_GROUP;

    private SimpleCursorAdapter adapter;
    private String[] from = new String[]{Database.COLUMN_READ, Database.COLUMN_SOURCE, Database.COLUMN_DATE};
    private int[] to = new int[]{R.id.list_image, R.id.list_title, R.id.list_date};

    private OnDateSelectedListener listener;

    public void setTitle(@Nullable final String title) {
        this.title = title;
    }

    public void setFilter(@NonNull final String condition, @NonNull final String[] values) {
        this.condition = condition;
        this.values = values;
    }

    public void setOrder(@NonNull final String order) {
        this.order = order;
    }

    public void setMapping(final String[] from, final int[] to) {
        this.from = from;
        this.to = to;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        if (title == null)
            title = getString(R.string.navigation_list);

        // adapter
        adapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.item_dialog,
                Database.getInstance(getContext()).getList(condition, values, order),
                from, to,
                0);
        adapter.setViewBinder(this);

        // dialog
        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setAdapter(adapter, this)
                .setNegativeButton(R.string.button_cancel, null)
                .create();
    }

    @Override
    public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
        if (columnIndex == cursor.getColumnIndex(Database.COLUMN_READ)) {
            if (cursor.getInt(cursor.getColumnIndex(Database.COLUMN_READ)) != 0)
                view.setVisibility(View.VISIBLE);
            else
                view.setVisibility(View.INVISIBLE);
            return true;
        } else if (columnIndex == cursor.getColumnIndex(Database.COLUMN_DATE)) {
            try {
                ((TextView) view).setText(DateFormat.getMediumDateFormat(
                        getContext()).format(Database.getDateFromInt(cursor.getInt(columnIndex))));
            } catch (NullPointerException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        Cursor c = (Cursor) adapter.getItem(which);
        if (listener != null)
            listener.onDateSelected(
                    Database.getDateFromInt(c.getInt(c.getColumnIndex(Database.COLUMN_DATE))));
    }

    public void setOnDateSelectedListener(@Nullable final OnDateSelectedListener listener) {
        this.listener = listener;
    }
}
