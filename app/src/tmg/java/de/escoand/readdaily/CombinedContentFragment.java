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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

public class CombinedContentFragment extends Fragment implements OnDateSelectedListener {
    private HeaderContentFragment header;
    private DayContentFragment day;
    private Date date;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_content, container, false);

        header = (HeaderContentFragment) getChildFragmentManager().findFragmentById(R.id.content_header);
        day = (DayContentFragment) getChildFragmentManager().findFragmentById(R.id.content_day);

        if (date != null) {
            header.onDateSelected(date);
            day.onDateSelected(date);
        }

        return v;
    }

    @Override
    public void onDateSelected(@NonNull final Date date) {
        this.date = date;

        if (header == null || day == null)
            return;

        header.onDateSelected(date);
        day.onDateSelected(date);
    }
}
