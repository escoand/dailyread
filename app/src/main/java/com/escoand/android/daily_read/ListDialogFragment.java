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
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Date;

public class ListDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    OnDateSelectedListener listener;
    SimpleCursorAdapter adapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_list, null);

        // adapter
        adapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.item_list,
                new Database(getContext()).getList(),
                new String[]{Database.COLUMN_SOURCE, Database.COLUMN_DATE},
                new int[]{R.id.listTitle, R.id.listDate},
                0);

        // list
        ListView list = (ListView) v.findViewById(R.id.listView);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        // dialog
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.navigation_list)
                .setView(v)
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = (Cursor) adapter.getItem(position);
        Date date = Database.getDateFromInt(c.getInt(c.getColumnIndex(Database.COLUMN_DATE)));
        if (listener != null)
            listener.onDateSelected(date);
        dismiss();
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }
}
