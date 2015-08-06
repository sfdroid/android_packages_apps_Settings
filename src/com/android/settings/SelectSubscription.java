/* Copyright (c) 2010-14, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.settings;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import java.util.List;

public class SelectSubscription extends  TabActivity {

    private static final String LOG_TAG = "SelectSubscription";
    public static final String SUBSCRIPTION_KEY = "subscription";
    public static final String PACKAGE = "PACKAGE";
    public static final String TARGET_CLASS = "TARGET_CLASS";

    private String[] tabLabel = {"SUB 1", "SUB 2", "SUB 3"};

    private TabSpec subscriptionPref;

    @Override
    public void onPause() {
        super.onPause();
    }

    /*
     * Activity class methods
     */

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        log("Creating activity");


        Intent intent =  getIntent();
        if ((intent.getStringExtra(PACKAGE) == null)
                ||(intent.getStringExtra(TARGET_CLASS) == null)){
            intent.putExtra(PACKAGE, "com.android.settings");
            intent.putExtra(TARGET_CLASS, "com.android.settings.deviceinfo.MSimSubscriptionStatus");
        }
        if (intent.getStringExtra(PACKAGE).equals("com.android.settings")
             && intent.getStringExtra(TARGET_CLASS).equals("com.android.settings.IccLockSettings")){
            setTitle(getString(R.string.sim_lock_settings_title));
        }else{
            setTitle(getString(R.string.subs_settings));
        }

        setContentView(R.layout.select_subscription);
        TabHost tabHost = getTabHost();

        String pkg = intent.getStringExtra(PACKAGE);
        String targetClass = intent.getStringExtra(TARGET_CLASS);
        if (pkg == null || pkg.length() <= 0 || targetClass == null || targetClass.length() <= 0) {
            return;
        }

        int numPhones = TelephonyManager.getDefault().getPhoneCount();

        for (int i = 0; i < numPhones; i++) {
            SubscriptionInfo sir = SubscriptionManager.from(this)
                    .getActiveSubscriptionInfoForSimSlotIndex(i);
            String displayName =
                    (sir != null) ? sir.getDisplayName().toString() : tabLabel[i];

            log("Creating SelectSub activity = " + i + " displayName = " + displayName);

            // Add phone Id to the displayName to differentiate
            // tag names of tabs
            subscriptionPref = tabHost.newTabSpec(displayName + i);
            subscriptionPref.setIndicator(displayName);

            intent = new Intent().setClassName(pkg, targetClass)
                    .setAction(intent.getAction());

            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, i);
            subscriptionPref.setContent(intent);
            tabHost.addTab(subscriptionPref);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
