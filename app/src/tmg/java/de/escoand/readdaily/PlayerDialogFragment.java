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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

public class PlayerDialogFragment extends DialogFragment implements Runnable, MediaPlayer.OnCompletionListener {
    private static final String STATE_DATE = "date";
    private static final String STATE_POSITION = "position";

    private ProgressBar progressBar;
    private TextView progressText;

    private Date date;
    @DrawableRes
    private int image = -1;
    private String title;
    private MediaPlayer player;

    @Override
    @NonNull
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final View root = getActivity().getLayoutInflater().inflate(R.layout.fragment_player, null);
        final TextView playerTitle = (TextView) root.findViewById(R.id.player_title);
        final ImageView playerImage = (ImageView) root.findViewById(R.id.player_image);
        progressBar = (ProgressBar) root.findViewById(R.id.player_progress);
        progressText = (TextView) root.findViewById(R.id.player_text);

        if (savedInstanceState != null) {
            setDate(getContext(), Database.getDateFromInt(savedInstanceState.getInt(STATE_DATE)));
            player.seekTo(savedInstanceState.getInt(STATE_POSITION));
        }

        if (image > 0) {
            final Bitmap tmp = BitmapFactory.decodeResource(getResources(), image);
            final int width = Math.round(tmp.getWidth() / 100);
            final int height = Math.round(tmp.getHeight() / 100);
            root.setBackgroundDrawable(new BitmapDrawable(Bitmap.createScaledBitmap(tmp, width, height, false)));
            playerImage.setImageResource(image);
        }

        playerTitle.setText(title);

        return new AlertDialog.Builder(getContext())
                .setView(root)
                .create();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        if (player != null) {
            outState.putInt(STATE_DATE, Database.getIntFromDate(date));
            outState.putInt(STATE_POSITION, player.getCurrentPosition());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (player != null) {
            player.start();
            new Thread(this).start();
        } else
            dismiss();
    }

    @Override
    public void onDestroyView() {
        if (player != null)
            player.release();
        super.onDestroyView();
    }

    public void setDate(@NonNull final Context context, @NonNull final Date date) {
        this.date = date;

        // image
        switch (date.getMonth()) {
            case 0:
                image = R.mipmap.img_month_01;
                break;
            case 1:
                image = R.mipmap.img_month_02;
                break;
            case 2:
                image = R.mipmap.img_month_03;
                break;
            case 3:
                image = R.mipmap.img_month_04;
                break;
            case 4:
                image = R.mipmap.img_month_05;
                break;
            case 5:
                image = R.mipmap.img_month_06;
                break;
            case 6:
                image = R.mipmap.img_month_07;
                break;
            case 7:
                image = R.mipmap.img_month_08;
                break;
            case 8:
                image = R.mipmap.img_month_09;
                break;
            case 9:
                image = R.mipmap.img_month_10;
                break;
            case 10:
                image = R.mipmap.img_month_11;
                break;
            case 11:
                image = R.mipmap.img_month_12;
                break;
            default: // do nothing
                break;
        }

        Cursor c = Database.getInstance(context).getDay(date, Database.COLUMN_TYPE + " IN (?,?)", new String[]{Database.TYPE_EXEGESIS, Database.TYPE_MEDIA});
        while (c.moveToNext())
            switch (c.getString(c.getColumnIndex(Database.COLUMN_TYPE))) {

                // title
                case Database.TYPE_EXEGESIS:
                    title = c.getString(c.getColumnIndex(Database.COLUMN_TITLE));
                    break;

                // media
                case Database.TYPE_MEDIA:
                    player = MediaPlayer.create(context, Uri.parse(c.getString(c.getColumnIndex(Database.COLUMN_SOURCE))));
                    player.setOnCompletionListener(this);
                    break;

                // do nothing
                default:
                    break;
            }
        c.close();
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
        dismiss();
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
                        progressBar.setMax(duration);
                        progressBar.setProgress(progress);
                        progressBar.setSecondaryProgress(duration);
                        progressText.setText(text);
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
