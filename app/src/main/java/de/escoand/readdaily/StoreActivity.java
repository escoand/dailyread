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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
    private final OkHttpClient client = new OkHttpClient();
    private StoreArrayAdapter listAdapter;
    private BillingProcessor billing;

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

        ListView list = (ListView) findViewById(R.id.listView);
        list.setEmptyView(findViewById(R.id.listLoading));
        listAdapter = new StoreArrayAdapter(this, billing);
        list.setAdapter(listAdapter);

        DownloadHandler.isStoragePermissionGranted(this);
    }

    @Override
    public void onBillingInitialized() {
        billing.loadOwnedPurchasesFromGoogle();

        Request request = new Request.Builder()
                .url(getString(R.string.product_list_url))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final String body = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray products = new JSONArray(body);
                            for (int i = 0; i < products.length(); i++)
                                listAdapter.add(new StoreListItem(products.getString(i)));
                            listAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            // TODO non-technical message to user
                            Log.e(getClass().getName(), Log.getStackTraceString(e));
                        }
                    }
                });
            }

            @Override
            public void onFailure(final Call call, final IOException e) {
                Log.e(getClass().getName(), Log.getStackTraceString(e));
            }
        });
    }

    @Override
    public void onProductPurchased(final String productId, final TransactionDetails details) {
        // ToDo start downloading
    }

    @Override
    public void onPurchaseHistoryRestored() {
        // empty, but must be implemented
    }

    @Override
    public void onBillingError(final int errorCode, final Throwable error) {
        // TODO non-technical message to user
        Log.e("billing", Log.getStackTraceString(error));
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!billing.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == DownloadHandler.REQUEST_PERMISSIONS &&
                (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Snackbar.make(findViewById(R.id.content), getString(R.string.message_missing_permission), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        if (billing != null)
            billing.release();
        super.onDestroy();
    }
}
