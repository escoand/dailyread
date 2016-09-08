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
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

public class DailyFragment extends Fragment implements SimpleCursorAdapter.ViewBinder, View.OnClickListener {
    private static final String[] from = new String[]{Database.COLUMN_TITLE, Database.COLUMN_TEXT, Database.COLUMN_SOURCE};
    private static final int[] to = new int[]{R.id.daily_title, R.id.daily_text, R.id.daily_source};
    private static SimpleCursorAdapter adapter;
    private static Database db;
    ListView list;
    private Cursor cursor;
    private Date date = new Date();
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
        v.findViewById(R.id.buttonStore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), StoreActivity.class), 0);
            }
        });

        // floating buttons
        if (v.findViewById(R.id.floating_more) != null)
            v.findViewById(R.id.floating_more).setOnClickListener(this);
        if (v.findViewById(R.id.floating_note) != null)
            v.findViewById(R.id.floating_note).setOnClickListener(this);
        if (v.findViewById(R.id.floating_share) != null)
            v.findViewById(R.id.floating_share).setOnClickListener(this);
        if (v.findViewById(R.id.floating_read) != null)
            v.findViewById(R.id.floating_read).setOnClickListener(this);
        if (v.findViewById(R.id.floating_bible) != null)
            v.findViewById(R.id.floating_bible).setOnClickListener(this);
        if (v.findViewById(R.id.floating_readall) != null)
            v.findViewById(R.id.floating_readall).setOnClickListener(this);

        refresh();

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
        Intent i = new Intent();
        Animator anim;
        switch (v.getId()) {

            // show buttons
            case R.id.floating_more:
                toggleVisibility(v.getRootView().findViewById(R.id.floating_bible));
                toggleVisibility(v.getRootView().findViewById(R.id.floating_intro));
                toggleVisibility(v.getRootView().findViewById(R.id.floating_note));
                toggleVisibility(v.getRootView().findViewById(R.id.floating_read));
                toggleVisibility(v.getRootView().findViewById(R.id.floating_readall));
                toggleVisibility(v.getRootView().findViewById(R.id.floating_share));
                toggleVisibility(v.getRootView().findViewById(R.id.floating_voty));

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
            case R.id.footer_today:
                setDate(new Date());
                break;

            // read bible
            case R.id.read_bible:
            case R.id.footer_bible:
            case R.id.floating_bible:
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
            case R.id.floating_note:
                i.setAction("com.evernote.action.CREATE_NEW_NOTE");
                i.putExtra(Intent.EXTRA_TITLE, "");
                i.putExtra(Intent.EXTRA_TEXT, "");
                i.putExtra("TAG_NAME_LIST", new ArrayList<String>());
                i.putExtra("AUTHOR", "");
                i.putExtra("SOURCE_URL", "");
                i.putExtra("SOURCE_APP", "");
                break;

            // mark
            case R.id.floating_readall:
                db.markAsRead(date);
                setDate(getDate());
                break;

            // share
            case R.id.floating_share:
                // TODO date, title, bible, text, appname
                i.setAction(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT, cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT)));
                break;
        }
        // TODO check intent-ed application
        if (i.getAction() != null)
            startActivity(i);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
        refresh();
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
                toggleVisibility(getView().findViewById(R.id.floating_more), View.VISIBLE);
            else
                toggleVisibility(getView().findViewById(R.id.floating_more), View.GONE);
            toggleVisibility(getView().findViewById(R.id.floating_bible), View.GONE);
            toggleVisibility(getView().findViewById(R.id.floating_intro), View.GONE);
            toggleVisibility(getView().findViewById(R.id.floating_note), View.GONE);
            toggleVisibility(getView().findViewById(R.id.floating_read), View.GONE);
            toggleVisibility(getView().findViewById(R.id.floating_readall), View.GONE);
            toggleVisibility(getView().findViewById(R.id.floating_share), View.GONE);
            toggleVisibility(getView().findViewById(R.id.floating_voty), View.GONE);
        }
    }

    public void registerDataListener(DataListener listener) {
        this.listener.add(listener);
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
