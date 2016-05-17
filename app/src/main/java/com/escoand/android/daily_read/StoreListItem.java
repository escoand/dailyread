/*
 * Copyright (C) 2016  escoand
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

package com.escoand.android.daily_read;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.loopj.android.http.AsyncHttpClient;
import com.rey.material.widget.Button;
import com.rey.material.widget.ProgressView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;

public class StoreListItem {
    private final String productId;
    private final String imageURL;
    private final String dataURL;
    private final String type;
    private SkuDetails details;
    private boolean isInstalled;
    private boolean isPurchased;
    private boolean isError = false;

    private Activity activity;
    private View container;
    //private TextView title;
    //private TextView desc;
    private ImageView image;
    private Button buttonRemove;
    private Button buttonAction;
    private ProgressView progressImage;
    private ProgressView progressData;

    private BillingProcessor billing;

    protected StoreListItem(JSONObject json) throws JSONException {
        productId = json.getString("name");
        imageURL = json.getString("image");
        dataURL = json.getString("data");
        type = json.getString("type");
    }

    public View getView(final Activity activity, ViewGroup parent, final BillingProcessor billing) {
        if (container != null)
            return container;

        this.activity = activity;
        this.billing = billing;
        container = activity.getLayoutInflater().inflate(R.layout.item_store, parent, false);
        //title = (TextView) container.findViewById(R.id.productTitle);
        //desc = (TextView) container.findViewById(R.id.productDescription);
        buttonRemove = (Button) container.findViewById(R.id.buttonRemove);
        buttonAction = (Button) container.findViewById(R.id.buttonAction);
        image = (ImageView) container.findViewById(R.id.productImage);
        progressImage = (ProgressView) container.findViewById(R.id.progressImage);
        progressData = (ProgressView) container.findViewById(R.id.progressData);

        // details
        if (details == null) {
            details = billing.getPurchaseListingDetails(productId);
            isPurchased = billing.isPurchased(productId);
        }

        // text
        /*if (details != null) {
            title.setText(details.title);
            desc.setText(details.description);
        } else {
            title.setText(productId);
            desc.setVisibility(View.GONE);
        }*/

        // image
        new AsyncHttpClient().get(imageURL, new DataAsyncProgressHandler(progressImage) {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                int len = 0;
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].getName().equals("Content-Length"))
                        len = Integer.valueOf(headers[i].getValue());
                }
                image.setImageBitmap(BitmapFactory.decodeByteArray(responseBody, 0, len));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
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

        return container;
    }

    private void refreshButton() {

        // remove
        isInstalled = new Database(activity).isInstalled(productId);
        if (isInstalled) {
            buttonRemove.setVisibility(View.VISIBLE);
        } else
            buttonRemove.setVisibility(View.GONE);

        // error
        if (isError) {
            buttonAction.setText(activity.getString(R.string.button_error));
            buttonAction.setEnabled(false);
            buttonAction.setVisibility(View.VISIBLE);
        }

        // up-to-date
        if (new Database(activity).isInstalled(productId)) {
            buttonAction.setVisibility(View.GONE);
        }

        // download
        else if (isPurchased || details == null) {
            buttonAction.setText(activity.getString(R.string.button_download));
            buttonAction.setEnabled(true);
            buttonAction.setVisibility(View.VISIBLE);
            buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonAction.setClickable(false);
                    new AsyncHttpClient().get(dataURL, new FileAsyncProgressHandler(activity, progressData) {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, File file) {
                            if (type.equals("xml"))
                                isError = new Database(activity).loadDataXML(productId, 0, file);
                            else if (type.equals("csv"))
                                isError = new Database(activity).loadDataCSV(productId, 0, file);
                            refreshButton();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                            isError = true;
                            refreshButton();
                            throwable.printStackTrace();
                        }
                    });
                }
            });
        }

        // purchase
        else if (details != null) {
            buttonAction.setText(activity.getString(R.string.button_purchase));
            buttonAction.setEnabled(true);
            buttonAction.setVisibility(View.VISIBLE);
            buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    billing.purchase(activity, productId);
                }
            });
        }

        // unknown
        else {
            buttonAction.setVisibility(View.GONE);
        }
    }
}
