package triangle.triangleapp.presentation.stream;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.support.annotation.Nullable;

import triangle.triangleapp.domain.ChatAction;
import triangle.triangleapp.TriangleApplication;
import triangle.triangleapp.helpers.AdapterType;
import triangle.triangleapp.logic.NetworkBroadcastReceiver;
import triangle.triangleapp.logic.NetworkChangeCallback;
import triangle.triangleapp.logic.StreamManager;
import triangle.triangleapp.logic.StreamManagerCallback;

/**
 * Created by Kevin Ly on 6/15/2017.
 */

public class StreamPresenter implements StreamManagerCallback {
    private StreamView mView;
    private StreamManager mManager;

    public StreamPresenter(StreamView streamView) {
        mView = streamView;
        mManager = new StreamManager(this);


        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        NetworkBroadcastReceiver broadcastReceiver = new NetworkBroadcastReceiver();
        broadcastReceiver.setNetworkChangeCallback(new NetworkChangeCallback() {
            @Override
            public void onConnected() {
                mView.networkConnectivityChanged(ConnectionState.CONNECTED, null);
            }

            @Override
            public void onDisconnected(@Nullable String reason) {
                mView.networkConnectivityChanged(ConnectionState.DISCONNECTED, reason);
            }
        });
        TriangleApplication.getAppContext().registerReceiver(broadcastReceiver, filter);
    }

    /**
     * Starts or stops the streaming
     */
    public void stream() {
        mView.showPreview();
        mManager.setPreviewView(mView.getPreviewSurface());
        boolean isStreaming = mManager.stream();

        if (isStreaming) {
            mView.streamStarted();
        } else {
            mView.hidePreview();
            mView.streamStopped();
        }
    }

    /**
     * Checks if streaming
     */
    public boolean isStreaming() {
        return mManager.getIsStreaming();
    }

    @Override
    public void chatMessageReceived(@NonNull ChatAction message) {
        mView.showMessage(message);
    }

    @Override
    public void streamConnected() {
        mView.connected(AdapterType.TYPE_STREAM);
    }

    @Override
    public void streamError(@NonNull Exception exception, boolean fatal) {
        mView.errorOccurred(AdapterType.TYPE_STREAM, exception, fatal);
    }

    @Override
    public void chatConnected() {

    }

    @Override
    public void chatError(@NonNull Exception exception) {

    }

    /**
     * Sends a chat message
     *
     * @param message The message to be sent
     */
    public void sendChatMessage(@NonNull String message) {
        mManager.sendChatMessage(message);
    }

    @Override
    public void setViewersCount(int viewersAmount) {
        Log.i("Viewers", "Amount: " + viewersAmount); //todo: Call view and show it
        mView.showViewersCount(viewersAmount);
    }
}
