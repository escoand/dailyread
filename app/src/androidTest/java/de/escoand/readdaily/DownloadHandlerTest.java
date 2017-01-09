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
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class DownloadHandlerTest {
    private final String INVISIBLE_URL = "http://google.com";
    private final String INVISIBLE_ID = "google.com";

    private final String DOWNLOAD_ID = "android.test.purchased";
    private final String DOWNLOAD_JSON = "{" +
            "\"packageName\":\"" + BuildConfig.APPLICATION_ID + "\"," +
            "\"orderId\":\"transactionId." + DOWNLOAD_ID + "\"," +
            "\"productId\":\"" + DOWNLOAD_ID + "\"," +
            "\"developerPayload\":\"inapp:" + DOWNLOAD_ID + ":2d7328a9-f376-4556-ad2f-dc8c1feb0271\"," +
            "\"purchaseTime\":0," +
            "\"purchaseState\":0," +
            "\"purchaseToken\":\"inapp:" + BuildConfig.APPLICATION_ID + ":" + DOWNLOAD_ID + "\"" +
            "}";

    private final Context context = InstrumentationRegistry.getTargetContext();

    @Test
    public void test_01_InvisibleDownload() {
        long id = DownloadHandler.startInvisibleDownload(context, INVISIBLE_URL, INVISIBLE_ID);
        Assert.assertTrue(id + " > 0", id > 0);
    }

    @Test
    public void test_11_Start() {
        long id = DownloadHandler.startDownload(context, "", DOWNLOAD_JSON, DOWNLOAD_ID);
        Assert.assertTrue(id + " > 0", id > 0);
    }

    @Test
    public void test_12_Progress() {
        float prog = DownloadHandler.downloadProgress(context, DOWNLOAD_ID);
        Assert.assertTrue(prog + " >= 0", prog >= 0);
    }

    @Test
    public void test_13_Stop() {
        DownloadHandler.stopDownload(context, DOWNLOAD_ID);
        float end = DownloadHandler.downloadProgress(context, DOWNLOAD_ID);
        Assert.assertEquals(end, (float) DownloadHandler.NO_SUBSCRIPTION_DOWNLOAD);
    }

    @Test
    public void test_21_Start() {
        long id = DownloadHandler.startDownload(context, "", DOWNLOAD_JSON, DOWNLOAD_ID);
        Assert.assertTrue(id + " > 0", id > 0);
    }

    @Test
    public void test_22_Progress() throws InterruptedException {
        while (true) {
            try {
                float prog = DownloadHandler.downloadProgress(context, DOWNLOAD_ID);

                Assert.assertTrue("0 <= " + prog + " <= 1", prog >= 0 && prog <= 1);

                if (prog >= 1) break;
            } catch (IllegalStateException e) {
                // ignore
            }

            Thread.sleep(1000);
        }
    }

    @Test
    public void test_23_Receive() throws Exception {
        new DownloadHandler().onReceive(context, null);
    }
}
