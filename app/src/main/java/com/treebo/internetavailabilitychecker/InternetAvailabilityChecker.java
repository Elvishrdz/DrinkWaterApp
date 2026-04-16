package com.treebo.internetavailabilitychecker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

// TODO: replace with a better implementation to check the internet connectivity
public class InternetAvailabilityChecker {
    private static InternetAvailabilityChecker instance;
    private ConnectivityManager connectivityManager;
    private List<InternetConnectivityListener> listeners = new ArrayList<>();
    private boolean isConnected = false;

    private InternetAvailabilityChecker(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    updateConnection(true);
                }

                @Override
                public void onLost(Network network) {
                    updateConnection(false);
                }
            });
        } else {
            // Simplified check for older versions
            isConnected = isNetworkAvailable(context);
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new InternetAvailabilityChecker(context.getApplicationContext());
        }
    }

    public static InternetAvailabilityChecker getInstance() {
        if (instance == null) {
            throw new IllegalStateException("InternetAvailabilityChecker not initialized. Call init() first.");
        }
        return instance;
    }

    public void addInternetConnectivityListener(InternetConnectivityListener listener) {
        if (listener != null) {
            listeners.add(listener);
            listener.onInternetConnectivityChanged(isConnected);
        }
    }

    public void removeInternetConnectivityListener(InternetConnectivityListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void removeInternetConnectivityChangeListener(InternetConnectivityListener listener) {
        removeInternetConnectivityListener(listener);
    }

    private void updateConnection(boolean connected) {
        if (isConnected != connected) {
            isConnected = connected;
            new Handler(Looper.getMainLooper()).post(() -> {
                for (InternetConnectivityListener listener : listeners) {
                    listener.onInternetConnectivityChanged(isConnected);
                }
            });
        }
    }
}
