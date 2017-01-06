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

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.Random;

public class DownloadHandler extends BroadcastReceiver {
    public final static int REQUEST_PERMISSIONS = 9;
    public final static long MISSING_PERMISSION = -3;
    public final static long NO_SUBSCRIPTION_DOWNLOAD = -2;
    public final static long DOWNLOAD_UNKNOWN = -1;

    public static long startInvisibleDownload(Context context, String url, String title) {
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        String name = String.valueOf(new Random().nextInt());

        Log.w("DownloadHandler", "load invisible " + url);

        long id = manager.enqueue(new DownloadManager.Request(Uri.parse(url))
                .setTitle(title));
        Database.getInstance(context).addDownload(name, id);

        return id;
    }

    public static long startDownload(Context context, String signature, String responseData, String title) {
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        String name;

        try {
            name = new JSONObject(responseData).getString("productId");
        } catch (JSONException e) {
            if (!BuildConfig.DEBUG)
                FirebaseCrash.report(e);
            return -1;
        }

        Log.w("DownloadHandler", "load " + name);

        long id = manager.enqueue(new DownloadManager.Request(Uri.parse(context.getString(R.string.product_data_url)))
                .addRequestHeader("App-Signature", signature)
                .addRequestHeader("App-ResponseData", responseData)
                .setTitle(title)
                .setDescription(context.getString(R.string.app_title)));
        Database.getInstance(context).addDownload(name, id);

        return id;
    }

    public static float downloadProgress(Context context, String name) {
        Cursor cursor = Database.getInstance(context).getDownloads();
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
            return NO_SUBSCRIPTION_DOWNLOAD;

        // get download
        cursor = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE))
                .query(new DownloadManager.Query().setFilterById(id));
        if (!cursor.moveToFirst())
            return DOWNLOAD_UNKNOWN;

        // get progress
        progress = cursor.getFloat(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) /
                cursor.getFloat(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

        cursor.close();
        return progress;
    }

    public static void stopDownload(Context context, String name) {
        Database db = Database.getInstance(context);
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
        final Database db = Database.getInstance(context);
        Cursor downloads = db.getDownloads();

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
                @SuppressWarnings("ResultOfMethodCallIgnored")
                @Override
                public void run() {
                    Log.w("DownloadHandler", "import " + name);

                    try {
                        FileInputStream stream = new ParcelFileDescriptor.AutoCloseInputStream(manager.openDownloadedFile(id));

                        switch (manager.getMimeTypeForDownloadedFile(id)) {

                            // register feedback
                            case "application/json":
                                byte[] buf = new byte[256];
                                stream.read(buf);
                                Log.w("DownloadHandler", "register feedback " + new String(buf));
                                break;

                            // csv data
                            case "text/plain":
                                db.importCSV(name, stream);
                                break;

                            // xml data
                            case "application/xml":
                                db.importXML(name, stream);
                                break;

                            // zipped data
                            case "application/zip":
                                db.importZIP(name, stream);
                                break;
                        }

                        // clean
                        stream.close();
                        download.close();
                        manager.remove(id);
                        db.removeDownload(id);

                    } catch (Exception e) {
                        Log.e("DownloadHandler", Log.getStackTraceString(e));
                        if (!BuildConfig.DEBUG)
                            FirebaseCrash.report(e);
                    }

                    Log.w("DownloadHandler", "finished " + name);
                }
            }).start();

        }
        downloads.close();

        Log.w("DownloadHandler", "receiving done");
    }
}
