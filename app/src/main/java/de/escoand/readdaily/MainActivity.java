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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements
        OnDateSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    private Date date = null;
    private Database db = Database.getInstance(this);
    private Cursor cursor;

    private DrawerLayout layout;
    private Toolbar toolbar;
    private Toolbar toolbarRight;
    private View playerButton;
    private EndlessContentPager pager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        setContentView(R.layout.activity_drawer);

        // drawer
        layout = (DrawerLayout) findViewById(R.id.wrapper);
        ((NavigationView) findViewById(R.id.drawer)).setNavigationItemSelectedListener(this);
        layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);

        // toolbars
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarRight = (Toolbar) findViewById(R.id.toolbar_right);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(Gravity.LEFT);
            }
        });
        toolbarRight.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.closeDrawer(Gravity.RIGHT);
            }
        });
        playerButton = toolbar.findViewById(R.id.toolbar_player);

        // search
        ((SearchView) toolbar.findViewById(R.id.toolbar_search)).setOnQueryTextListener(new OnSearchListener());

        // fragments
        pager = (EndlessContentPager) findViewById(R.id.content_pager);
        pager.addDataListener(this);
        pager.addDataListener((OnDateSelectedListener) getSupportFragmentManager().findFragmentById(R.id.content_voty));
        pager.addDataListener((OnDateSelectedListener) getSupportFragmentManager().findFragmentById(R.id.content_intro));

        // floating buttons
        findViewById(R.id.button_today).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.onDateSelected(new Date());
            }
        });
        if (findViewById(R.id.button_more) != null)
            findViewById(R.id.button_more).setOnClickListener(new OnMoreClickListener());
        if (findViewById(R.id.button_share) != null)
            findViewById(R.id.button_share).setOnClickListener(new OnShareClickListener());
        if (findViewById(R.id.button_intro) != null)
            findViewById(R.id.button_intro).setOnClickListener(new OnIntroClickListener());
        if (findViewById(R.id.button_voty) != null)
            findViewById(R.id.button_voty).setOnClickListener(new OnVotyClickListener());
    }

    @Override
    protected void attachBaseContext(final Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        DialogFragment dialog = null;
        Intent intent = null;

        layout.closeDrawers();

        switch (item.getItemId()) {

            // today
            case R.id.button_today:
                pager.onDateSelected(new Date());
                break;

            // list dialogs
            case R.id.button_list:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_SOURCE + "!=''", new String[]{Database.TYPE_EXEGESIS});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(pager);
                break;
            case R.id.button_list_intro:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setTitle(getString(R.string.navigation_intro));
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_TITLE + "!=''", new String[]{Database.TYPE_INTRO});
                ((ListDialogFragment) dialog).setMapping(new String[]{Database.COLUMN_TITLE, Database.COLUMN_READ}, new int[]{R.id.list_title, R.id.list_image});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(new OnIntroSelectedListener());
                break;
            case R.id.button_list_voty:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setTitle(getString(R.string.navigation_voty));
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_SOURCE + "!=''", new String[]{Database.TYPE_YEAR});
                ((ListDialogFragment) dialog).setOrder(Database.COLUMN_DATE);
                ((ListDialogFragment) dialog).setOnDateSelectedListener(new OnVotySelectedListener());
                break;

            // calendar
            case R.id.button_calendar:
                dialog = new CalendarDialogFragment();
                ((CalendarDialogFragment) dialog).setOnDateSelectedListener(pager);
                break;

            // search
            case R.id.button_search:
                /*if (search != null) {
                    search.setVisibility(View.VISIBLE);
                    search.setIconified(false);
                    search.requestFocus();
                    for (DataListener tmp : listener)
                        tmp.onDataUpdated(null, null);
                }*/
                break;

            // reminder
            case R.id.button_reminder:
                dialog = new ReminderDialogFragment();
                break;

            // settings
            case R.id.button_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;

            // store
            case R.id.button_store:
                intent = new Intent(this, StoreActivity.class);
                break;

            // about
            case R.id.button_about:
                intent = new Intent(this, AboutActivity.class);
                break;

            // do nothing
            default:
                break;
        }

        // start dialog
        if (dialog != null) {
            dialog.show(getSupportFragmentManager(), "dialog");
        }

        // start intent
        // TODO check intent-ed application
        else if (intent != null)
            startActivityForResult(intent, 0);

        return true;
    }

    @Override
    public void onDateSelected(final Date date) {
        SimpleDateFormat frmt = new SimpleDateFormat();
        String pattern;

        // default title
        toolbar.setTitle(getString(R.string.app_title));
        toolbar.setSubtitle(null);
        playerButton.setVisibility(View.GONE);
        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerDialogFragment player = new PlayerDialogFragment();
                player.setDate(getBaseContext(), date);
                player.show(getSupportFragmentManager(), player.getClass().getName());
            }
        });

        // init buttons
        if (findViewById(R.id.button_bible_exegesis) != null)
            findViewById(R.id.button_bible_exegesis).setOnClickListener(
                    new OnBibleClickListener(this, date, Database.TYPE_EXEGESIS));
        ((FloatingActionButton) findViewById(R.id.button_more)).setImageResource(R.drawable.icon_plus);
        toggleVisibility(R.id.button_more, View.GONE);
        toggleVisibility(R.id.button_intro, View.GONE);
        toggleVisibility(R.id.button_voty, View.GONE);

        if (date == null)
            return;

        this.date = date;
        this.cursor = db.getDay(date);

        // content
        if (!cursor.moveToFirst())
            return;
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            switch (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE))) {

                // exegesis title
                case Database.TYPE_EXEGESIS:
                    pattern = getString(R.string.toolbar_title);
                    pattern = pattern.replaceAll("%app_title%", "'" + getString(R.string.app_title) + "'");
                    pattern = pattern.replaceAll("%app_subtitle%", "'" + getString(R.string.app_subtitle) + "'");
                    frmt.applyPattern(pattern);
                    toolbar.setTitle(frmt.format(date));
                    pattern = getString(R.string.toolbar_subtitle);
                    pattern = pattern.replaceAll("%app_title%", "'" + getString(R.string.app_title) + "'");
                    pattern = pattern.replaceAll("%app_subtitle%", "'" + getString(R.string.app_subtitle) + "'");
                    frmt.applyPattern(pattern);
                    toolbar.setSubtitle(frmt.format(date));
                    break;

                // more button
                case Database.TYPE_YEAR:
                case Database.TYPE_INTRO:
                    toggleVisibility(R.id.button_more, View.VISIBLE);
                    break;

                // audio player
                case Database.TYPE_MEDIA:
                    playerButton.setVisibility(View.VISIBLE);
                    break;

                // do nothing
                default:
                    break;
            }
        }
    }

    private boolean toggleVisibility(@Nullable final View v, final int force) {
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

    private boolean toggleVisibility(@IdRes final int id, final int force) {
        return toggleVisibility(findViewById(id), force);
    }

    private boolean toggleVisibility(@Nullable final View v) {
        return toggleVisibility(v, -1);
    }

    private boolean toggleVisibility(@IdRes final int id) {
        return toggleVisibility(findViewById(id));
    }

    private class OnMoreClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {

            // image
            if (findViewById(R.id.button_voty).getVisibility() == View.VISIBLE ||
                    findViewById(R.id.button_intro).getVisibility() == View.VISIBLE)
                ((FloatingActionButton) v).setImageResource(R.drawable.icon_plus);
            else
                ((FloatingActionButton) v).setImageResource(R.drawable.icon_close);

            // buttons
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                switch (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE))) {

                    case Database.TYPE_YEAR:
                        toggleVisibility(R.id.button_voty);
                        break;

                    case Database.TYPE_INTRO:
                        toggleVisibility(R.id.button_intro);
                        break;

                    default: // do nothing
                        break;
                }
            }
        }
    }

    private class OnIntroSelectedListener implements OnDateSelectedListener {
        @Override
        public void onDateSelected(@NonNull final Date date) {
            IntroContentFragment dialog = new IntroContentFragment();
            dialog.onDateSelected(date);
            dialog.show(getSupportFragmentManager(), dialog.getClass().getName());
        }
    }

    private class OnVotySelectedListener implements OnDateSelectedListener {
        @Override
        public void onDateSelected(@NonNull final Date date) {
            YearContentFragment dialog = new YearContentFragment();
            dialog.onDateSelected(date);
            dialog.show(getSupportFragmentManager(), dialog.getClass().getName());
        }
    }

    private class OnIntroClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            new OnMoreClickListener().onClick(findViewById(R.id.button_more));
            findViewById(R.id.content_intro).setVisibility(View.VISIBLE);
            findViewById(R.id.content_voty).setVisibility(View.GONE);
            layout.openDrawer(Gravity.RIGHT);
        }
    }

    private class OnVotyClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            new OnMoreClickListener().onClick(findViewById(R.id.button_more));
            findViewById(R.id.content_intro).setVisibility(View.GONE);
            findViewById(R.id.content_voty).setVisibility(View.VISIBLE);
            layout.openDrawer(Gravity.RIGHT);
        }
    }

    private class OnShareClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            String title = null;
            String verse = null;
            String text = null;

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_EXEGESIS)) {
                    title = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TITLE));
                    text = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
                } else if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_DAY))
                    verse = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
            }

            if (title != null && text != null && verse != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, DateFormat.getDateInstance().format(date) + "\n" +
                        title + " (" + verse + ")\n" + text + "\n" + getString(R.string.app_title));
                startActivityForResult(intent, 0);
            }
        }
    }

    private class OnSearchListener implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
        @Override
        public boolean onQueryTextChange(final String newText) {
            //adapter.changeCursor(db.getSearch(newText));
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(final String query) {
            //adapter.changeCursor(db.getSearch(query));
            //search.setVisibility(View.GONE);
            return true;
        }

        @Override
        public boolean onClose() {
            //search.setVisibility(View.GONE);
            return true;
        }
    }
}