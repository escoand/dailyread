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
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

public class DownloadHandler extends BroadcastReceiver {
    public final static long DOWNLOAD_FAILED = -5;
    public final static long DOWNLOAD_PAUSED = -4;
    public final static long DOWNLOAD_PENDING = -3;
    public final static long SUBSCRIPTION_DOWNLOAD_UNKNOWN = -2;
    public final static long DOWNLOAD_ID_UNKNOWN = -1;

    public static long startInvisibleDownload(final Context context, final String url, final String title) {
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        String name = String.valueOf(new Random().nextInt());

        LogHandler.w("load invisible " + url);

        long id = manager.enqueue(new DownloadManager.Request(Uri.parse(url))
                .setTitle(title));
        Database.getInstance(context).addDownload(name, id, null);

        return id;
    }

    public static long startDownload(@NonNull final Context context, @NonNull final String signature,
                                     @NonNull final String responseData, @NonNull final String title,
                                     @Nullable final String mimeType) {
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        String name;

        try {
            name = new JSONObject(responseData).getString("productId");
        } catch (JSONException e) {
            LogHandler.log(e);
            return -1;
        }

        LogHandler.w("load " + name);

        long id = manager.enqueue(new DownloadManager.Request(Uri.parse(context.getString(R.string.product_data_url)))
                .addRequestHeader("App-Signature", signature)
                .addRequestHeader("App-ResponseData", responseData)
                .setTitle(title)
                .setDescription(context.getString(R.string.app_title)));
        Database.getInstance(context).addDownload(name, id, mimeType);

        return id;
    }

    public static float downloadProgress(final Context context, final String name) {
        Cursor cursor = null;
        long id = 0;

        // get download id
        cursor = Database.getInstance(context).getDownloads();
        while (cursor.moveToNext())
            if (cursor.getString(cursor.getColumnIndex(Database.COLUMN_SUBSCRIPTION)).equals(name)) {
                id = cursor.getLong(cursor.getColumnIndex(Database.COLUMN_ID));
                break;
            }
        cursor.close();
        if (id <= 0)
            return SUBSCRIPTION_DOWNLOAD_UNKNOWN;

        // get download
        cursor = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE))
                .query(new DownloadManager.Query().setFilterById(id));
        if (!cursor.moveToFirst()) {
            Database.getInstance(context).removeDownload(id);
            cursor.close();
            return DOWNLOAD_ID_UNKNOWN;
        }

        // get status
        final int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        final int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
        float progress = cursor.getFloat(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)) /
                cursor.getFloat(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

        if (status == DownloadManager.STATUS_FAILED)
            progress = DOWNLOAD_FAILED;
        else if (status == DownloadManager.STATUS_PAUSED)
            progress = DOWNLOAD_PAUSED;
        //else if (status == DownloadManager.STATUS_PENDING)
        //    progress = DOWNLOAD_PENDING;

        // get progress

        cursor.close();
        return progress;
    }

    public static void stopDownload(final Context context, final String name) {
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
    public void onReceive(final Context context, final Intent intent) {
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final Database db = Database.getInstance(context);
        final Cursor downloads = db.getDownloads();

        LogHandler.w("receive starting");

        while (downloads.moveToNext()) {
            final long id = downloads.getLong(downloads.getColumnIndex(Database.COLUMN_ID));
            final String name = downloads.getString(downloads.getColumnIndex(Database.COLUMN_SUBSCRIPTION));
            final String mime = downloads.getString(downloads.getColumnIndex(Database.COLUMN_TYPE));
            final Cursor download = manager.query(new DownloadManager.Query().setFilterById(id));

            // download exists
            if (!download.moveToFirst())
                continue;

            // download finished
            if (download.getInt(download.getColumnIndex(DownloadManager.COLUMN_STATUS)) != DownloadManager.STATUS_SUCCESSFUL)
                continue;

            // import file in background
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogHandler.w("import starting of " + name);

                        final FileInputStream stream = new ParcelFileDescriptor.AutoCloseInputStream(manager.openDownloadedFile(id));
                        final String mimeServer = manager.getMimeTypeForDownloadedFile(id);

                        LogHandler.i("id: " + String.valueOf(id));
                        LogHandler.i("manager: " + manager.toString());
                        LogHandler.i("stream: " + stream.toString());
                        LogHandler.i("mime: " + mime);
                        LogHandler.i("mimeServer: " + mimeServer);

                        switch (mime != null ? mime : (mimeServer != null ? mimeServer : "")) {

                            // register feedback
                            case "application/json":
                                final byte[] buf = new byte[256];
                                final int len = stream.read(buf);
                                LogHandler.w("register feedback: " + new String(buf, 0, len));
                                break;

                            // csv data
                            case "text/plain":
                                db.importCSV(name, stream);
                                break;

                            // xml data
                            case "application/xml":
                            case "text/xml":
                                db.importXML(name, stream);
                                break;

                            // zipped data
                            case "application/zip":
                                db.importZIP(name, stream);
                                break;

                            // do nothing
                            default:
                                LogHandler.log(new IntentFilter.MalformedMimeTypeException());
                                break;
                        }

                        stream.close();
                        LogHandler.w("import finished (" + name + ")");
                    }


                    // file error
                    catch (FileNotFoundException e) {
                        LogHandler.logAndShow(e, context, R.string.message_download_open);
                    }

                    // stream error
                    catch (IOException e) {
                        LogHandler.logAndShow(e, context, R.string.message_download_read);
                    }

                    // xml error
                    catch (XmlPullParserException e) {
                        LogHandler.logAndShow(e, context, R.string.message_download_xml);
                    }

                    // clean
                    finally {
                        manager.remove(id);
                        db.removeDownload(id);
                        LogHandler.w("clean finished");
                    }
                }
            }).start();
        }

        downloads.close();
        LogHandler.w("receiving done");
    }
}
