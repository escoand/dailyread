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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.DataAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;

import cz.msebera.android.httpclient.Header;

public class StoreListItem {
    private final String productId;
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
        new AsyncHttpClient().get(
                String.format(activity.getString(R.string.product_img_url), productId),
                new DataAsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        int len = 0;
                        for (Header header : headers) {
                            if (header.getName().equals("Content-Length"))
                                len = Integer.valueOf(header.getValue());
                        }
                        image.setImageBitmap(BitmapFactory.decodeByteArray(responseBody, 0, len));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(activity).removeData(productId);
                refreshButton();
            }
        });

        refreshButton();

        return this.parent;
    }

    private void refreshButton() {
        boolean isInstalled = new Database(activity).isInstalled(productId);

        // remove
        if (isInstalled) {
            buttonRemove.setVisibility(View.VISIBLE);
        } else
            buttonRemove.setVisibility(View.GONE);

        // up-to-date
        if (new Database(activity).isInstalled(productId)) {
            buttonAction.setVisibility(View.GONE);
        }

        // download
        else if (listing != null && transaction != null) {
            buttonAction.setText(activity.getString(R.string.button_download));
            buttonAction.setEnabled(true);
            buttonAction.setVisibility(View.VISIBLE);
            buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RequestParams params = new RequestParams();
                    AsyncHttpClient request = new AsyncHttpClient();

                    buttonAction.setEnabled(false);

                    params.add("data", transaction.purchaseInfo.responseData);
                    params.add("signature", transaction.purchaseInfo.signature);

                    request.setUserAgent(BuildConfig.APPLICATION_ID + " " + BuildConfig.VERSION_NAME);
                    request.post(activity.getString(R.string.product_data_url), params, new FileAsyncProgressHandler(activity, progress) {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, File file) {
                            try {
                                new Database(activity).loadDataXML(productId, 0, file);
                            } catch (Exception e) {
                                errorHandling(e);
                            }
                            refreshButton();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable e, File file) {
                            errorHandling(e);
                            refreshButton();
                        }
                    });
                }
            });
        }

        // purchase
        else if (listing != null) {
            buttonAction.setText(listing.priceText);
            buttonAction.setEnabled(true);
            buttonAction.setVisibility(View.VISIBLE);
            buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    billing.purchase(activity, productId);
                }
            });
        }
    }

    private void errorHandling(Throwable e) {
        Log.e("error", Log.getStackTraceString(e));
        Snackbar.make(parent, e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
    }
}
