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
import android.os.AsyncTask;
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
        new SplashScreenTimeout().execute();

        // load initial data
        new InitialDataLoader().execute();

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

    private class SplashScreenTimeout extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (BuildConfig.DEBUG) {
                try {
                    Thread.sleep(SPLASH_MIN_TIMEOUT);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            latch.countDown();
            return null;
        }
    }

    private class InitialDataLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            final TextDatabase database = TextDatabase.getInstance(getApplicationContext());
            final int dataId = getResources().getIdentifier("data_initial", "raw", getPackageName());

            // load if database is empty
            if (database.getSubscriptionDao().getAll().isEmpty() && dataId != 0) {
                try {
                    LogHandler.i("try to load initial data as xml");
                    database.getImporter().importXML("default", getResources().openRawResource(dataId));
                } catch (Exception e1) {
                    LogHandler.w("failed to load initial data as xml");
                    LogHandler.log(e1);

                    try {
                        LogHandler.i("try to load initial data as csv");
                        database.getImporter().importCSV("default", getResources().openRawResource(dataId));
                    } catch (Exception e2) {
                        LogHandler.w("failed to load initial data as csv");
                        LogHandler.log(e2);
                    }
                }
            }

            latch.countDown();
            return null;
        }
    }
}
