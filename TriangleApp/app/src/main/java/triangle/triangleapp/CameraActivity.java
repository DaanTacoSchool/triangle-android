package triangle.triangleapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private CameraPreview mPreview;
    private Context myContext;
    private Button captureButton,switchCamera;
    private Camera mCamera;//required
    //below variables are required
    private MediaRecorder mMediaRecorder;
    private boolean websocketConnected = false;
    private WebSocket mWebSocketInstance;
    private int camId;
    private boolean cameraFront = false;
    private CameraHelper ch=  new CameraHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initMain();
    }
    private void initMain(){
        if (checkCameraHardware(this)) {

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);
            ch.setCameraPreview(mPreview);
            captureButton = (Button) findViewById(R.id.button_capture);
            captureButton.setOnClickListener(captureListener);
            switchCamera = (Button) findViewById(R.id.button_switchCamera);
            switchCamera.setOnClickListener(switchCameraListener);
            myContext = this;

        }
    }
    public void onResume() {
        super.onResume();
       ch.resume(myContext);
    }

    View.OnClickListener switchCameraListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    ch.releaseCamera();
                    ch.chooseCamera();
                } else {
                    Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    };

    @Override protected void onPause() {
        super.onPause();
        ch.releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        ch.releaseCamera();              // release the camera immediately on pause event
    }
    boolean recording = false;
    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {ch.startStopRecording(myContext);}};
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
        // no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
    private void initWebsocket() {
        // Send the file
        String url = "ws://145.49.28.137:1234/send";
        String protocol = "WS";
        AsyncHttpClient.getDefaultInstance()
                .websocket(url, protocol, new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override public void onCompleted(Exception ex, WebSocket webSocket) {
                        if (ex != null) {
                            ex.printStackTrace();
                            return;
                        }

                        websocketConnected = true;
                        mWebSocketInstance = webSocket;
                    }
                });
    }

}
