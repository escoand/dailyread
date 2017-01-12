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

import android.util.Log;

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

    public static void log(final Throwable exception) {
        Log.e(getTag(), Log.getStackTraceString(exception));
        FirebaseCrash.report(exception);
    }
}
