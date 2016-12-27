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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class EndlessContentPager extends ViewPager implements OnDateSelectedListener {
    private static final int POSITIONS_MAX = 1000;
    private static final int POSITIONS_INTIAL = POSITIONS_MAX / 2;

    private final HashMap<Integer, CombinedContentFragment> fragments = new HashMap<>();

    public EndlessContentPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setAdapter(new EndlessDayPagerAdapter(((AppCompatActivity) context).getSupportFragmentManager()));
        setCurrentItem(POSITIONS_INTIAL, false);
    }

    private Date getDateOfPosition(final int position) {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, position - POSITIONS_INTIAL);
        return calendar.getTime();
    }

    private int getPositionOfDate(final Date date) {
        return (int) Math.ceil(POSITIONS_INTIAL +
                (date.getTime() - new Date().getTime()) / 24 / 60 / 60 / 1000.0);
    }

    @Override
    public void onDateSelected(@NonNull final Date date) {
        setCurrentItem(getPositionOfDate(date), false);
    }

    @Override
    protected void onPageScrolled(final int position, final float offset, final int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        DateListenerHandler.getInstance().onDateSelected(getDateOfPosition(position), this);
    }

    private class EndlessDayPagerAdapter extends FragmentStatePagerAdapter {
        public EndlessDayPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return POSITIONS_MAX;
        }

        @Override
        public Fragment getItem(final int position) {
            final CombinedContentFragment content = new CombinedContentFragment();
            content.onDateSelected(getDateOfPosition(position));
            fragments.put(position, content);
            return content;
        }

        @Override
        public void destroyItem(final ViewGroup container, final int position, final Object object) {
            super.destroyItem(container, position, object);
            fragments.remove(position);
        }
    }
}
