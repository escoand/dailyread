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

package com.escoand.android.daily_read;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rey.material.app.DatePickerDialog;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
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
        toolbar.setNavigationIcon(R.drawable.toolbar_menu);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.openDrawer(GravityCompat.START);
            }
        });

        // drawer
        ListView list = (ListView) findViewById(R.id.drawerList);
        list.setAdapter(new DrawerArrayAdapter(this,
                getResources().obtainTypedArray(R.array.drawer_items),
                getResources().obtainTypedArray(R.array.drawer_icons)));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switchFragment(position);
            }
        });

        // content
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        daily = new DailyFragment();
        ft.replace(R.id.content, daily);
        ft.commit();
        setDate(new Date());
    }

    private void switchFragment(int position) {
        FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        Dialog.Builder dialogBuilder = null;

        switch (getResources().obtainTypedArray(R.array.drawer_icons).getResourceId(position, -1)) {

            // today
            case R.drawable.drawer_today:
                setDate(new Date());
                break;

            // calendar
            case R.drawable.drawer_calendar:
                dialogBuilder = new DatePickerDialog.Builder() {
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        DatePickerDialog dialog = (DatePickerDialog) fragment.getDialog();
                        setDate(dialog.getCalendar().getTime());
                        super.onPositiveActionClicked(fragment);
                    }
                };
                ((DatePickerDialog.Builder) dialogBuilder).date(daily.getDate().getTime());
                break;

            // list
            case R.drawable.drawer_list:
                dialogBuilder = new Dialog.Builder() {
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        super.onPositiveActionClicked(fragment);
                    }
                };
                com.rey.material.widget.ListView v = new com.rey.material.widget.ListView(getBaseContext());
                v.setAdapter(new SimpleCursorAdapter(
                        getBaseContext(),
                        R.layout.item_list,
                        new Database(getBaseContext()).getList(),
                        new String[]{Database.COLUMN_SOURCE, Database.COLUMN_DATE},
                        new int[]{R.id.listText, R.id.listDate},
                        0));
                dialogBuilder.getDialog().setContentView(v);
                break;

            // store
            case R.drawable.drawer_store:
                startActivityForResult(new Intent(this, StoreActivity.class), 0);
                break;

            // about
            case R.drawable.drawer_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }

        if (dialogBuilder != null) {
            if (prev != null)
                ft.remove(prev);
            ft.addToBackStack(null);
            dialogBuilder.positiveAction(getString(R.string.button_ok)).negativeAction(getString(R.string.button_cancel));
            new DialogFragment().newInstance(dialogBuilder).show(fm, "dialog");
        }
        layout.closeDrawers();
    }

    private void setDate(Date date) {
        toolbar.setSubtitle(DateFormat.getLongDateFormat(getBaseContext()).format(date));
        daily.setDate(date);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        daily.setDate(daily.getDate());
    }
}
