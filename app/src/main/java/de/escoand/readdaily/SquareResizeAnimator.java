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

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

public class SquareResizeAnimator extends ValueAnimator {
    View view;

    public SquareResizeAnimator(final View view, int start, int end) {
        this.view = view;
        setIntValues(start, end);

        addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.height = (int) getAnimatedValue();
                lp.width = (int) getAnimatedValue();
                view.setLayoutParams(lp);
            }
        });
    }
}
