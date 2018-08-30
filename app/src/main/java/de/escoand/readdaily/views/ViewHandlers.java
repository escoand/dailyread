/*
 * Copyright (c) 2018 escoand.
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

package de.escoand.readdaily.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;

import de.escoand.readdaily.R;
import de.escoand.readdaily.database.entity.TextInfo;

public class ViewHandlers {

    public void onBibleClick(View view, TextInfo textInfo) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        final String url = view.getContext().getString(R.string.url_bible)
                + settings.getString("bible_translation", "LUT") + "/"
                + textInfo.text.getSource().replaceAll(" ", "");
        final Intent intent = new Intent();

        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        view.getContext().startActivity(intent);
    }

}
