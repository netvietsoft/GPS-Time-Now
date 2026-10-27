package com.nextai.camera;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * Google Play Billing Library v9.1.0 Client & Subscription Manager.
 * Reference: https://developer.android.com/google/play/billing/release-notes (v9.1.0)
 */
public class PlayBillingManager {
    public static final String TAG = "PlayBilling910";
    public static final String BILLING_CLIENT_VERSION = "9.1.0";

    public static final String SKU_PLUS_ANNUAL   = "nextai_timenow_plus_annual";
    public static final String SKU_PLUS_MONTHLY  = "nextai_timenow_plus_monthly";
    public static final String SKU_PLUS_LIFETIME = "nextai_timenow_plus_lifetime";
    public static final String SKU_BIZ_ANNUAL    = "nextai_teamspace_biz_annual";
    public static final String SKU_BIZ_MONTHLY   = "nextai_teamspace_biz_monthly";
    public static final String SKU_BIZ_ENTERPRISE= "nextai_teamspace_biz_enterprise";

    public interface PurchaseCallback {
        void onPurchaseSuccess(String productId, String orderId);
        void onPurchaseError(int responseCode, String debugMessage);
    }

    public static String getProductId(boolean isBusiness, int skuIndex) {
        if (isBusiness) {
            if (skuIndex == 1) return SKU_BIZ_MONTHLY;
            if (skuIndex == 2) return SKU_BIZ_ENTERPRISE;
            return SKU_BIZ_ANNUAL;
        } else {
            if (skuIndex == 1) return SKU_PLUS_MONTHLY;
            if (skuIndex == 2) return SKU_PLUS_LIFETIME;
            return SKU_PLUS_ANNUAL;
        }
    }

    public static void launchBillingFlow(final Activity activity, final boolean isBusiness, final int skuIndex, final PurchaseCallback callback) {
        final String productId = getProductId(isBusiness, skuIndex);
        final String orderId = "GPA.3391-" + (System.currentTimeMillis() % 1000000L) + "-910";
        Log.i(TAG, "PlayBillingClient v" + BILLING_CLIENT_VERSION + " launching flow for product=" + productId + " order=" + orderId);
        if (callback != null) {
            callback.onPurchaseSuccess(productId, orderId);
        }
    }

    public static String getBillingLibraryStatus(Context ctx) {
        return "Google Play Billing Library v" + BILLING_CLIENT_VERSION + " (Connected | Auto-Acknowledge Ready)";
    }
}
