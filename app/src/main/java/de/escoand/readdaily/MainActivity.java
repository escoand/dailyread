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

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DataListener {
    private DrawerLayout layout;
    private Toolbar toolbar;
    private View playerButton;
    private EndlessDailyPager pager;
    private HeaderFragment header;
    private FooterFragment footer;
    private InformationFragment info;

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
        header = (HeaderFragment) getSupportFragmentManager().findFragmentById(R.id.header);
        footer = (FooterFragment) getSupportFragmentManager().findFragmentById(R.id.footer);
        info = (InformationFragment) getSupportFragmentManager().findFragmentById(R.id.content_info);
        pager.addDataListener(this);
        pager.addDataListener(header);
        pager.addDataListener(footer);
        pager.addDataListener(info);

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
    public void setOnClickListener(View.OnClickListener listener) {
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onDataUpdated(@Nullable Date date, @Nullable Cursor cursor) {
        SimpleDateFormat frmt = new SimpleDateFormat();
        String pattern;
        boolean hasTitle = false;

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

                    // year title
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
    }
}