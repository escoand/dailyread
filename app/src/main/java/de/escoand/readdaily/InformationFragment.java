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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

public class InformationFragment extends Fragment implements DataListener {
    private String intro_title;
    private String intro_text;
    private String voty_title;
    private String voty_text;

    private TextView title;
    private TextView text;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.item_daily, container, false);
        title = (TextView) v.findViewById(R.id.daily_title);
        text = (TextView) v.findViewById(R.id.daily_text);
        v.findViewById(R.id.daily_source).setVisibility(View.GONE);
        return v;
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
    }

    @Override
    public void onDataUpdated(@Nullable Date date, @Nullable Cursor cursor) {
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            switch (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE))) {

                case Database.TYPE_INTRO:
                    intro_title = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TITLE));
                    intro_text = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
                    break;

                case Database.TYPE_YEAR:
                    voty_title = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TITLE));
                    voty_text = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
                    break;
            }
        }
    }

    public void showText(String type) {
        switch (type) {

            case Database.TYPE_INTRO:
                title.setText(intro_title);
                text.setText(Html.fromHtml(intro_text).toString());
                break;

            case Database.TYPE_YEAR:
                title.setText(voty_title);
                text.setText(Html.fromHtml(voty_text).toString());
                break;
        }
    }
}
