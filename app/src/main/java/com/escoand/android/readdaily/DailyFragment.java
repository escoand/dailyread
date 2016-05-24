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

package com.escoand.android.readdaily;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

public class DailyFragment extends Fragment implements
        SimpleCursorAdapter.ViewBinder,
        AdapterView.OnItemClickListener,
        View.OnClickListener {
    private static final String[] from = new String[]{Database.COLUMN_TITLE, Database.COLUMN_TEXT, Database.COLUMN_SOURCE};
    private static final int[] to = new int[]{R.id.itemTitle, R.id.itemText, R.id.itemAuthor};
    private static SimpleCursorAdapter listAdapter;
    private static Database db;
    private Date date = new Date();
    private FloatingActionButton floating_note;
    private FloatingActionButton floating_share;
    private FloatingActionButton floating_read;

    private View selected = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, null);

        db = new Database(getContext());

        listAdapter = new SimpleCursorAdapter(getContext(), R.layout.item_daily, null, from, to, 0);
        listAdapter.setViewBinder(this);

        ListView list = (ListView) v.findViewById(R.id.listView);
        list.setEmptyView(v.findViewById(R.id.listNoData));
        list.setAdapter(listAdapter);
        list.setOnItemClickListener(this);

        v.findViewById(R.id.buttonStore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), StoreActivity.class), 0);
            }
        });

        // floating buttons
        floating_note = (FloatingActionButton) container.getRootView().findViewById(R.id.floating_note);
        floating_share = (FloatingActionButton) container.getRootView().findViewById(R.id.floating_share);
        floating_read = (FloatingActionButton) container.getRootView().findViewById(R.id.floating_read);
        floating_note.setOnClickListener(this);
        floating_share.setOnClickListener(this);
        floating_read.setOnClickListener(this);

        refresh();

        return v;
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        Log.e("setViewValue", String.valueOf(columnIndex) + "/" + cursor.getColumnCount());
        Log.e("setViewValue", view.toString());
        if (columnIndex == cursor.getColumnIndex(Database.COLUMN_TITLE) && view instanceof TextView)
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
                    return true;
            }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (selected != null)
            selected.setBackgroundColor(getResources().getColor(android.support.v7.cardview.R.color.cardview_light_background));

        // unselect
        if (selected != null && view == selected.getParent()) {
            floating_note.setVisibility(View.GONE);
            floating_share.setVisibility(View.GONE);
            selected = null;
        }

        // select
        else {
            selected = view.findViewById(R.id.daily_card);
            selected.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            floating_note.setVisibility(View.VISIBLE);
            floating_share.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        Cursor c = listAdapter.getCursor();
        Intent i = new Intent();
        switch (v.getId()) {
            case R.id.floating_note:
                i.setAction("com.evernote.action.CREATE_NEW_NOTE");
                // TODO
                i.putExtra(Intent.EXTRA_TITLE, "");
                i.putExtra(Intent.EXTRA_TEXT, "");
                i.putExtra("TAG_NAME_LIST", new ArrayList<String>());
                i.putExtra("AUTHOR", "");
                i.putExtra("SOURCE_URL", "");
                i.putExtra("SOURCE_APP", "");
                startActivity(i);
                break;
            case R.id.floating_share:
                i.setAction(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT, c.getString(c.getColumnIndex(Database.COLUMN_TEXT)));
                startActivity(i);
                break;
            case R.id.floating_read:
                db.markAsRead(date);
                floating_read.setImageResource(R.drawable.floating_read);
                break;
        }
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
        refresh();
    }

    private void refresh() {
        if (listAdapter == null || db == null)
            return;

        Cursor c = db.getDay(date);
        listAdapter.changeCursor(c);
        if (c.getCount() > 0) {
            floating_read.setVisibility(View.VISIBLE);
            if (c.getInt(c.getColumnIndex(Database.COLUMN_READ)) != 0)
                floating_read.setImageResource(R.drawable.floating_read);
            else
                floating_read.setImageResource(R.drawable.floating_unread);
        } else
            floating_read.setVisibility(View.GONE);
    }
}
