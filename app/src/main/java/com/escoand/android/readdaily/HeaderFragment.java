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

package com.escoand.android.readdaily;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HeaderFragment extends Fragment implements HeaderInterface {
    private ViewGroup root;
    private TextView title;
    private TextView subtitle;
    private View bible;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_header, container);
        title = (TextView) root.findViewById(R.id.header_title);
        subtitle = (TextView) root.findViewById(R.id.header_subtitle);
        bible = root.findViewById(R.id.read_bible);
        return root;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        bible.setOnClickListener(listener);
    }

    @Override
    public void updateHeader(Cursor cursor) {
        String title = null;
        String subtitle = null;

        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_EXEGESIS))
                title = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TITLE));
            else if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE)).equals(Database.TYPE_DAY))
                subtitle = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
        }

        if (this.title != null && title != null && this.subtitle != null && subtitle != null) {
            this.title.setText(title);
            this.subtitle.setText(subtitle);
            root.setVisibility(View.VISIBLE);
        } else
            root.setVisibility(View.GONE);
    }
}
