package triangle.triangleapp.presentation.stream.impl;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import triangle.triangleapp.R;
import triangle.triangleapp.domain.ChatAction;
import triangle.triangleapp.helpers.AdapterType;
import triangle.triangleapp.presentation.impl.ChatFragment;
import triangle.triangleapp.presentation.stream.ConnectionState;
import triangle.triangleapp.presentation.stream.StreamPresenter;
import triangle.triangleapp.presentation.stream.StreamView;

/**
 * Created by Kevin Ly on 6/15/2017.
 */

public class StreamActivity extends AppCompatActivity implements StreamView, ChatFragment.OnFragmentInteractionListener {
    private static final String TAG = "StreamActivity";
    private StreamPresenter mPresenter;
    private SurfaceView mCameraSurfaceView;
    private static boolean firstStart = true;

    private Button mButtonStream;
    private Button mButtonChat;
    private TextView mCountViewers;
    private ChatFragment chatFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mPresenter = new StreamPresenter(this);

        mCameraSurfaceView = (SurfaceView) findViewById(R.id.surface_camera_preview);
        mButtonStream = (Button) findViewById(R.id.btn_stream);
        mCountViewers = (TextView) findViewById(R.id.textView_counting_viewers);
        mButtonStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start or stop the stream
                mPresenter.stream();
            }
        });

        chatFrag = ChatFragment.newInstance();
        mButtonChat = (Button) findViewById(R.id.btnChat);
        mButtonChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in,
                        android.R.animator.fade_out);*/

                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                if (chatFrag.isAdded()) { // if the fragment is already in container
                    if (chatFrag.isHidden()) {
                        ft.show(chatFrag);
                    } else {
                        ft.hide(chatFrag);
                    }
                } else { // fragment needs to be added to frame container
                    ft.add(R.id.frameLayout, chatFrag, "A");
                }

                ft.commit();
            }
        });
    }

    @Override
    public void streamStarted() {
        mButtonStream.setText(R.string.btn_stop_streaming);

        Toast.makeText(this, R.string.notify_streaming_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void streamStopped() {
        mButtonStream.setText(R.string.btn_start_streaming);
        Toast.makeText(this, R.string.notify_streaming_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPreview() {
        mCameraSurfaceView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidePreview() {
        mCameraSurfaceView.setVisibility(View.INVISIBLE);
    }

    @Override
    public Surface getPreviewSurface() {
        SurfaceHolder holder = mCameraSurfaceView.getHolder();
        if (holder != null) {
            return holder.getSurface();
        }
        return null;
    }

    @Override
    public void networkConnectivityChanged(@ConnectionState int state, @Nullable String reason) {

        switch (state) {
            case ConnectionState.CONNECTED: {

                // Do not execute on start
                if (!firstStart) {
                    Toast.makeText(getApplicationContext(), R.string.notify_connected, Toast.LENGTH_LONG).show();
                }

                mButtonStream.setEnabled(true);
                break;
            }
            case ConnectionState.DISCONNECTED: {
                Toast.makeText(getApplicationContext(), R.string.notify_disconnected, Toast.LENGTH_LONG).show();
                mButtonStream.setEnabled(false);
                if (mPresenter.isStreaming()) {
                    mPresenter.stream();
                }
                break;
            }
        }

        firstStart = false;
    }

    @Override
    public void connected(@AdapterType int type) {
        Log.i(TAG, "Connected adapter = " + type);
    }

    @Override
    public void errorOccurred(@AdapterType int type, @NonNull final Exception exception, boolean fatal) {
        Log.e(TAG, "Error adapter = " + type, exception);

        //Determine error text
        String exceptionType = exception.getClass().getSimpleName();

        final String errorMsg;
        switch (exceptionType) {
            case "ConnectException":
                errorMsg = getString(R.string.err_connect_server);
                break;
            case "PEMException":
                errorMsg = getString(R.string.err_decode_server_pubkey);
                break;
            default:
                Log.e(TAG, "Unhandled exception type: " + exceptionType);
                errorMsg = getString(R.string.err_unknown);
                break;
        }

        //Show toast on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int toastDuration = Toast.LENGTH_LONG;
                Toast.makeText(StreamActivity.this, errorMsg, toastDuration).show();
            }
        });

        if (fatal) {
            this.finish();
        }
    }

    @Override
    public void showMessage(@NonNull final ChatAction message) {
        final ChatAction msg = message;
        runOnUiThread(new Runnable() {
            public void run() {
                Log.i(TAG, "Received message, message = " + msg.getMessage());
                if (chatFrag.isAdded()) {
                    chatFrag.addReceivedMessage(msg);
                } else {
                    Log.e(TAG, "error");
                    android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.add(R.id.frameLayout, chatFrag, "A");
                    ft.hide(chatFrag);
                    ft.commit();

                }

            }
        });

    }

    @Override
    public void showViewersCount(@NonNull final int viewersCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCountViewers.setText(Integer.toString(viewersCount));
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSendMessage(String msg) {
        mPresenter.sendChatMessage(msg);
    }

    public void showCount() {

    }
}
