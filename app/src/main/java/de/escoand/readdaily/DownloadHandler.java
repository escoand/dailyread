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

public class DownloadHandler extends BroadcastReceiver {

    public static long startDownload(Context context, String signature, String responseData, String title) {
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
                .setTitle(title)
                .setDescription(context.getString(R.string.app_title)));
        new Database(context).addDownload(name, id);

        return id;
    }

    public static float downloadProgress(Context context, String name) {
        Cursor cursor = new Database(context).getDownloads();
        long id = 0;
        float progress;

        // get download id
        while (cursor.moveToNext())
            if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_SUBSCRIPTION)).equals(name)) {
                id = cursor.getLong(cursor.getColumnIndex(Database.COLUMN_ID));
                break;
            }
        cursor.close();
        if (id <= 0)
            return -2;

        // get download
        cursor = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE))
                .query(new DownloadManager.Query().setFilterById(id));
        if (!cursor.moveToFirst())
            return -1;

        // get progress
        progress = cursor.getFloat(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) /
                cursor.getFloat(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

        cursor.close();
        return progress;
    }

    public static void stopDownload(Context context, String name) {
        Database db = new Database(context);
        Cursor c = db.getDownloads();
        long id = 0;

        // get download id
        while (c.moveToNext())
            if (c.getString(c.getColumnIndex(Database.COLUMN_SUBSCRIPTION)).equals(name)) {
                id = c.getLong(c.getColumnIndex(Database.COLUMN_ID));
                break;
            }
        c.close();
        if (id <= 0)
            return;

        // stop download
        ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).remove(id);
        db.removeDownload(id);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final Database database = new Database(context);
        Cursor downloads = database.getDownloads();

        Log.w("DownloadHandler", "receiving");

        downloads.moveToPosition(-1);
        while (downloads.moveToNext()) {
            final String name = downloads.getString(downloads.getColumnIndex(Database.COLUMN_SUBSCRIPTION));
            final long id = downloads.getLong(downloads.getColumnIndex(Database.COLUMN_ID));
            final Cursor download = manager.query(new DownloadManager.Query().setFilterById(id));

            // download exists
            download.moveToPosition(-1);
            if (!download.moveToFirst())
                continue;

            // download finished
            if (download.getInt(download.getColumnIndex(DownloadManager.COLUMN_STATUS)) != DownloadManager.STATUS_SUCCESSFUL)
                continue;

            // import file in background
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.w("DownloadHandler", "import " + name);

                    try {
                        File file = new File(download.getString(download.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).replace("file:/", "/"));
                        FileInputStream stream = new FileInputStream(file);

                        switch (manager.getMimeTypeForDownloadedFile(id)) {

                            // csv data
                            case "text/plain":
                                new Database(context).importCSV(name, stream);
                                break;

                            // xml data
                            case "application/xml":
                                new Database(context).importXML(name, stream);
                                break;

                            // zipped data
                            case "application/zip":
                                new Database(context).importZIP(name, stream);
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
            }).start();

        }
        downloads.close();

        Log.w("DownloadHandler", "receiving done");
    }
}
