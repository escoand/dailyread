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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.text.DateFormat;
import java.util.Date;

public class FooterContentFragment extends AbstractContentFragment implements OnDateSelectedListener {
    private String shareText;

    public FooterContentFragment() {
        layout = R.layout.item_footer;
        from = new String[]{BaseColumns._ID, BaseColumns._ID, BaseColumns._ID};
        to = new int[]{R.id.button_today, R.id.button_share, R.id.button_bible_exegesis};
        condition = Database.COLUMN_TYPE + "=?";
        values = new String[]{Database.TYPE_EXEGESIS};
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        ListView list = (ListView) super.onCreateView(inflater, container, savedInstanceState);
        list.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return list;
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        switch (view.getId()) {

            // today
            case R.id.button_today:
                view.setOnClickListener(new OnTodayClickListener());
                return true;

            // share
            case R.id.button_share:
                view.setOnClickListener(new OnShareClickListener());
                return true;

            // online bible
            case R.id.button_bible_exegesis:
                view.setOnClickListener(new OnBibleClickListener(getActivity(), date, Database.TYPE_EXEGESIS));
                return true;

            // do nothing
            default:
                break;
        }

        return super.setViewValue(view, cursor, columnIndex);
    }

    @Override
    public void onDateSelected(@NonNull final Date date) {
        final Cursor cursor = Database.getInstance(getContext()).getDay(date,
                Database.COLUMN_TYPE + " IN (?,?)",
                new String[]{Database.TYPE_EXEGESIS, Database.TYPE_DAY});
        String title = null;
        String verse = null;
        String text = null;

        while (cursor.moveToNext()) {
            switch (cursor.getString(cursor.getColumnIndex(Database.COLUMN_TYPE))) {
                case Database.TYPE_EXEGESIS:
                    title = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TITLE));
                    text = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
                    break;
                case Database.TYPE_DAY:
                    verse = cursor.getString(cursor.getColumnIndex(Database.COLUMN_TEXT));
                    break;
                default: // do nothing
                    break;
            }
        }
        cursor.close();

        if (title != null && text != null && verse != null)
            shareText = DateFormat.getDateInstance().format(date) + "\n" +
                    title + " (" + verse + ")\n" + text + "\n" + getString(R.string.app_title);
        else
            shareText = null;

        super.onDateSelected(date);
    }

    private class OnTodayClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View view) {
            DateListenerHandler.getInstance().onDateSelected(new Date());
        }
    }

    private class OnShareClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            if (shareText == null)
                return;

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivityForResult(intent, 0);
        }
    }
}
