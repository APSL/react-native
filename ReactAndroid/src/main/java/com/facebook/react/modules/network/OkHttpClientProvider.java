/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.modules.network;

import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

/**
 * Helper class that provides the same OkHttpClient instance that will be used for all networking
 * requests.
 */
public class OkHttpClientProvider {

  // Centralized OkHttpClient for all networking requests.
  private static @Nullable OkHttpClient sClient;

  public static OkHttpClient getOkHttpClient() {
    if (sClient == null) {
      sClient = createClient();
    }
    return sClient;
  }

  // okhttp3 OkHttpClient is immutable
  // This allows app to init an OkHttpClient with custom settings.
  public static void replaceOkHttpClient(OkHttpClient client) {
    sClient = client;
  }

  public static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
    if (Build.VERSION.SDK_INT < 21) {
      try {
        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, null, null);
        client.sslSocketFactory(new TLSSocketFactory(sc.getSocketFactory()));

        ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build();

        List<ConnectionSpec> specs = new ArrayList<>();
        specs.add(cs);
        specs.add(ConnectionSpec.COMPATIBLE_TLS);
        specs.add(ConnectionSpec.CLEARTEXT);

        client.connectionSpecs(specs);
      } catch (Exception exc) {
        Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
      }
    }

    return client;
  }

  private static OkHttpClient createClient() {
    // No timeouts by default
    OkHttpClient.Builder client = new OkHttpClient.Builder()
      .connectTimeout(0, TimeUnit.MILLISECONDS)
      .readTimeout(0, TimeUnit.MILLISECONDS)
      .writeTimeout(0, TimeUnit.MILLISECONDS)
      .cookieJar(new ReactCookieJarContainer());
    return enableTls12OnPreLollipop(client).build();
  }
}
