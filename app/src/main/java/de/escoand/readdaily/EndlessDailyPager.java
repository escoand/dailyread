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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class EndlessDailyPager extends ViewPager {
    private EndlessDailyPagerAdapter adapter;
    private ArrayList<DataListener> listeners = new ArrayList<>();

    public EndlessDailyPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        adapter = new EndlessDailyPagerAdapter(((AppCompatActivity) context).getSupportFragmentManager());
        setAdapter(adapter);
        setCurrentItem(Integer.MAX_VALUE / 2, false);
    }

    public void addDataListener(DataListener listener) {
        listeners.add(listener);
    }

    public void OnClick(int id) {
        ((DailyFragment) adapter.getItem(getCurrentItem())).onClick(id, null);
    }

    private class EndlessDailyPagerAdapter extends FragmentPagerAdapter {
        public EndlessDailyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Fragment getItem(int position) {
            final GregorianCalendar calendar = new GregorianCalendar();
            final DailyFragment daily = new DailyFragment();

            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).setOnClickListener(daily);
                daily.registerDataListener(listeners.get(i));
            }

            calendar.add(Calendar.DATE, position - getCount() / 2);
            daily.onDateSelected(calendar.getTime());
            Log.e("date", calendar.getTime().toString());

            return daily;
        }
    }

}
