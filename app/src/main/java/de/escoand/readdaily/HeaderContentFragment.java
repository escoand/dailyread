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
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

public class HeaderContentFragment extends AbstractContentFragment {
    private ProgressBar playerProgress;
    private TextView playerText;
    private LinearLayout texts;

    private MediaPlayer player = null;

    private boolean isLarge = false;
    private int smallWidth = 0;

    public HeaderContentFragment() {
        layout = R.layout.item_header;
        from = new String[]{Database.COLUMN_TEXT, Database.COLUMN_SOURCE, "_id",
                "_id", "_id", "_id", "_id"};
        to = new int[]{R.id.header_title, R.id.header_source, R.id.player_image, R.id.button_bible_day,
                R.id.player_progress, R.id.player_text, R.id.header_texts};
        condition = Database.COLUMN_TYPE + "=?";
        values = new String[]{Database.TYPE_DAY};
    }

    @Override
    public void onDateSelected(@NonNull final Date date) {
        super.onDateSelected(date);

        Cursor c = Database.getInstance(getContext()).getDay(date, Database.COLUMN_TYPE + "=?", new String[]{Database.TYPE_MEDIA});
        if (c.moveToFirst()) {
            player = MediaPlayer.create(getContext(), Uri.parse(c.getString(c.getColumnIndex(Database.COLUMN_SOURCE))));
            player.setOnCompletionListener(new MediaPlayerHandler());
        }
        c.close();
    }

    @Override
    public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
        switch (view.getId()) {

            // player
            case R.id.player_progress:
                playerProgress = (ProgressBar) view;
                return true;
            case R.id.player_text:
                playerText = (TextView) view;
                return true;
            case R.id.header_texts:
                texts = (LinearLayout) view;
                return true;
            case R.id.button_bible_day:
                view.setOnClickListener(new OnBibleClickListener(getActivity(), date, Database.TYPE_DAY));
                return true;

            // image
            case R.id.player_image:
                switch (date.getMonth()) {
                    case 0:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_01);
                        break;
                    case 1:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_02);
                        break;
                    case 2:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_03);
                        break;
                    case 3:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_04);
                        break;
                    case 4:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_05);
                        break;
                    case 5:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_06);
                        break;
                    case 6:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_07);
                        break;
                    case 7:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_08);
                        break;
                    case 8:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_09);
                        break;
                    case 9:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_10);
                        break;
                    case 10:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_11);
                        break;
                    case 11:
                        ((ImageView) view).setImageResource(R.mipmap.img_month_12);
                        break;
                    default: // do nothing
                        break;
                }
                view.setOnClickListener(new MediaPlayerHandler());
                return true;

            // do nothing
            default:
                break;
        }

        return false;
    }

    public View.OnClickListener getOnPlayClickListener() {
        return new MediaPlayerHandler();
    }

    private class MediaPlayerHandler implements View.OnClickListener, MediaPlayer.OnCompletionListener, Runnable {
        @Override
        public void onClick(final View v) {
            View root = v.getRootView();
            View playerControls = root.findViewById(R.id.player_control);
            ImageView playerImage = (ImageView) root.findViewById(R.id.player_image);
            View playerContainer = root.findViewById(R.id.player_container);

            if (player == null)
                return;

            AnimatorSet anims = new AnimatorSet();
            Animator anim1 = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_in);
            Animator anim2 = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_out);
            Animator anim3;
            ValueAnimator anim4;
            ValueAnimator anim5;

            if (smallWidth == 0)
                smallWidth = root.getMeasuredWidth();

            // animations
            if (!isLarge) {
                anim1.setTarget(playerContainer);
                anim2.setTarget(texts);
                anim3 = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_half_out);
                anim4 = new WidthResizeAnimator(root, smallWidth, root.getMeasuredWidth() + texts.getMeasuredWidth());
                anim5 = new SquareResizeAnimator(playerControls, playerControls.getMeasuredWidth(), playerControls.getMeasuredHeight() * 2);
            } else {
                anim1.setTarget(texts);
                anim2.setTarget(playerContainer);
                anim3 = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_half_in);
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

        @Override
        public void onCompletion(MediaPlayer mp) {
            onClick(null);
        }

        @Override
        public void run() {
            while (getActivity() != null && player != null) {
                try {
                    final int duration = player.getDuration();
                    final int progress = player.getCurrentPosition();
                    final String text = String.format(Locale.getDefault(), "%02d:%02d", progress / 60000, (progress / 1000) % 60);

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

                    // sleep 1 second
                    Thread.sleep(1000);
                } catch (Exception e) {
                    return;
                }
            }
        }
    }

    private class SquareResizeAnimator extends ValueAnimator {
        public SquareResizeAnimator(final View view, int start, int end) {
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
        public WidthResizeAnimator(final View view, int start, int end) {
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
            // empty, but must be implemented
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            // empty, but must be implemented
        }
    }
}
