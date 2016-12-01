/*
 * Copyright (c) 2016 escoand.
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
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class PushInstanceService extends FirebaseInstanceIdService {

    public static boolean setRegistration(Context context, boolean activate) {
        long id = DownloadHandler.startInvisibleDownload(
                context,
                String.format(
                        context.getString(activate ? R.string.push_register_url : R.string.push_unregister_url),
                        Uri.encode(FirebaseInstanceId.getInstance().getToken())),
                context.getString(R.string.message_push_register));
        return id > 0;
    }

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();

        Log.w("PushInstanceService", "token refreshed " + token);

        setRegistration(this, PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notifications", true));
    }
}
