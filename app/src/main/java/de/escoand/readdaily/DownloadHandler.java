/*
 * Copyright (c) 2018 escoand.
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

import de.escoand.readdaily.database.TextDatabase;
import de.escoand.readdaily.database.dao.DownloadDao;
import de.escoand.readdaily.database.entity.Download;
import de.escoand.readdaily.database.util.Importer;

public class DownloadHandler extends BroadcastReceiver {
    public final static long DOWNLOAD_FAILED = -5;
    public final static long DOWNLOAD_PAUSED = -4;
    public final static long DOWNLOAD_PENDING = -3;
    public final static long SUBSCRIPTION_DOWNLOAD_UNKNOWN = -2;
    public final static long DOWNLOAD_ID_UNKNOWN = -1;

    public static long startInvisibleDownload(final Context context, final String url, final String title) {
        final DownloadDao dao = TextDatabase.getInstance(context).getDownloadDao();
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        String name = String.valueOf(new Random().nextInt());

        LogHandler.w("load invisible " + url);

        long id = manager.enqueue(new DownloadManager.Request(Uri.parse(url))
                .setTitle(title));
        dao.insert(new Download(name, id, null));

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
        final DownloadDao dao = TextDatabase.getInstance(context).getDownloadDao();
        final Download download = dao.findBySubscription(name);

        // check if exists
        if (download == null)
            return SUBSCRIPTION_DOWNLOAD_UNKNOWN;

        // get download
        final Cursor cursor = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE))
                .query(new DownloadManager.Query().setFilterById(download.getDownloadId()));
        if (!cursor.moveToFirst()) {
            dao.delete(download);
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
        final DownloadDao dao = TextDatabase.getInstance(context).getDownloadDao();
        final Download download = dao.findBySubscription(name);

        // check if exists
        if (download == null)
            return;

        // stop download
        ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).remove(download.getDownloadId());
        dao.delete(download);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final DownloadDao dao = TextDatabase.getInstance(context).getDownloadDao();
        final Importer importer = TextDatabase.getInstance(context).getImporter();
        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        LogHandler.w("receive starting");

        for (final Download download : dao.getAll()) {
            final Cursor managerData = manager.query(new DownloadManager.Query().setFilterById(download.getDownloadId()));

            // download exists
            if (!managerData.moveToFirst())
                continue;

            // download finished
            if (managerData.getInt(managerData.getColumnIndex(DownloadManager.COLUMN_STATUS)) != DownloadManager.STATUS_SUCCESSFUL)
                continue;

            // import file in background
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogHandler.w("import starting of " + download.getSubscription());

                        final FileInputStream stream = new ParcelFileDescriptor.AutoCloseInputStream(manager.openDownloadedFile(download.getDownloadId()));
                        final String mimeServer = manager.getMimeTypeForDownloadedFile(download.getDownloadId());

                        LogHandler.i("id: " + String.valueOf(download.getDownloadId()));
                        LogHandler.i("manager: " + manager.toString());
                        LogHandler.i("stream: " + stream.toString());
                        LogHandler.i("mime: " + download.getMimeType());
                        LogHandler.i("mimeServer: " + mimeServer);

                        switch (download.getMimeType() != null ? download.getMimeType() : (mimeServer != null ? mimeServer : "")) {

                            // register feedback
                            case "application/json":
                                final byte[] buf = new byte[256];
                                final int len = stream.read(buf);
                                LogHandler.w("register feedback: " + new String(buf, 0, len));
                                break;

                            // xml data
                            case "application/xml":
                            case "text/xml":
                                importer.importXML(download.getSubscription(), stream);
                                break;

                            // zipped data
                            case "application/zip":
                                importer.importZIP(download.getSubscription(), stream, context.getFilesDir());
                                break;

                            // do nothing
                            default:
                                LogHandler.log(new IntentFilter.MalformedMimeTypeException());
                                break;
                        }

                        stream.close();
                        LogHandler.w("import finished (" + download.getSubscription() + ")");
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
                        manager.remove(download.getDownloadId());
                        dao.delete(download);
                        LogHandler.w("clean finished");
                    }
                }
            }).start();
        }

        LogHandler.w("receiving done");
    }
}
