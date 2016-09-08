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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

public class FooterFragment extends Fragment implements DataListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_footer, container);
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        getView().findViewById(R.id.footer_today).setOnClickListener(listener);
        getView().findViewById(R.id.footer_share).setOnClickListener(listener);
        getView().findViewById(R.id.footer_bible).setOnClickListener(listener);
    }

    @Override
    public void onDataUpdated(Date date, Cursor cursor) {
        if (cursor.getCount() > 0)
            getView().setVisibility(View.VISIBLE);
        else
            getView().setVisibility(View.GONE);
    }
}
