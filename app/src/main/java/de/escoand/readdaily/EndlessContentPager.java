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
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class EndlessContentPager extends ViewPager {
    private HashMap<Integer, DayContentFragment> fragments = new HashMap<>();
    private ArrayList<OnDateSelectedListener> listeners = new ArrayList<>();

    public EndlessContentPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAdapter(new EndlessDayPagerAdapter(((AppCompatActivity) context).getSupportFragmentManager()));
        setCurrentItem(getAdapter().getCount() / 2, false);
    }

    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set initial date
        if (savedInstanceState != null) {
            date = Database.getDateFromInt(savedInstanceState.getInt("date"));
        } else if (date == null)
            date = new Date();
    }

    @Override
    public void onResume() {
        onDateSelected(date);
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("date", Database.getIntFromDate(date));
    }*/

    public Date getDateOfPosition(int position) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, position - getAdapter().getCount() / 2);
        return calendar.getTime();
    }

    public int getPositionOfDate(Date date) {
        return (int) (new Date().getTime() - date.getTime());
    }

    public DayContentFragment getCurrentFragment() {
        return fragments.get(getCurrentItem());
    }

    public void addDataListener(OnDateSelectedListener listener) {
        listeners.add(listener);
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        Date date = getDateOfPosition(position);

        super.onPageScrolled(position, offset, offsetPixels);

        for (OnDateSelectedListener tmp : listeners)
            if (tmp != null && date != null)
                tmp.onDateSelected(date);
    }

    private class EndlessDayPagerAdapter extends FragmentPagerAdapter {
        public EndlessDayPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Fragment getItem(int position) {
            final DayContentFragment content = new DayContentFragment();
            content.onDateSelected(getDateOfPosition(position));
            fragments.put(position, content);
            return content;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            fragments.remove(position);
        }
    }
}
