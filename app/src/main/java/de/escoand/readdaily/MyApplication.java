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

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.config.ConfigurationBuilder;
import org.acra.sender.HttpSender;

import java.util.HashMap;

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        try {
            ACRA.init(this, new ConfigurationBuilder(this)
                    .setBuildConfigClass(BuildConfig.class)
                    .setHttpMethod(HttpSender.Method.PUT)
                    .setReportType(HttpSender.Type.JSON)
                    .setFormUri(getString(R.string.crash_reporting_url))
                    .setFormUriBasicAuthLogin(getString(R.string.crash_reporting_login))
                    .setFormUriBasicAuthPassword(getString(R.string.crash_reporting_password))
                    .setHttpHeaders(new HashMap<String, String>(1) {
                        {
                            put("X-Requested-With", "XMLHttpRequest");
                        }
                    })
                    .build());
        } catch (Exception e) {
            LogHandler.log(e);
        }
    }
}
