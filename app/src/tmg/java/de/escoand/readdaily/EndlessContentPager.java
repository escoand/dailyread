/*
 * Copyright (c) 2017 escoand.
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
import android.view.MotionEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.Observer;

public class EndlessContentPager extends ViewPager implements Observer {
    private final static int CENTER_PAGE = Integer.MAX_VALUE / 2;
    private boolean isScrolling = false;

    public EndlessContentPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setAdapter(new CustomPageAdapter(((AppCompatActivity) context).getSupportFragmentManager()));
        setCurrentItem(CENTER_PAGE, false);
    }


    @Override
    protected void onPageScrolled(final int position, final float offset, final int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        if (offset == 0) {
            final GregorianCalendar calendar = new GregorianCalendar();
            calendar.add(Calendar.DATE, getCurrentItem() - CENTER_PAGE);
            DatePersistence.getInstance().deleteObserver(this);
            DatePersistence.getInstance().setDate(calendar.getTime());
            DatePersistence.getInstance().addObserver(this);
            isScrolling = false;
        } else
            isScrolling = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScrolling && ev.getAction() == MotionEvent.ACTION_DOWN)
            return false;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isScrolling && ev.getAction() == MotionEvent.ACTION_DOWN)
            return false;
        return super.onTouchEvent(ev);
    }

    @Override
    public void update(Observable o, Object arg) {
        final Date cur = new Date();
        final Date set = DatePersistence.getInstance().getDate();
        final int diff = (int) ((set.getTime() - cur.getTime()) / (1000 * 60 * 60 * 24));
        DatePersistence.getInstance().deleteObservers();
        setCurrentItem(CENTER_PAGE + diff, false);
        DatePersistence.getInstance().restoreObservers();
        DatePersistence.getInstance().notifyObservers();
    }

    private class CustomPageAdapter extends FragmentPagerAdapter {

        CustomPageAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            final DayContentFragment fragment = new DayContentFragment();
            final GregorianCalendar calendar = new GregorianCalendar();
            calendar.add(Calendar.DATE, position - CENTER_PAGE);
            fragment.setDateOnCreate(calendar.getTime());
            return fragment;
        }

        @Override
        public int getCount() {
            return CENTER_PAGE * 2;
        }
    }
}