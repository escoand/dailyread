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

import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

public class HeaderContentFragment extends AbstractContentFragment {

    public HeaderContentFragment() {
        layout = R.layout.item_header;
        from = new String[]{Database.COLUMN_TEXT, Database.COLUMN_SOURCE, BaseColumns._ID, BaseColumns._ID};
        to = new int[]{R.id.header_title, R.id.header_source, R.id.player_image, R.id.button_bible_day};
        condition = Database.COLUMN_TYPE + "=?";
        values = new String[]{Database.TYPE_DAY};
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ListView list = (ListView) super.onCreateView(inflater, container, savedInstanceState);
        list.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return list;
    }

    @Override
    public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
        switch (view.getId()) {

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
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        PlayerDialogFragment player = new PlayerDialogFragment();
                        player.setDate(getContext(), date);
                        player.show(getFragmentManager(), player.getClass().getName());
                    }
                });
                return true;

            // button
            case R.id.button_bible_day:
                view.setOnClickListener(new OnBibleClickListener(getActivity(), date, Database.TYPE_DAY));
                return true;

            // do nothing
            default:
                break;
        }

        return super.setViewValue(view, cursor, columnIndex);
    }
}
