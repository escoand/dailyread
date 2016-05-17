/*
 * Copyright (C) 2016  escoand
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

package com.escoand.android.daily_read;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerArrayAdapter extends ArrayAdapter<DrawerListItem> {
    public DrawerArrayAdapter(Context context, TypedArray items, TypedArray icons) {
        super(context, R.layout.item_drawer);
        for (int i = 0; i < items.length(); i++)
            this.add(new DrawerListItem(items.getString(i), icons.getDrawable(i)));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;

        if (!getItem(position).text.isEmpty() && getItem(position).icon != null) {
            LayoutInflater i = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = i.inflate(R.layout.item_drawer, parent, false);
            ImageView icon = (ImageView) v.findViewById(R.id.drawerIcon);
            TextView title = (TextView) v.findViewById(R.id.drawerTitle);

            icon.setImageDrawable(getItem(position).icon);
            title.setText(getItem(position).text);
        } else {
            v = new ImageView(getContext());
            ((ImageView) v).setImageDrawable(getItem(position).icon);
        }

        return v;
    }
}
