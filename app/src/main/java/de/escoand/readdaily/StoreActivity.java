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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import org.json.JSONArray;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class StoreActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    private StoreArrayAdapter listAdapter;
    private BillingProcessor billing;
    private ListView list;

    @Override
    protected void attachBaseContext(final Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
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
            public void onClick(final View v) {
                //onBackPressed();
                setResult(Activity.RESULT_CANCELED, new Intent());
                finish();
            }
        });

        getLayoutInflater().inflate(R.layout.fragment_store, (ViewGroup) findViewById(R.id.content), true);

        list = (ListView) findViewById(R.id.listView);
        list.setEmptyView(findViewById(R.id.listLoading));
        listAdapter = new StoreArrayAdapter(this, billing);
        list.setAdapter(listAdapter);
    }

    @Override
    public void onBillingInitialized() {
        billing.loadOwnedPurchasesFromGoogle();
        new OnReloadClickListener().onClick(null);
    }

    @Override
    public void onProductPurchased(@NonNull final String productId, @NonNull final TransactionDetails details) {
        for (int i = 0; i < listAdapter.getCount(); i++)
            if (listAdapter.getItem(i).getProductId().equals(productId))
                listAdapter.getItem(i).download();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        // empty, but must be implemented
    }

    @Override
    public void onBillingError(final int errorCode, final Throwable error) {
        // TODO non-technical message to user
        LogHandler.log(error);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
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

    private class OnReloadClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View view) {
            new OkHttpClient()
                    .newCall(new Request.Builder().url(getString(R.string.product_list_url)).build())
                    .enqueue(new RequestCallback());
        }
    }

    private class RequestCallback implements Callback {
        @Override
        public void onResponse(final Call call, final Response response) {
            try {
                final String body = response.body().string();
                final JSONArray products = new JSONArray(body);

                for (int i = 0; i < products.length(); i++) {
                    final String name = products.getJSONObject(i).getString("name");
                    final String mime = products.getJSONObject(i).getString("mime");
                    final StoreListItem item = new StoreListItem(name, mime);

                    // append to list
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listAdapter.add(item);
                            listAdapter.notifyDataSetChanged();
                        }
                    });
                }

            } catch (final Exception e) {
                LogHandler.logAndShow(e, list, R.string.message_download_list,
                        R.string.button_reload, new OnReloadClickListener());
            }
        }

        @Override
        public void onFailure(final Call call, final IOException e) {
            LogHandler.logAndShow(e, list, R.string.message_download_list, R.string.button_reload,
                    new OnReloadClickListener());
        }
    }
}
