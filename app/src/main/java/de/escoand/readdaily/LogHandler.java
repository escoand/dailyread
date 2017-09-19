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
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.acra.ACRA;

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
        ACRA.getErrorReporter().putCustomData(caller, message);
        ACRA.getErrorReporter().handleException(null);
    }

    public static void log(final Throwable error) {
        Log.e(getTag(), Log.getStackTraceString(error));
        ACRA.getErrorReporter().handleException(error);
    }

    public static void logAndShow(final Throwable error, final View view, @StringRes final int message,
                                  @StringRes final int button, final View.OnClickListener listener) {
        log(error);
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction(button, listener).show();
    }

    public static void logAndShow(final Throwable error, final Context context, @StringRes final int message) {
        log(error);
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
