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
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener {
    private DrawerLayout layout;
    private Toolbar toolbar;
    private HeaderFragment header;
    private DailyFragment daily;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // bottom sheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setHideable(false);

        // fragments
        FragmentManager fm = getSupportFragmentManager();
        header = (HeaderFragment) fm.findFragmentById(R.id.header);
        daily = (DailyFragment) fm.findFragmentById(R.id.content);
        header.setOnClickListener(daily);
        daily.setHeaderInterface(header);
        onDateSelected(new Date());
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
    public boolean onNavigationItemSelected(MenuItem item) {
        DialogFragment dialog = null;

        switch (item.getItemId()) {

            // today
            case R.id.navigation_today:
                onDateSelected(new Date());
                break;

            // calendar
            case R.id.navigation_calendar:
                dialog = new CalendarDialogFragment();
                ((CalendarDialogFragment) dialog).setOnDateSelectedListener(this);
                break;

            // list
            case R.id.navigation_list:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_TEXT + "!=''", new String[]{Database.TYPE_DAY});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(this);
                break;
            case R.id.navigation_intro:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_SOURCE + "!=''", new String[]{Database.TYPE_INTRO});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(this);
                break;
            case R.id.navigation_voty:
                dialog = new ListDialogFragment();
                ((ListDialogFragment) dialog).setFilter(Database.COLUMN_TYPE + "=? AND " + Database.COLUMN_SOURCE + "!=''", new String[]{Database.TYPE_YEAR});
                ((ListDialogFragment) dialog).setOnDateSelectedListener(this);
                break;

            // store
            case R.id.navigation_store:
                startActivityForResult(new Intent(getApplication(), StoreActivity.class), 0);
                break;

            // about
            case R.id.navigation_about:
                startActivity(new Intent(getApplication(), AboutActivity.class));
                break;
        }

        if (dialog != null)
            dialog.show(getSupportFragmentManager(), "dialog");

        layout.closeDrawers();

        return false;
    }

    @Override
    public void onDateSelected(Date date) {
        SimpleDateFormat frmt = new SimpleDateFormat();
        String pattern;

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

        daily.setDate(date);
    }
}