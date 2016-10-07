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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
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
    private DailyFragment daily;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        setContentView(R.layout.activity_drawer);

        layout = (DrawerLayout) findViewById(R.id.wrapper);

        // toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(GravityCompat.START);
            }
        });

        // drawer
        ((NavigationView) findViewById(R.id.drawer)).setNavigationItemSelectedListener(this);

        // fragments
        FragmentManager fm = getSupportFragmentManager();
        HeaderFragment header = (HeaderFragment) fm.findFragmentById(R.id.header);
        daily = (DailyFragment) fm.findFragmentById(R.id.content);
        FooterFragment footer = (FooterFragment) fm.findFragmentById(R.id.footer);
        header.setOnClickListener(daily);
        footer.setOnClickListener(daily);
        daily.registerDataListener(this);
        daily.registerDataListener(header);
        daily.registerDataListener(footer);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        daily.setDate(daily.getDate());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        layout.closeDrawers();
        daily.onClick(item.getItemId(), null);
        return true;
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onDataUpdated(Date date, Cursor cursor) {
        SimpleDateFormat frmt = new SimpleDateFormat();
        String pattern;

        // default title
        toolbar.setTitle(getString(R.string.app_title));
        toolbar.setSubtitle(null);

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
                    return;

                // year title
                case Database.TYPE_YEAR:
                    toolbar.setTitle(getString(R.string.navigation_voty));
                    break;

                // intro title
                case Database.TYPE_INTRO:
                    toolbar.setTitle(getString(R.string.navigation_intro));
                    break;

            }
        }
    }
}