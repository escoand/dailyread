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
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class EndlessContentPager extends ViewPager {
    private final ArrayList<DayContentFragment> pages = new ArrayList<>(3);
    private final InstantScroller scroller;
    private int delayedPage = -1;

    public EndlessContentPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        pages.add(new DayContentFragment());
        pages.add(new DayContentFragment());
        pages.add(new DayContentFragment());

        pages.get(0).setDayOffset(-1);
        pages.get(1).setDayOffset(0);
        pages.get(2).setDayOffset(1);

        setAdapter(new CustomPageAdapter(((AppCompatActivity) context).getSupportFragmentManager()));
        setCurrentItem((pages.size() - 1) / 2, false);

        scroller = new InstantScroller(context);
        try {
            final Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            field.set(this, scroller);
        } catch (Exception e) {
            LogHandler.log(e);
        }
    }

    private void delayedPageUpdate() {
        if (delayedPage >= 0 && delayedPage < pages.size())
            pages.get(delayedPage).update(null, null);
    }

    @Override
    protected void onPageScrolled(final int position, final float offset, final int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        if (position == 0 && offset == 0 || position == pages.size() - 1) {
            DatePersistence.getInstance().deleteObserver(pages.get(position));
            DatePersistence.getInstance().setDateOffset(position == 0 ? -1 : +1);
            DatePersistence.getInstance().addObserver(pages.get(position));
            delayedPage = position;
            scroller.setScrollInstantly(true);
            setCurrentItem((pages.size() - 1) / 2, true);
        }
    }

    private class CustomPageAdapter extends FragmentPagerAdapter {

        public CustomPageAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(final int position) {
            return pages.get(position);
        }

        @Override
        public int getCount() {
            return pages.size();
        }
    }

    private class InstantScroller extends Scroller {
        private boolean scrollInstantly = false;
        private Field currX = null;
        private Field currY = null;

        public InstantScroller(final Context context) {
            super(context);
            try {
                currX = Scroller.class.getDeclaredField("mCurrX");
                currY = Scroller.class.getDeclaredField("mCurrY");
                currX.setAccessible(true);
                currY.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setScrollInstantly(final boolean instantly) {
            scrollInstantly = instantly;
        }

        @Override
        public boolean computeScrollOffset() {
            if (scrollInstantly && currX != null && currY != null) {
                try {
                    currX.set(this, this.getFinalX());
                    currY.set(this, this.getFinalY());
                    this.forceFinished(true);
                    scrollInstantly = false;
                    EndlessContentPager.this.delayedPageUpdate();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return super.computeScrollOffset();
        }
    }
}