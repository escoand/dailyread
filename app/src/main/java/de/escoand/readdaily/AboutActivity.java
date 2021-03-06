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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("readability", false))
            setTheme(R.style.AppTheme_Readability);
        setContentView(R.layout.activity_nodrawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onBackPressed();
            }
        });

        ViewGroup v = (ViewGroup) findViewById(R.id.content);
        v.addView(getLayoutInflater().inflate(R.layout.fragment_about, v, false));

        ((TextView) v.findViewById(R.id.about_version)).setText(
                String.format(getString(R.string.about_version), BuildConfig.VERSION_NAME));
        v.findViewById(R.id.about_recommend).setOnClickListener(this);
        v.findViewById(R.id.about_tickets).setOnClickListener(this);

        // licenses
        ((TextView) v.findViewById(R.id.about_libraries)).setText(String.format("%s\n\n" +
                        "Android In-App Billing v3 Library %s\nApache License 2.0 by AnjLab\n\n" +
                        "Calligraphy %s\nApache License 2.0 by Christopher Jenkins\n\n" +
                        "CircleImageView %s\nApache License 2.0 by Henning Dodenhof\n\n" +
                        "Design Support Library %s\nApache License 2.0 by The Android Open Source Project\n\n" +
                        "Google Firebase Core %s\nApache License 2.0 by The Google\n\n" +
                        "Google Firebase Messaging %s\nApache License 2.0 by Google\n\n" +
                        "Material Calendar View %s\nby Prolific Interactive\n\n" +
                        "OkHttp %s\nApache License 2.0 by Square, Inc.\n\n" +
                        "v7 appcompat library %s\nApache License 2.0 by The Android Open Source Project\n\n" +
                        "v7 cardview library %s\nApache License 2.0 by The Android Open Source Project",
                getString(R.string.about_libraries),
                com.anjlab.android.iab.v3.BuildConfig.VERSION_NAME,
                uk.co.chrisjenx.calligraphy.BuildConfig.VERSION_NAME,
                de.hdodenhof.circleimageview.BuildConfig.VERSION_NAME,
                android.support.design.BuildConfig.VERSION_NAME,
                "",
                "",
                com.prolificinteractive.materialcalendarview.BuildConfig.VERSION_NAME,
                "",
                android.support.v7.appcompat.BuildConfig.VERSION_NAME,
                android.support.v7.cardview.BuildConfig.VERSION_NAME
        ));
    }

    @Override
    protected void attachBaseContext(final Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onClick(final View v) {
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
            default: // do nothing
                break;
        }
        if (i.getAction() != null)
            startActivity(i);
    }
}
