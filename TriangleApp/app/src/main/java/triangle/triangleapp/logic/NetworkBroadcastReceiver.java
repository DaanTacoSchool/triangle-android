package triangle.triangleapp.logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static android.net.ConnectivityManager.EXTRA_EXTRA_INFO;

/**
 * Created by Koen on 21-06-17.
 */

public class NetworkBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangeReceiver";
    private NetworkChangeCallback mCallback;

    public void setNetworkChangeCallback(NetworkChangeCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = isDeviceConnected(context);

        if (isConnected) {
            Log.i(TAG, "Connection established");
            if (mCallback != null) {
                mCallback.onConnected();
            }
        } else {
            Log.i(TAG, "Connection lost");
            if (mCallback != null) {
                String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
                mCallback.onDisconnected(reason);
            }
        }
    }

    private boolean isDeviceConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
