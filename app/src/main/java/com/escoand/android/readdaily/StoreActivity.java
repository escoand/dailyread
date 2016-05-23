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

package com.escoand.android.readdaily;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import cz.msebera.android.httpclient.Header;

public class StoreActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    private final AsyncHttpClient client = new AsyncHttpClient();
    private StoreArrayAdapter listAdapter;
    private BillingProcessor billing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nodrawer);

        billing = new BillingProcessor(
                getBaseContext(),
                getString(R.string.license_key),
                getString(R.string.merchant_id),
                this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onBackPressed();
                setResult(Activity.RESULT_CANCELED, new Intent());
                finish();
            }
        });

        getLayoutInflater().inflate(R.layout.fragment_list, (ViewGroup) findViewById(R.id.content), true);

        ListView list = (ListView) findViewById(R.id.listView);
        list.setEmptyView(findViewById(R.id.listLoading));
        listAdapter = new StoreArrayAdapter(this, billing);
        list.setAdapter(listAdapter);

        getPurchasesList();
    }

    private void getPurchasesList() {
        client.get(getString(R.string.products_list_url), new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                listAdapter.clear();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        listAdapter.add(new StoreListItem(response.getJSONObject(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                throwable.printStackTrace();
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!billing.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (billing != null)
            billing.release();
        super.onDestroy();
    }
}
