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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.Date;

public class HeaderFragment extends Fragment implements DataListener, View.OnClickListener, Runnable {
    private View root;
    private View playerControls;
    private ImageView playerImage;
    private View playerContainer;
    private ProgressBar playerProgress;
    private TextView playerText;
    private LinearLayout texts;
    private TextView title;
    private TextView subtitle;
    private View bible;

    private MediaPlayer player = null;

    private boolean isLarge = false;
    private int smallWidth = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_header, container);
        playerControls = root.findViewById(R.id.player_control);
        playerImage = (ImageView) root.findViewById(R.id.player_image);
        playerContainer = root.findViewById(R.id.player_container);
        playerProgress = (ProgressBar) root.findViewById(R.id.player_progress);
        playerText = (TextView) root.findViewById(R.id.player_text);
        texts = (LinearLayout) root.findViewById(R.id.header_texts);
        title = (TextView) root.findViewById(R.id.header_title);
        subtitle = (TextView) root.findViewById(R.id.header_subtitle);
        bible = root.findViewById(R.id.button_bible_day);

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
                    playerImage.setImageResource(R.mipmap.img_month_01);
                    break;
                case 1:
                    playerImage.setImageResource(R.mipmap.img_month_02);
                    break;
                case 2:
                    playerImage.setImageResource(R.mipmap.img_month_03);
                    break;
                case 3:
                    playerImage.setImageResource(R.mipmap.img_month_04);
                    break;
                case 4:
                    playerImage.setImageResource(R.mipmap.img_month_05);
                    break;
                case 5:
                    playerImage.setImageResource(R.mipmap.img_month_06);
                    break;
                case 6:
                    playerImage.setImageResource(R.mipmap.img_month_07);
                    break;
                case 7:
                    playerImage.setImageResource(R.mipmap.img_month_08);
                    break;
                case 8:
                    playerImage.setImageResource(R.mipmap.img_month_09);
                    break;
                case 9:
                    playerImage.setImageResource(R.mipmap.img_month_10);
                    break;
                case 10:
                    playerImage.setImageResource(R.mipmap.img_month_11);
                    break;
                case 11:
                    playerImage.setImageResource(R.mipmap.img_month_12);
                    break;
            }
            this.title.setText(title);
            this.subtitle.setText(subtitle);
            getView().setVisibility(View.VISIBLE);
        } else
            getView().setVisibility(View.GONE);

        // audio file
        File file = new File(getActivity().getFilesDir(), Database.getIntFromDate(date) + ".mp3");
        if (file.exists()) {
            playerImage.setOnClickListener(this);
            player = MediaPlayer.create(getContext(), Uri.parse(file.getAbsolutePath()));
        } else {
            playerImage.setOnClickListener(null);
            if (player != null)
                player.release();
            player = null;
        }
    }

    @Override
    public void onClick(View view) {
        togglePlayer();
    }

    @Override
    public void run() {
        while (player != null) {
            final int duration = player.getDuration();
            final int progress = player.getCurrentPosition();
            final String text = String.format("%02d:%02d", progress / 60000, (progress / 1000) % 60);

            // update player ui
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playerProgress.setMax(duration);
                    playerProgress.setProgress(progress);
                    playerProgress.setSecondaryProgress(duration);
                    playerText.setText(text);
                }
            });

            // TODO close player on finish

            // sleep 1 second
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                return;
            }
        }
    }

    private void togglePlayer() {
        AnimatorSet anims = new AnimatorSet();
        Animator anim1 = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_in);
        Animator anim2 = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_out);
        Animator anim3;
        ValueAnimator anim4, anim5;

        if (smallWidth == 0)
            smallWidth = root.getMeasuredWidth();

        // animations
        if (!isLarge) {
            anim1.setTarget(playerContainer);
            anim2.setTarget(texts);
            anim3 = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_half_out);
            anim4 = new WidthResizeAnimator(root, smallWidth, root.getMeasuredWidth() + texts.getMeasuredWidth());
            anim5 = new SquareResizeAnimator(playerControls, playerControls.getMeasuredWidth(), playerControls.getMeasuredHeight() * 2);
        } else {
            anim1.setTarget(texts);
            anim2.setTarget(playerContainer);
            anim3 = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_half_in);
            anim4 = new WidthResizeAnimator(root, root.getMeasuredWidth(), smallWidth);
            anim5 = new SquareResizeAnimator(playerControls, playerControls.getMeasuredWidth(), playerControls.getMeasuredHeight() / 2);
        }
        anim3.setTarget(playerImage);

        // animate
        anims.playTogether(anim1, anim2, anim3, anim4, anim5);
        anims.addListener(new AnimListener());
        anims.setDuration(500);
        anims.setInterpolator(new DecelerateInterpolator());
        anims.start();
        new Thread(this).start();
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

    private class AnimListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
            if (!isLarge) {
                ViewGroup.LayoutParams lp = texts.getLayoutParams();
                lp.height = texts.getMeasuredHeight();
                lp.width = texts.getMeasuredWidth();
                texts.setLayoutParams(lp);
            } else {
                player.pause();
                player.seekTo(0);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!isLarge) {
                player.start();
            } else {
                texts.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }

            isLarge = !isLarge;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
