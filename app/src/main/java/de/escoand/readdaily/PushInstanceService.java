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
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class PushInstanceService extends FirebaseInstanceIdService {
    private static final String REGISTRATION_DONE = "registration_done";

    public static boolean doRegistration(@NonNull final Context context, final boolean activate) {
        return doRegistration(context, activate, false);
    }

    public static boolean doRegistration(@NonNull final Context context, final boolean activate, final boolean force) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (!force && prefs.getBoolean(REGISTRATION_DONE, false))
            return true;

        long id = DownloadHandler.startInvisibleDownload(
                context,
                String.format(
                        context.getString(activate ? R.string.push_register_url : R.string.push_unregister_url),
                        Uri.encode(FirebaseInstanceId.getInstance().getToken())
                ),
                context.getString(R.string.message_push_register));

        prefs.edit().putBoolean(REGISTRATION_DONE, id > 0).apply();

        return id > 0;
    }

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();

        Log.w(getClass().getName(), "token refreshed " + token);

        doRegistration(this, PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notifications", true), false);
    }
}
