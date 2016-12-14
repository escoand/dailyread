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
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener {
    private Date date = null;
    private Database db = Database.getInstance(this);
    private Cursor cursor;
    private ArrayList<DataListener> listeners = new ArrayList<>();

    private DrawerLayout layout;
    private Toolbar toolbar;
    private View playerButton;
    private EndlessDailyPager pager;

    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set initial date
        if (savedInstanceState != null) {
            date = Database.getDateFromInt(savedInstanceState.getInt("date"));
        } else if (date == null)
            date = new Date();
    }*/

    @Override
    public void onResume() {
        onDateSelected(date);
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("date", Database.getIntFromDate(date));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        setContentView(R.layout.activity_drawer);

        layout = (DrawerLayout) findViewById(R.id.wrapper);
        //layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);

        // toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(GravityCompat.START);
            }
        });
        playerButton = toolbar.findViewById(R.id.toolbar_player);

        // drawer
        ((NavigationView) findViewById(R.id.drawer)).setNavigationItemSelectedListener(this);

        // fragments
        pager = (EndlessDailyPager) findViewById(R.id.content_pager);
        final HeaderFragment header = (HeaderFragment) getSupportFragmentManager().findFragmentById(R.id.header);
        listeners.add(header);
        listeners.add((DataListener) getSupportFragmentManager().findFragmentById(R.id.footer));
        listeners.add((DataListener) getSupportFragmentManager().findFragmentById(R.id.content_voty));
        listeners.add((DataListener) getSupportFragmentManager().findFragmentById(R.id.content_intro));

        pager.setHandler(this);

        //daily.setSearchView((SearchView) toolbar.findViewById(R.id.toolbar_search));

        // player button
        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                header.togglePlayer();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        layout.closeDrawers();
        pager.OnClick(item.getItemId());
        return true;
    }

    @Override
    public void onDateSelected(Date date) {
        onDateSelected(date, null, null);
    }

    @Override
    public void onDateSelected(Date date, String condition, String[] values) {
        SimpleDateFormat frmt = new SimpleDateFormat();
        String pattern;
        boolean hasTitle = false;

        if (date == null)
            return;

        this.date = date;
        this.cursor = db.getDay(date, condition, values);

        // default title
        toolbar.setTitle(getString(R.string.app_title));
        toolbar.setSubtitle(null);
        playerButton.setVisibility(View.GONE);

        if (cursor != null) {
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
                        hasTitle = true;
                        break;

                    // voty title
                    case Database.TYPE_YEAR:
                        if (!hasTitle)
                            toolbar.setTitle(getString(R.string.navigation_voty));
                        break;

                    // intro title
                    case Database.TYPE_INTRO:
                        if (!hasTitle)
                            toolbar.setTitle(getString(R.string.navigation_intro));
                        break;

                    // audio player
                    case Database.TYPE_MEDIA:
                        playerButton.setVisibility(View.VISIBLE);
                        break;

                }
            }
        }

        // update listeners
        for (DataListener tmp : listeners)
            tmp.onDataUpdated(date, cursor);
    }

    @Override
    public void updateDataListener(DataListener listener) {
        listener.onDataUpdated(date, cursor);
    }

    public void registerDataListener(DataListener listener) {
        listeners.add(listener);
        if (date != null && cursor != null)
            listener.onDataUpdated(date, cursor);
    }

    @Override
    public void unregisterDataListener(DataListener listener) {
        listeners.remove(listener);
    }
}