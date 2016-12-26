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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.Date;

public class OnBibleClickListener implements View.OnClickListener {
    private Activity context;
    private Date date;
    private String type;

    public OnBibleClickListener(@NonNull final Activity context, @NonNull final Date date, @NonNull final String type) {
        super();
        this.context = context;
        this.date = date;
        this.type = type;
    }

    @Override
    public void onClick(final View v) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        Cursor cursor = Database.getInstance(context).getDay(date, Database.COLUMN_TYPE + "=?", new String[]{type});

        if (cursor.moveToFirst()) {
            String verse = cursor.getString(cursor.getColumnIndex(Database.COLUMN_SOURCE));
            String url = context.getString(R.string.url_bible)
                    + settings.getString("bible_translation", "LUT") + "/"
                    + verse.replaceAll(" ", "");
            Intent intent = new Intent();

            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        }
    }
}

