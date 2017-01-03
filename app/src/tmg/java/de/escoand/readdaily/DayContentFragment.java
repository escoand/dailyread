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

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;

public class DayContentFragment extends AbstractContentFragment {
    private View header;

    public DayContentFragment() {
        condition = Database.COLUMN_TYPE + " IN (?,?)";
        values = new String[]{Database.TYPE_WEEK, Database.TYPE_EXEGESIS};
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final FrameLayout root = new FrameLayout(getContext());
        final ListView list = (ListView) super.onCreateView(inflater, container, savedInstanceState);
        final View empty = inflater.inflate(R.layout.list_empty, container, false);

        // header
        header = inflater.inflate(R.layout.item_header, container, false);
        list.addHeaderView(header);
        updateHeader();

        list.setDivider(null);
        list.setEmptyView(empty);
        root.addView(list);
        root.addView(empty);

        return root;
    }

    @Override
    public void onDateSelected(@NonNull Date date) {
        super.onDateSelected(date);
        updateHeader();
    }

    private void updateHeader() {
        if (date == null || header == null)
            return;

        final Cursor c = Database.getInstance(getContext()).getDay(date, Database.COLUMN_TYPE + "=?", new String[]{Database.TYPE_DAY});
        final ImageView image = (ImageView) header.findViewById(R.id.player_image);
        final TextView title = (TextView) header.findViewById(R.id.header_title);
        final TextView source = (TextView) header.findViewById(R.id.header_source);
        final View button = header.findViewById(R.id.button_bible_day);

        if (!c.moveToFirst()) {
            header.setVisibility(View.GONE);
            return;
        }

        header.setVisibility(View.VISIBLE);

        // image
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
            default: // do nothing
                break;
        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                PlayerDialogFragment player = new PlayerDialogFragment();
                player.setDate(getContext(), date);
                player.show(getFragmentManager(), player.getClass().getName());
            }
        });

        // text
        title.setText(Html.fromHtml(c.getString(c.getColumnIndex(Database.COLUMN_TEXT))));
        source.setText(c.getString(c.getColumnIndex(Database.COLUMN_SOURCE)));

        // button
        button.setOnClickListener(new OnBibleClickListener(getActivity(), date, Database.TYPE_DAY));
    }
}
