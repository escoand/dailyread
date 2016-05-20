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

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.anjlab.android.iab.v3.BillingProcessor;

public class StoreArrayAdapter extends ArrayAdapter<StoreListItem> {
    private final Activity activity;
    private final BillingProcessor billing;

    public StoreArrayAdapter(Activity activity, BillingProcessor billing) {
        super(activity, R.layout.item_store);
        this.activity = activity;
        this.billing = billing;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getView(activity, parent, billing);
    }
}
