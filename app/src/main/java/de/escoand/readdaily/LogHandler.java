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

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

public class LogHandler {
    private static String getTag() {
        final Thread thread = Thread.currentThread();
        final StackTraceElement caller = thread.getStackTrace()[4];
        return caller.getClassName() + "." + caller.getMethodName() +
                "(" + caller.getFileName() + ":" + caller.getLineNumber() + ")" +
                "[" + thread.getId() + "]";
    }

    public static void log(final int priority, final String message) {
        log(getTag(), priority, message);
    }

    public static void log(final String caller, final int priority, final String message) {
        Log.println(priority, caller, message);
        FirebaseCrash.log(caller + ": " + message);
    }

    public static void log(final Throwable error) {
        Log.e(getTag(), Log.getStackTraceString(error));
        FirebaseCrash.report(error);
    }

    public static void logAndShow(final Throwable error, final View view, final String message,
                                  final String button, final View.OnClickListener listener) {
        log(error);
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction(button, listener).show();
    }

    public static void logAndShow(final Throwable error, final Activity activity, final String message) {
        log(error);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
