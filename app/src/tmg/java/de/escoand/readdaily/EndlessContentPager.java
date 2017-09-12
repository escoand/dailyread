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

import java.util.ArrayList;

public class EndlessContentPager extends ViewPager {
    private final ArrayList<DayContentFragment> pages = new ArrayList<>(3);

    public EndlessContentPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        pages.add(new DayContentFragment());
        pages.add(new DayContentFragment());
        pages.add(new DayContentFragment());

        pages.get(0).setDayOffset(-1);
        pages.get(1).setDayOffset(0);
        pages.get(2).setDayOffset(1);

        setAdapter(new CustomPageAdapter(((AppCompatActivity) context).getSupportFragmentManager()));
        setCurrentItem((pages.size() - 1) / 2, false);
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);

        // end of days
        if (position == 0 && offset == 0 || position == pages.size() - 1) {

            DatePersistence.getInstance().deleteObserver(pages.get(position));
            DatePersistence.getInstance().setDateOffset(position == 0 ? -1 : +1);
            // TODO try to avoid flickering when changing item
            setCurrentItem((pages.size() - 1) / 2, false);
            pages.get(position).update(null, null);
            DatePersistence.getInstance().addObserver(pages.get(position));
        }
    }

    private class CustomPageAdapter extends FragmentPagerAdapter {

        public CustomPageAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return pages.get(position);
        }

        @Override
        public int getCount() {
            return pages.size();
        }
    }
}