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
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnDateSelectedListener {
    private DrawerLayout layout;
    private Toolbar toolbar;
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

        // content
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        daily = new DailyFragment();
        ft.replace(R.id.content, daily);
        ft.commit();
        onDateSelected(new Date());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        daily.setDate(daily.getDate());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
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

        if (dialog != null) {
            Fragment prev = fm.findFragmentByTag("dialog");
            if (prev != null)
                ft.remove(prev);
            ft.addToBackStack(null);
            dialog.show(fm, "dialog");
        }
        layout.closeDrawers();
        return false;
    }

    @Override
    public void onDateSelected(Date date) {
        toolbar.setSubtitle(DateFormat.getLongDateFormat(getBaseContext()).format(date));
        daily.setDate(date);
    }
}
