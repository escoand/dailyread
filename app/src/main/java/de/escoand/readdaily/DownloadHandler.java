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

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class DownloadHandler extends BroadcastReceiver {

    public static long startDownload(Context context, String signature, String responseData, String title, int revision) {
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        String name;

        try {
            name = new JSONObject(responseData).getString("productId");
        } catch (JSONException e) {
            return -1;
        }

        Log.w("DownloadHandler", "load " + name);

        long id = manager.enqueue(new DownloadManager.Request(Uri.parse(context.getString(R.string.product_data_url)))
                .addRequestHeader("App-Signature", signature)
                .addRequestHeader("App-ResponseData", responseData)
                .setDestinationInExternalFilesDir(context, null, name)
                .setTitle(context.getString(R.string.app_title))
                .setDescription(title));
        new Database(context).addDownload(name, revision, id);

        return id;
    }

    public static float downloadProgress(Context context, String name) {
        Cursor c = new Database(context).getDownloads();
        long id = 0;

        // get download id
        while (c.moveToNext())
            if (c.getString(c.getColumnIndex(Database.COLUMN_SUBSCRIPTION)).equals(name)) {
                id = c.getLong(c.getColumnIndex(Database.COLUMN_ID));
                break;
            }
        if (id <= 0)
            return -2;

        // get download
        c = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE))
                .query(new DownloadManager.Query().setFilterById(id));
        if (!c.moveToFirst())
            return -1;

        return c.getFloat(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) /
                c.getFloat(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Database database = new Database(context);
        Cursor downloads = database.getDownloads();

        Log.w("DownloadHandler", "received");

        downloads.moveToPosition(-1);
        while (downloads.moveToNext()) {
            String name = downloads.getString(downloads.getColumnIndex(Database.COLUMN_SUBSCRIPTION));
            int revision = downloads.getInt(downloads.getColumnIndex(Database.COLUMN_REVISION));
            long id = downloads.getLong(downloads.getColumnIndex(Database.COLUMN_ID));

            Cursor download = manager.query(new DownloadManager.Query().setFilterById(id));

            // download exists
            download.moveToPosition(-1);
            if (!download.moveToFirst())
                continue;

            // download finished
            if (download.getInt(download.getColumnIndex(DownloadManager.COLUMN_STATUS)) != DownloadManager.STATUS_SUCCESSFUL)
                continue;

            Log.w("DownloadHandler", "import " + name);

            // import file
            try {
                File file = new File(download.getString(download.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).replace("file:/", "/"));
                InputStream stream = new FileInputStream(file);

                switch (manager.getMimeTypeForDownloadedFile(id)) {

                    // xml data
                    case "application/xml":
                        new Database(context).loadDataXML(name, revision, stream);
                        break;

                    // zipped data
                    case "application/zip":
                        new Database(context).loadDataZIP(name, revision, stream, context);
                        break;
                }

                // clean
                stream.close();
                file.delete();
                manager.remove(id);
                database.removeDownload(id);

            } catch (Exception e) {
                Log.e("DownloadHandler", Log.getStackTraceString(e));
            }

            Log.w("DownloadHandler", "finished " + name);
        }

        Log.w("DownloadHandler", "receive done");
    }
}
