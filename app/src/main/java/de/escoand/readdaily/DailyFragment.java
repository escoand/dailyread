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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DailyFragment extends Fragment implements SimpleCursorAdapter.ViewBinder, View.OnClickListener, OnDateSelectedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private final String[] from = new String[]{Database.COLUMN_TITLE, Database.COLUMN_TEXT, Database.COLUMN_SOURCE};
    private final int[] to = new int[]{R.id.daily_title, R.id.daily_text, R.id.daily_source};
    private Date date = null;
    private String condition = null;
    private String[] values = null;
    private Database db;
    private SimpleCursorAdapter adapter;
    private Cursor cursor;
    private ListView list;
    private ArrayList<DataListener> listener = new ArrayList<>();
    private SharedPreferences settings;
    private SearchView search = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_daily, container);

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // data
        db = ((ReadDailyApp) getActivity().getApplication()).getDatabase();
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
        if (v.findViewById(R.id.button_bible_exegesis) != null)
            v.findViewById(R.id.button_bible_exegesis).setOnClickListener(this);
        if (v.findViewById(R.id.button_readall) != null)
            v.findViewById(R.id.button_readall).setOnClickListener(this);
        if (v.findViewById(R.id.button_intro) != null)
            v.findViewById(R.id.button_intro).setOnClickListener(this);
        if (v.findViewById(R.id.button_voty) != null)
            v.findViewById(R.id.button_voty).setOnClickListener(this);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set initial date
        if (savedInstanceState != null) {
            date = Database.getDateFromInt(savedInstanceState.getInt("date"));
            condition = savedInstanceState.getString("condition");
            values = savedInstanceState.getStringArray("values");
        } else
            date = new Date();
    }

    @Override
    public void onResume() {
        onDateSelected(date, condition, values);
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("date", Database.getIntFromDate(date));
        outState.putString("condition", condition);
        outState.putStringArray("values", values);
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


    @Override
    public void onClick(View v) {
        onClick(v.getId(), v);
    }

    public void onClick(@NonNull Integer id, @Nullable View v) {
        DialogFragment dialog = null;
        Intent i = null;
        Animator anim;
        String title = null;
        String verse = null;
        String text = null;

        switch (id) {

            // list dialogs
            case R.id.button_list:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_SOURCE + "!=''", new String[]{Database.TYPE_EXEGESIS});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(this);
                break;
            case R.id.button_list_intro:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setTitle(getString(R.string.navigation_intro));
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_TITLE + "!=''", new String[]{Database.TYPE_INTRO});
                ((ListDialogFragment) dialog).setMapping(new String[]{Database.COLUMN_TITLE, Database.COLUMN_READ}, new int[]{R.id.list_title, R.id.list_image});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(this);
                break;
            case R.id.button_list_voty:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setTitle(getString(R.string.navigation_voty));
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_SOURCE + "!=''", new String[]{Database.TYPE_YEAR});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(this, Database.COLUMN_TYPE + "=?", new String[]{Database.TYPE_YEAR});
                break;

            // calendar
            case R.id.button_calendar:
                dialog = new CalendarDialogFragment();
                ((CalendarDialogFragment) dialog).setOnDateSelectedListener(this);
                break;

            // search
            case R.id.button_search:
                if (search != null) {
                    search.setVisibility(View.VISIBLE);
                    search.setIconified(false);
                    search.requestFocus();
                    for (DataListener tmp : listener)
                        tmp.onDataUpdated(null, null);
                }
                break;

            // reminder
            case R.id.button_reminder:
                dialog = new ReminderDialogFragment();
                break;

            // settings
            case R.id.button_settings:
                i = new Intent(getActivity(), SettingsActivity.class);
                break;

            // store
            case R.id.button_store:
                i = new Intent(getActivity(), StoreActivity.class);
                break;

            // about
            case R.id.button_about:
                i = new Intent(getActivity(), AboutActivity.class);
                break;

            // today
            case R.id.button_today:
                onDateSelected(new Date());
                break;

            // toggle buttons
            case R.id.button_more:

                // toggle action buttons
                toggleVisibility(getView().findViewById(R.id.button_bible_exegesis));
                toggleVisibility(getView().findViewById(R.id.button_note));
                toggleVisibility(getView().findViewById(R.id.button_read));
                toggleVisibility(getView().findViewById(R.id.button_readall));
                toggleVisibility(getView().findViewById(R.id.button_share));

                // toggle linked buttons
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {

                    switch (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE))) {

                        case Database.TYPE_YEAR:
                            toggleVisibility(getView().findViewById(R.id.button_voty));
                            break;

                        case Database.TYPE_INTRO:
                            toggleVisibility(getView().findViewById(R.id.button_intro));
                            break;

                    }
                }

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

            // read bible for day
            case R.id.button_bible_day:
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_DAY))
                        verse = cursor.getString(cursor.getColumnIndex(Database.COLUMN_SOURCE));
                }
                if (verse != null) {
                    String url = getString(R.string.url_bible)
                            + settings.getString("bible_translation", "LUT") + "/"
                            + verse.replaceAll(" ", "");
                    i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                }
                break;

            // read bible for exegesis
            case R.id.button_bible_exegesis:
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_EXEGESIS))
                        verse = cursor.getString(cursor.getColumnIndex(Database.COLUMN_SOURCE));
                }
                if (verse != null) {
                    String url = getString(R.string.url_bible)
                            + settings.getString("bible_translation", "LUT") + "/"
                            + verse.replaceAll(" ", "");
                    i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                }
                break;

            // intro
            case R.id.button_intro:
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_INTRO)) {
                        onDateSelected(Database.getDateFromInt(cursor.getInt(cursor.getColumnIndex(Database.COLUMN_DATE))));
                        break;
                    }
                }
                break;

            // voty
            case R.id.button_voty:
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_YEAR)) {
                        onDateSelected(
                                Database.getDateFromInt(cursor.getInt(cursor.getColumnIndex(Database.COLUMN_DATE))),
                                Database.COLUMN_TYPE + "=?",
                                new String[]{Database.TYPE_YEAR}
                        );
                        break;
                    }
                }
                break;

            // mark
            case R.id.button_readall:
                db.markAsRead(date);
                refreshUI();
                break;

            // share
            case R.id.button_share:
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_EXEGESIS)) {
                        title = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TITLE));
                        text = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
                    } else if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_DAY))
                        verse = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
                }
                if (title != null && text != null && verse != null) {
                    i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, DateFormat.getDateInstance().format(date) + "\n" +
                            title + " (" + verse + ")\n" + text + "\n" + getString(R.string.app_title));
                }
                break;

            // note
            case R.id.button_note:
                i = new Intent("com.evernote.action.CREATE_NEW_NOTE");
                i.putExtra(Intent.EXTRA_TITLE, "");
                i.putExtra(Intent.EXTRA_TEXT, "");
                i.putExtra("TAG_NAME_LIST", new ArrayList<String>());
                i.putExtra("AUTHOR", "");
                i.putExtra("SOURCE_URL", "");
                i.putExtra("SOURCE_APP", "");
                break;
        }

        // start dialog
        if (dialog != null) {
            dialog.show(getActivity().getSupportFragmentManager(), "dialog");
        }

        // start intent
        // TODO check intent-ed application
        else if (i != null)
            getActivity().startActivityForResult(i, 0);
    }

    @Override
    public void onDateSelected(Date date) {
        onDateSelected(date, null, null);
    }

    @Override
    public void onDateSelected(Date date, String condition, String[] values) {
        this.date = date;
        this.condition = condition;
        this.values = values;
        refreshUI();
    }

    public void setSearchView(SearchView search) {
        this.search = search;
        if (search != null)
            search.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.changeCursor(db.getSearch(newText));
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.changeCursor(db.getSearch(query));
        search.setVisibility(View.GONE);
        return true;
    }

    @Override
    public boolean onClose() {
        search.setVisibility(View.GONE);
        return true;
    }

    private void refreshUI() {
        if (adapter == null || db == null)
            return;

        cursor = db.getDay(date, condition, values);

        // workaround: scroll to top
        adapter.changeCursor(null);

        // data listeners
        for (DataListener tmp : listener)
            tmp.onDataUpdated(date, cursor);
        if (listener.size() > 0 && date.getYear() < 1000)
            adapter.changeCursor(db.getDay(date, Database.COLUMN_TYPE + " IN (?,?,?)", new String[]{Database.TYPE_MONTH, Database.TYPE_WEEK, Database.TYPE_EXEGESIS}));
        else
            adapter.changeCursor(cursor);

        if (getView() == null)
            return;

        // init views
        list.setAlpha(1);
        list.setEnabled(true);
        ((FloatingActionButton) getView().findViewById(R.id.button_more)).setImageResource(R.drawable.icon_plus);
        toggleVisibility(getView().findViewById(R.id.button_more), View.GONE);
        toggleVisibility(getView().findViewById(R.id.button_bible_exegesis), View.GONE);
        toggleVisibility(getView().findViewById(R.id.button_intro), View.GONE);
        toggleVisibility(getView().findViewById(R.id.button_note), View.GONE);
        toggleVisibility(getView().findViewById(R.id.button_read), View.GONE);
        toggleVisibility(getView().findViewById(R.id.button_readall), View.GONE);
        toggleVisibility(getView().findViewById(R.id.button_share), View.GONE);
        toggleVisibility(getView().findViewById(R.id.button_voty), View.GONE);

        // plus button
        int entries = 0;
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            switch (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE))) {
                case Database.TYPE_EXEGESIS:
                case Database.TYPE_YEAR:
                case Database.TYPE_INTRO:
                    entries++;
                    break;
            }
        }
        if (entries > 1)
            toggleVisibility(getView().findViewById(R.id.button_more), View.VISIBLE);
    }

    public void registerDataListener(DataListener listener) {
        this.listener.add(listener);
        if (date != null && cursor != null)
            listener.onDataUpdated(date, cursor);
    }

    private boolean toggleVisibility(View v, int force) {
        if (v == null)
            return false;
        if (force >= 0)
            v.setVisibility(force);
        else if (v.getVisibility() == View.VISIBLE)
            v.setVisibility(View.GONE);
        else
            v.setVisibility(View.VISIBLE);
        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean toggleVisibility(View v) {
        return toggleVisibility(v, -1);
    }
}
