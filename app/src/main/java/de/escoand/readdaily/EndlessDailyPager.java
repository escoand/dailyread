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

import java.util.Calendar;
import java.util.GregorianCalendar;

public class EndlessDailyPager extends ViewPager {
    private OnDateSelectedListener handler = null;

    public EndlessDailyPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAdapter(new EndlessDailyPagerAdapter(((AppCompatActivity) context).getSupportFragmentManager()));
        setCurrentItem(Integer.MAX_VALUE / 2, false);
    }

    public void OnClick(int id) {
        //((DataHandler) adapter.getItem(getCurrentItem())).onClick(id, null);
    }

    public void setHandler(OnDateSelectedListener listener) {
        handler = listener;
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
            final DayContentFragment content = new DayContentFragment();

            calendar.add(Calendar.DATE, position - getCount() / 2);
            Log.e("date", calendar.getTime().toString());

            if (handler != null) {
                handler.registerDataListener(content);
                handler.onDateSelected(calendar.getTime());
            }

            return content;
        }
    }
}
