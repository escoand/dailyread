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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;

import java.util.HashMap;

public class Billing implements BillingProcessor.IBillingHandler {
    private static Billing instance;
    private final Context context;
    private final BillingProcessor billing;
    private final HashMap<String, String> mimeTypes = new HashMap<>();
    private OnBillingInitializedListener listener = null;

    private Billing(Context context) {
        this.context = context;
        billing = new BillingProcessor(context, context.getString(R.string.license_key), context.getString(R.string.merchant_id), this);
    }

    public static synchronized Billing getInstance(Context context) {
        if (instance == null)
            instance = new Billing(context);
        return instance;
    }

    public SkuDetails getPurchaseListingDetails(String productId) {
        return billing.getPurchaseListingDetails(productId);
    }

    public TransactionDetails getPurchaseTransactionDetails(String productId) {
        return billing.getPurchaseTransactionDetails(productId);
    }

    public boolean isInitialized() {
        return billing.isInitialized();
    }

    public void setOnBillingInitializedListener(OnBillingInitializedListener listener) {
        this.listener = listener;
        if (billing.isInitialized())
            listener.onBillingInitialized();
    }

    @Override
    public void onBillingInitialized() {
        billing.loadOwnedPurchasesFromGoogle();
        if (listener != null)
            listener.onBillingInitialized();
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        LogHandler.log(error);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        DownloadHandler.startDownload(
                context,
                details.purchaseInfo.signature,
                details.purchaseInfo.responseData,
                getPurchaseListingDetails(productId).title.replaceFirst("", ""),
                mimeTypes.get(productId)
        );
    }

    public void purchase(Activity activity, String productId, String mimeType) {
        mimeTypes.put(productId, mimeType);
        billing.purchase(activity, productId);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        billing.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void finalize() throws Throwable {
        if (billing != null)
            billing.release();
        super.finalize();
    }

    public interface OnBillingInitializedListener {
        void onBillingInitialized();
    }
}
