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

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;

public class HeaderFragment extends Fragment implements DataListener, View.OnClickListener {
    private View root;
    private ImageView image;
    private LinearLayout texts;
    private TextView title;
    private TextView subtitle;
    private View bible;

    private boolean isLarge = false;
    private int smallWidth = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_header, container);
        image = (ImageView) root.findViewById(R.id.header_image);
        texts = (LinearLayout) root.findViewById(R.id.header_text);
        title = (TextView) root.findViewById(R.id.header_title);
        subtitle = (TextView) root.findViewById(R.id.header_subtitle);
        bible = root.findViewById(R.id.button_bible_day);

        image.setOnClickListener(this);

        return root;
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        bible.setOnClickListener(listener);
    }

    @Override
    public void onDataUpdated(Date date, Cursor cursor) {
        String title = null;
        String subtitle = null;

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)) != null && cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_DAY)) {
                title = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
                subtitle = cursor.getString(cursor.getColumnIndex(Database.COLUMN_SOURCE));
            }
        }

        if (this.title != null && title != null && this.subtitle != null && subtitle != null) {
            switch (date.getMonth()) {
                case 0:
                    image.setImageResource(R.mipmap.img_month_01);
                    break;
                case 1:
                    image.setImageResource(R.mipmap.img_month_02);
                    break;
                case 2:
                    image.setImageResource(R.mipmap.img_month_03);
                    break;
                case 3:
                    image.setImageResource(R.mipmap.img_month_04);
                    break;
                case 4:
                    image.setImageResource(R.mipmap.img_month_05);
                    break;
                case 5:
                    image.setImageResource(R.mipmap.img_month_06);
                    break;
                case 6:
                    image.setImageResource(R.mipmap.img_month_07);
                    break;
                case 7:
                    image.setImageResource(R.mipmap.img_month_08);
                    break;
                case 8:
                    image.setImageResource(R.mipmap.img_month_09);
                    break;
                case 9:
                    image.setImageResource(R.mipmap.img_month_10);
                    break;
                case 10:
                    image.setImageResource(R.mipmap.img_month_11);
                    break;
                case 11:
                    image.setImageResource(R.mipmap.img_month_12);
                    break;
            }
            this.title.setText(title);
            this.subtitle.setText(subtitle);
            getView().setVisibility(View.VISIBLE);
        } else
            getView().setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        AnimatorSet anims = new AnimatorSet();
        Animator anim1 = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_in);
        Animator anim2 = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_out);
        Animator anim3, anim4, anim5;
        ValueAnimator anim6, anim7, anim8, anim9;

        if (smallWidth == 0)
            smallWidth = root.getMeasuredWidth();

        // alpha animations
        if (!isLarge) {
            anim1.setTarget(getView().findViewById(R.id.header_progress));
            anim2.setTarget(texts);
            anim3 = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_half_out);
            anim4 = anim1.clone();
            anim5 = anim1.clone();
        } else {
            anim1.setTarget(texts);
            anim2.setTarget(getView().findViewById(R.id.header_progress));
            anim3 = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_half_in);
            anim4 = anim2.clone();
            anim5 = anim2.clone();
        }
        anim3.setTarget(getView().findViewById(R.id.header_image));
        anim4.setTarget(getView().findViewById(R.id.header_forward));
        anim5.setTarget(getView().findViewById(R.id.header_rewind));

        // resize animation
        final View cntrl = getView().findViewById(R.id.header_control);
        if (!isLarge) {
            anim6 = new WidthResizeAnimator(root, smallWidth, root.getMeasuredWidth() + texts.getMeasuredWidth());
            anim7 = new WidthResizeAnimator(getView().findViewById(R.id.header_rewind), 0, getView().findViewById(R.id.header_rewind).getMeasuredHeight());
            anim8 = new WidthResizeAnimator(getView().findViewById(R.id.header_forward), 0, getView().findViewById(R.id.header_forward).getMeasuredHeight());
            anim9 = new SquareResizeAnimator(cntrl, cntrl.getMeasuredWidth(), getView().getMeasuredWidth() - 3 * getView().findViewById(R.id.header_rewind).getMeasuredHeight());
        } else {
            anim6 = new WidthResizeAnimator(root, root.getMeasuredWidth(), smallWidth);
            anim7 = new WidthResizeAnimator(getView().findViewById(R.id.header_rewind), getView().findViewById(R.id.header_rewind).getMeasuredWidth(), 0);
            anim8 = new WidthResizeAnimator(getView().findViewById(R.id.header_forward), getView().findViewById(R.id.header_rewind).getMeasuredWidth(), 0);
            anim9 = new SquareResizeAnimator(cntrl, cntrl.getMeasuredWidth(), cntrl.getMeasuredHeight() / 2);
        }

        // text size
        anims.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (isLarge) {
                    ViewGroup.LayoutParams lp = texts.getLayoutParams();
                    lp.height = texts.getMeasuredHeight();
                    lp.width = texts.getMeasuredWidth();
                    texts.setLayoutParams(lp);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isLarge)
                    texts.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        isLarge = !isLarge;

        // animate
        anims.playTogether(anim1, anim2, anim3, anim4, anim5, anim6, anim7, anim8, anim9);
        anims.setDuration(500);
        anims.setInterpolator(new DecelerateInterpolator());
        anims.start();
    }

    private class SquareResizeAnimator extends ValueAnimator {
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

    private class WidthResizeAnimator extends ValueAnimator {
        View view;

        public WidthResizeAnimator(final View view, int start, int end) {
            this.view = view;
            setIntValues(start, end);

            addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ViewGroup.LayoutParams lp = view.getLayoutParams();
                    lp.width = (int) getAnimatedValue();
                    view.setLayoutParams(lp);
                }
            });
        }
    }
}
