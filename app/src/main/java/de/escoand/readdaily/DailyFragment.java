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

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

public class DailyFragment extends Fragment implements SimpleCursorAdapter.ViewBinder, View.OnClickListener, OnDateSelectedListener {
    private static final String[] from = new String[]{Database.COLUMN_TITLE, Database.COLUMN_TEXT, Database.COLUMN_SOURCE};
    private static final int[] to = new int[]{R.id.daily_title, R.id.daily_text, R.id.daily_source};
    private static SimpleCursorAdapter adapter;
    private static Database db;
    ListView list;
    private Cursor cursor;
    private Date date;
    private ArrayList<DataListener> listener = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_daily, container);

        // data
        db = new Database(getContext());
        adapter = new SimpleCursorAdapter(getContext(), R.layout.item_daily, null, from, to, 0);
        adapter.setViewBinder(this);

        // list
        list = (ListView) v.findViewById(R.id.listView);
        list.setEmptyView(v.findViewById(R.id.listNoData));
        list.setAdapter(adapter);
        v.findViewById(R.id.button_store).setOnClickListener(this);

        // floating buttons
        if (v.findViewById(R.id.button_more) != null)
            v.findViewById(R.id.button_more).setOnClickListener(this);
        if (v.findViewById(R.id.button_note) != null)
            v.findViewById(R.id.button_note).setOnClickListener(this);
        if (v.findViewById(R.id.button_share) != null)
            v.findViewById(R.id.button_share).setOnClickListener(this);
        if (v.findViewById(R.id.button_read) != null)
            v.findViewById(R.id.button_read).setOnClickListener(this);
        if (v.findViewById(R.id.button_bible) != null)
            v.findViewById(R.id.button_bible).setOnClickListener(this);
        if (v.findViewById(R.id.button_readall) != null)
            v.findViewById(R.id.button_readall).setOnClickListener(this);

        setDate(new Date());

        return v;
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (columnIndex == cursor.getColumnIndex(Database.COLUMN_TITLE)) {
            View source = ((ViewGroup) view.getParent()).findViewById(R.id.daily_source);

            source.setVisibility(View.VISIBLE);

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
                case Database.TYPE_EXEGESIS:
                    if (listener.size() == 0)
                        view.setVisibility(View.VISIBLE);
                    else
                        view.setVisibility(View.GONE);
                    return true;
            }
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        onClick(v.getId(), v);
    }

    public void onClick(@NonNull int id, @Nullable View v) {
        DialogFragment dialog = null;
        Intent i = new Intent();
        Animator anim;

        switch (id) {

            // calendar
            case R.id.button_calendar:
                dialog = new CalendarDialogFragment();
                ((CalendarDialogFragment) dialog).setOnDateSelectedListener(this);
                break;

            // list
            case R.id.button_list:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_TEXT + "!=''", new String[]{Database.TYPE_DAY});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(this);
                break;
            case R.id.button_list_intro:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_SOURCE + "!=''", new String[]{Database.TYPE_INTRO});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(this);
                break;
            case R.id.button_list_voty:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_SOURCE + "!=''", new String[]{Database.TYPE_YEAR});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(this);
                break;

            // store
            case R.id.button_store:
                i.setClass(getActivity(), StoreActivity.class);
                break;

            // about
            case R.id.button_about:
                i.setClass(getActivity(), AboutActivity.class);
                break;

            // show buttons
            case R.id.button_more:
                toggleVisibility(getView().findViewById(R.id.button_bible));
                toggleVisibility(getView().findViewById(R.id.button_intro));
                toggleVisibility(getView().findViewById(R.id.button_note));
                toggleVisibility(getView().findViewById(R.id.button_read));
                toggleVisibility(getView().findViewById(R.id.button_readall));
                toggleVisibility(getView().findViewById(R.id.button_share));
                toggleVisibility(getView().findViewById(R.id.button_voty));

                // list
                if (list.isEnabled()) {
                    anim = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_half_out);
                    ((FloatingActionButton) v).setImageResource(R.drawable.icon_close);
                } else {
                    anim = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_half_in);
                    ((FloatingActionButton) v).setImageResource(R.drawable.icon_plus);
                }
                list.setEnabled(!list.isEnabled());
                anim.setTarget(list);
                anim.setDuration(500);
                anim.setInterpolator(new DecelerateInterpolator());
                anim.start();
                break;

            // today
            case R.id.button_today:
                setDate(new Date());
                break;

            // read bible
            case R.id.button_bible:
                String verse = null;
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_DAY))
                        verse = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
                }
                if (verse != null) {
                    String url = getString(R.string.url_bible) + verse.replaceAll(" ", "");
                    i.setAction(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                }
                break;

            // note
            case R.id.button_note:
                i.setAction("com.evernote.action.CREATE_NEW_NOTE");
                i.putExtra(Intent.EXTRA_TITLE, "");
                i.putExtra(Intent.EXTRA_TEXT, "");
                i.putExtra("TAG_NAME_LIST", new ArrayList<String>());
                i.putExtra("AUTHOR", "");
                i.putExtra("SOURCE_URL", "");
                i.putExtra("SOURCE_APP", "");
                break;

            // mark
            case R.id.button_readall:
                db.markAsRead(date);
                setDate(getDate());
                break;

            // share
            case R.id.button_share:
                // TODO date, title, bible, text, appname
                i.setAction(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT, cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT)));
                break;
        }

        // start dialog
        if (dialog != null) {
            dialog.show(getActivity().getSupportFragmentManager(), "dialog");
        }

        // start intent
        // TODO check intent-ed application
        else if (i.getAction() != null)
            startActivityForResult(i, 0);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
        refresh();
    }

    @Override
    public void onDateSelected(Date date) {
        setDate(date);
    }

    private void refresh() {
        if (adapter == null || db == null)
            return;

        cursor = db.getDay(date);

        // header interface
        for (DataListener tmp : listener)
            tmp.onDataUpdated(date, cursor);
        if (listener.size() > 0)
            adapter.changeCursor(db.getDay(date, Database.COLUMN_TYPE + "=?", new String[]{Database.TYPE_EXEGESIS}));
        else
            adapter.changeCursor(cursor);

        // floating action buttons
        if (getView() != null) {
            if (adapter.getCursor().getCount() > 0)
                toggleVisibility(getView().findViewById(R.id.button_more), View.VISIBLE);
            else
                toggleVisibility(getView().findViewById(R.id.button_more), View.GONE);
            toggleVisibility(getView().findViewById(R.id.button_bible), View.GONE);
            toggleVisibility(getView().findViewById(R.id.button_intro), View.GONE);
            toggleVisibility(getView().findViewById(R.id.button_note), View.GONE);
            toggleVisibility(getView().findViewById(R.id.button_read), View.GONE);
            toggleVisibility(getView().findViewById(R.id.button_readall), View.GONE);
            toggleVisibility(getView().findViewById(R.id.button_share), View.GONE);
            toggleVisibility(getView().findViewById(R.id.button_voty), View.GONE);
        }
    }

    public void registerDataListener(DataListener listener) {
        this.listener.add(listener);
        listener.onDataUpdated(date, cursor);
    }

    private void toggleVisibility(View v, int force) {
        if (v == null)
            return;
        if (force >= 0)
            v.setVisibility(force);
        else if (v.getVisibility() == View.VISIBLE)
            v.setVisibility(View.GONE);
        else
            v.setVisibility(View.VISIBLE);
    }

    private void toggleVisibility(View v) {
        toggleVisibility(v, -1);
    }
}
