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

package com.escoand.android.readdaily;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nodrawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ViewGroup v = (ViewGroup) findViewById(R.id.content);
        v.addView(getLayoutInflater().inflate(R.layout.fragment_about, null));

        ((TextView) v.findViewById(R.id.about_version)).setText(getString(R.string.about_version) + " " + BuildConfig.VERSION_NAME);
        v.findViewById(R.id.about_recommend).setOnClickListener(this);
        v.findViewById(R.id.about_tickets).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent();
        switch (v.getId()) {
            case R.id.about_recommend:
                i.setAction(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.message_recommend_subject));
                i.putExtra(Intent.EXTRA_TEXT, getString(R.string.message_recommend) + BuildConfig.APPLICATION_ID);
                i.setType("text/plain");
                break;
            case R.id.about_tickets:
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getString(R.string.url_tickets)));
                break;
        }
        if (i.getAction() != null)
            startActivity(i);
    }
}