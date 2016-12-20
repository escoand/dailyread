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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class EndlessContentPager extends ViewPager implements OnDateSelectedListener {
    private HashMap<Integer, CombinedContentFragment> fragments = new HashMap<>();
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

    private Date getDateOfPosition(int position) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, position - getAdapter().getCount() / 2);
        return calendar.getTime();
    }

    private int getPositionOfDate(Date date) {
        return (int) (getAdapter().getCount() / 2 + (date.getTime() - new Date().getTime()) / 24 / 60 / 60 / 1000);
    }

    public OnClickListener getCurrentOnPlayClickListener() {
        if (fragments.containsKey(getCurrentItem()))
            return fragments.get(getCurrentItem()).getOnPlayClickListener();
        return null;
    }

    public void addDataListener(OnDateSelectedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onDateSelected(@NonNull Date date) {
        setCurrentItem(getPositionOfDate(date), false);
    }

    @Override
    public void onDateSelected(@NonNull Date date, @Nullable String condition, @Nullable String[] values) {
        onDateSelected(date);
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
            return 5000;
        }

        @Override
        public Fragment getItem(int position) {
            final CombinedContentFragment content = new CombinedContentFragment();
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
