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

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StoreListItem implements Runnable {
    private final String productId;
    private final String mimeType;
    private Thread refrehThread = null;
    private float downloadProgress = -1;
    private SkuDetails listing;
    private TransactionDetails transaction;
    private Activity activity;
    private View parent;
    private ImageView image;
    private TextView title;
    private TextView description;
    private Button buttonRemove;
    private Button buttonAction;
    private ProgressBar progress;
    private BillingProcessor billing;

    public StoreListItem(final String productId, final String mimeType) {
        this.productId = productId;
        this.mimeType = mimeType;
    }

    public String getProductId() {
        return productId;
    }

    public View getView(final Activity activity, final ViewGroup parent, final BillingProcessor billing) {
        this.activity = activity;
        this.billing = billing;

        this.parent = activity.getLayoutInflater().inflate(R.layout.item_store, parent, false);
        image = (ImageView) this.parent.findViewById(R.id.product_image);
        title = (TextView) this.parent.findViewById(R.id.product_name);
        description = (TextView) this.parent.findViewById(R.id.product_description);
        buttonRemove = (Button) this.parent.findViewById(R.id.product_remove);
        buttonAction = (Button) this.parent.findViewById(R.id.procuct_action);
        progress = (ProgressBar) this.parent.findViewById(R.id.product_progress);

        // listing
        listing = billing.getPurchaseListingDetails(productId);
        transaction = billing.getPurchaseTransactionDetails(productId);
        if (listing != null) {
            title.setText(listing.title.replaceFirst(" \\([^()]+\\)$", ""));
            description.setText(listing.description);
            LogHandler.i("product: " + productId);
        } else {
            LogHandler.e("product not found: " + productId);
            this.parent.setVisibility(View.GONE);
        }

        // image
        new OkHttpClient().newCall(
                new Request.Builder()
                        .url(String.format(activity.getString(R.string.product_img_url), productId))
                        .build()
        ).enqueue(new RequestCallback());

        refreshUI();

        return this.parent;
    }

    public void refreshUI() {
        final SubscriptionDao dao = TextDatabase.getInstance(activity).getSubscriptionDao();
	final Subscription subscription = dao.findByName(productId);
        downloadProgress = DownloadHandler.downloadProgress(activity, productId);

        // failed
        if (downloadProgress == DownloadHandler.DOWNLOAD_FAILED) {
            LogHandler.i("failed");
            DownloadHandler.stopDownload(activity, productId);
            dao.delete(subscription);
            refreshUI();
        }

        // up-to-date
        else if (subscription != null) {
            LogHandler.d("up-to-date");
            buttonRemove.setVisibility(View.VISIBLE);
            buttonRemove.setText(activity.getString(R.string.button_remove));
            buttonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    dao.delete(subscription);
                    refreshUI();
                }
            });
            buttonAction.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            if (refrehThread == null || !refrehThread.isAlive()) {
                refrehThread = new Thread(this);
                refrehThread.start();
            }
        }

        // importing
        else if (listing != null && transaction != null && downloadProgress == 1) {
            LogHandler.d("importing");
            buttonAction.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);
            if (refrehThread == null || !refrehThread.isAlive()) {
                refrehThread = new Thread(this);
                refrehThread.start();
            }
        }

        // downloading
        else if (listing != null && transaction != null && downloadProgress >= 0 && downloadProgress < 1) {
            LogHandler.d("downloading");
            buttonRemove.setVisibility(View.VISIBLE);
            buttonRemove.setText(activity.getString(R.string.button_cancel));
            buttonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    DownloadHandler.stopDownload(activity, productId);
                    refreshUI();
                }
            });
            buttonAction.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(false);
            progress.setMax(100);
            progress.setProgress((int) (100 * downloadProgress));
            if (refrehThread == null || !refrehThread.isAlive()) {
                refrehThread = new Thread(this);
                refrehThread.start();
            }
        }

        // pending or paused
        else if (listing != null && transaction != null && (downloadProgress == DownloadHandler.DOWNLOAD_PENDING || downloadProgress == DownloadHandler.DOWNLOAD_PAUSED)) {
            LogHandler.d("pending or paused " + downloadProgress);
            buttonRemove.setVisibility(View.VISIBLE);
            buttonRemove.setText(activity.getString(R.string.button_cancel));
            buttonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    DownloadHandler.stopDownload(activity, productId);
                    refreshUI();
                }
            });
            buttonAction.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            progress.setIndeterminate(true);
            if (refrehThread == null || !refrehThread.isAlive()) {
                refrehThread = new Thread(this);
                refrehThread.start();
            }
        }

        // download
        else if (listing != null && transaction != null) {
            LogHandler.i("download");
            buttonRemove.setVisibility(View.GONE);
            buttonAction.setVisibility(View.VISIBLE);
            buttonAction.setText(activity.getString(R.string.button_download));
            buttonAction.setOnClickListener(new OnDownloadClickListener());
            progress.setVisibility(View.GONE);
        }

        // purchase
        else if (listing != null) {
            LogHandler.i("purchase");
            buttonRemove.setVisibility(View.GONE);
            buttonAction.setVisibility(View.VISIBLE);
            buttonAction.setText(listing.priceText);
            buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    billing.purchase(activity, productId);
                    listing = billing.getPurchaseListingDetails(productId);
                    transaction = billing.getSubscriptionTransactionDetails(productId);
                    refreshUI();
                }
            });
            progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void run() {
        while (downloadProgress >= 0 && downloadProgress <= 1) {

            // refresh ui
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshUI();
                }
            });

            // sleep 5 second
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                return;
            }
        }
    }

    public void download() {
        new OnDownloadClickListener().onClick(null);
    }

    private class OnDownloadClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            DownloadHandler.startDownload(
                    activity,
                    transaction.purchaseInfo.signature,
                    transaction.purchaseInfo.responseData,
                    (String) title.getText(),
                    mimeType
            );
            refreshUI();
        }
    }

    private class RequestCallback implements Callback {
        @Override
        public void onResponse(final Call call, final Response response) {
            try {
                final int len = Integer.valueOf(response.header("Content-Length"));
                final byte[] data = response.body().bytes();

                LogHandler.i("image size: " + len);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (len < 1024 * 1024)
                            image.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, len));
                        else
                            image.setImageResource(R.drawable.icon_close);
                    }
                });
            }

            // unexpected
            catch (final Exception e) {
                onFailue(e);
            }
        }

        @Override
        public void onFailure(final Call call, final IOException e) {
            onFailue(e);
        }

        private void onFailue(final Throwable e) {
            LogHandler.log(e);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    image.setImageResource(R.drawable.icon_close);
                }
            });
        }
    }
}
