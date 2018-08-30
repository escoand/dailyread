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

package de.escoand.readdaily;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.concurrent.CountDownLatch;

import de.escoand.readdaily.database.TextDatabase;

public class SplashActivity extends Activity {
    final long SPLASH_MIN_TIMEOUT = 3000;
    final CountDownLatch latch = new CountDownLatch(2);

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // show splash at least x seconds
        if (BuildConfig.DEBUG)
            latch.countDown();
        else
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(SPLASH_MIN_TIMEOUT);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    latch.countDown();
                }
            }).run();

        // load initial data
        new Thread(new Runnable() {
            @Override
            public void run() {
                final TextDatabase database = TextDatabase.getInstance(getApplicationContext());
                final int dataId = getResources().getIdentifier("data_initial", "raw", getPackageName());

                // load if database is empty
                if (database.getDownloadDao().getAll().isEmpty() && dataId != 0) {
                    try {
                        LogHandler.i("try to load initial data as xml");
                        database.getImporter().importXML("default", getResources().openRawResource(dataId));
                    } catch (Exception e) {
                        LogHandler.w("failed to load initial data as xml");

                        //try {
                        //    LogHandler.i("try to load initial data as csv");
                        //    database.getImporter().importCSV("default", context.getResources().openRawResource(dataId));
                        //} catch (Exception e) {
                        //    LogHandler.w("failed to load initial data as csv");
                        //}
                    }
                }

                latch.countDown();
            }
        }).run();

        // wait for the workers
        try {
            latch.await();
        } catch (InterruptedException e) {
            // ignore
        }

        // start main activity
        startActivity(new Intent(getApplication(), MainActivity.class));
        finish();
    }
}
