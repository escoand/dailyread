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

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.util.Log;
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
    Thread refrehThread = null;
    float downloadProgress = -1;
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

    public StoreListItem(String productId) {
        this.productId = productId;
    }

    public View getView(final Activity activity, ViewGroup parent, final BillingProcessor billing) {
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
            title.setText(listing.title.replace(" (" + activity.getString(R.string.app_title) + ")", ""));
            description.setText(listing.description);
        } else {
            Log.e("product not found", productId);
            this.parent.setVisibility(View.GONE);
        }

        // image
        Request request = new Request.Builder()
                .url(String.format(activity.getString(R.string.product_img_url), productId))
                .build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final int len = Integer.valueOf(response.header("Content-Length"));
                    final byte[] data = response.body().bytes();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            image.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, len));
                        }
                    });
                } catch (Exception e) {
                    errorHandling(e);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                errorHandling(e);
            }
        });

        refreshUI();

        return this.parent;
    }

    private void refreshUI() {
        final Database db = Database.getInstance(activity);
        final boolean isInstalled = db.isInstalled(productId);
        downloadProgress = DownloadHandler.downloadProgress(activity, productId);

        // up-to-date
        if (isInstalled) {
            buttonRemove.setVisibility(View.VISIBLE);
            buttonRemove.setText(activity.getString(R.string.button_remove));
            buttonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.removeData(productId);
                    refreshUI();
                }
            });
            buttonAction.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
        }

        // downloading
        else if (listing != null && transaction != null && downloadProgress >= 0 && downloadProgress < 1) {
            buttonRemove.setVisibility(View.VISIBLE);
            buttonRemove.setText(activity.getString(R.string.button_cancel));
            buttonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadHandler.stopDownload(activity, productId);
                    refreshUI();
                }
            });
            buttonAction.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            progress.setMax(100);
            progress.setProgress((int) (100 * downloadProgress));
            if (refrehThread == null || !refrehThread.isAlive()) {
                refrehThread = new Thread(this);
                refrehThread.start();
            }
        }

        // download
        else if (listing != null && transaction != null) {
            buttonRemove.setVisibility(View.GONE);
            buttonAction.setVisibility(View.VISIBLE);
            buttonAction.setText(activity.getString(R.string.button_download));
            buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long id = DownloadHandler.startDownload(
                            activity,
                            transaction.purchaseInfo.signature,
                            transaction.purchaseInfo.responseData,
                            (String) title.getText()
                    );
                    if (id > 0)
                        buttonAction.setEnabled(false);
                    refreshUI();
                }
            });
            progress.setVisibility(View.GONE);
        }

        // purchase
        else if (listing != null) {
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
        while (downloadProgress >= 0 && downloadProgress < 1) {

            // refresh ui
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshUI();
                }
            });

            // sleep 5 second
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                return;
            }
        }
    }

    private void errorHandling(final Throwable e) {
        Log.e("StoreListItem", Log.getStackTraceString(e));
        // TODO non-technical message to user
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(parent, e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
