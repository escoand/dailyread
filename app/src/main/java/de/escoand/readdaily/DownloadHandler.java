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
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EmptyStackException;
import java.util.Random;

public class DownloadHandler extends BroadcastReceiver {
    public final static long SUBSCRIPTION_DOWNLOAD_UNKNOWN = -2;
    public final static long DOWNLOAD_ID_UNKNOWN = -1;

    public static long startInvisibleDownload(Context context, String url, String title) {
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        String name = String.valueOf(new Random().nextInt());

        LogHandler.log(Log.WARN, "load invisible " + url);

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
            LogHandler.log(e);
            return -1;
        }

        LogHandler.log(Log.WARN, "load " + name);

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
            return SUBSCRIPTION_DOWNLOAD_UNKNOWN;

        // get download
        cursor = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE))
                .query(new DownloadManager.Query().setFilterById(id));
        if (!cursor.moveToFirst()) {
            Database.getInstance(context).removeDownload(id);
            return DOWNLOAD_ID_UNKNOWN;
        }

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
        final Cursor downloads = db.getDownloads();

        LogHandler.log(Log.WARN, "receive starting");

        while (downloads.moveToNext()) {
            final String name = downloads.getString(downloads.getColumnIndex(Database.COLUMN_SUBSCRIPTION));
            final long id = downloads.getLong(downloads.getColumnIndex(Database.COLUMN_ID));
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
                        LogHandler.log(Log.WARN, "import starting of " + name);

                        final FileInputStream stream = new ParcelFileDescriptor.AutoCloseInputStream(manager.openDownloadedFile(id));
                        final String mimeGet = manager.getMimeTypeForDownloadedFile(id);
                        final String mimeCur = download.getString(download.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));

                        LogHandler.log(Log.INFO, "id: " + String.valueOf(id));
                        LogHandler.log(Log.INFO, "manager: " + manager.toString());
                        LogHandler.log(Log.INFO, "stream: " + stream.toString());
                        LogHandler.log(Log.INFO, "mime-getter: " + mimeGet);
                        LogHandler.log(Log.INFO, "mime-cursor: " + mimeCur);

                        switch (mimeGet != null ? mimeGet : (mimeCur != null ? mimeCur : "")) {

                            // register feedback
                            case "application/json":
                                final byte[] buf = new byte[256];
                                final int len = stream.read(buf);
                                LogHandler.log(Log.WARN,
                                        "register feedback: " + new String(buf, 0, len));
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

                            // do nothing
                            default:
                                LogHandler.log(new IntentFilter.MalformedMimeTypeException());
                                break;
                        }

                        stream.close();
                        LogHandler.log(Log.WARN, "import finished (" + name + ")");
                    }


                    // file error
                    catch (FileNotFoundException e) {
                        LogHandler.log(e);
                        Toast.makeText(context, R.string.message_download_open, Toast.LENGTH_LONG).show();
                    }

                    // stream error
                    catch (IOException e) {
                        LogHandler.log(e);
                        Toast.makeText(context, R.string.message_download_read, Toast.LENGTH_LONG).show();
                    }

                    // xml error
                    catch (XmlPullParserException e) {
                        LogHandler.log(e);
                        Toast.makeText(context, R.string.message_download_xml, Toast.LENGTH_LONG).show();
                    }

                    // clean
                    finally {
                        manager.remove(id);
                        db.removeDownload(id);
                        LogHandler.log(Log.WARN, "clean finished");
                    }
                }
            }).start();
        }

        downloads.close();
        LogHandler.log(Log.WARN, "receiving done");
    }
}
