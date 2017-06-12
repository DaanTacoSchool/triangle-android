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
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
   // private boolean recording = false;
    private Button captureButton,switchCamera;
    private boolean websocketConnected = false;
    private WebSocket mWebSocketInstance;
    private int camId;
    private Context myContext;
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

    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    // release the old camera instance
                    // switch camera, from the front and the back and vice versa
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
        public void onClick(View v) {
            //TODO: catching exceptions
           ch.startStopRecording(myContext);
        }
    };
    //MARKER: old code

//  private boolean prepareVideoRecorder() {
//
//   // mCamera = ch.getCameraInstance();
//    mMediaRecorder = new MediaRecorder();
//
//    // Step 1: Unlock and set camera to MediaRecorder
//    mCamera.unlock();
//    mMediaRecorder.setCamera(mCamera);
//
//    // Step 2: Set sources
//    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
//    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//
//    if(CamcorderProfile.hasProfile(camId,1)){
//     // mMediaRecorder.setProfile(CamcorderProfile.get(camId,1));//old
//
//
//      try {
//        mMediaRecorder.setProfile(CamcorderProfile.get(camId, CamcorderProfile.QUALITY_480P));
//      }catch (Exception e){
//        Log.e(TAG,"480p not found for selected cam");
//      }
//
//
//    }else{
//     // mMediaRecorder.setProfile(CamcorderProfile.get(camId,CamcorderProfile.QUALITY_480P));
//      //TODO: Try-catch
//      try {
//        mMediaRecorder.setProfile(CamcorderProfile.get(camId, CamcorderProfile.QUALITY_HIGH));//Highest possible quality
//      }catch (Exception e){
//        Log.e(TAG,"quality error");
//      }
//
//    }
//
//    // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
//   // mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
//
//    // Step 4: Set output file
//    final String fileName = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
//    mMediaRecorder.setOutputFile(fileName);
//
//    // The below part is unchanged
//    // Step 5: Set the preview output
//    mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
//    mMediaRecorder.setOnInfoListener(new OnInfoListener() {
//      @Override public void onInfo(MediaRecorder mr, int what, int extra) {
//        // Handle the on duration exceeded
//        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
//
//
//          if (websocketConnected) {
//            File file = new File(fileName);
//
//            int size = (int) file.length();
//            byte bytes[] = new byte[size];
//            byte tmpBuff[] = new byte[size];
//            try {
//              FileInputStream fis = new FileInputStream(file);
//
//              int read = fis.read(bytes, 0, size);
//              if (read < size) {
//                int remain = size - read;
//                while (remain > 0) {
//                  read = fis.read(tmpBuff, 0, remain);
//                  System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
//                  remain -= read;
//                }
//              }
//            } catch (IOException e) {
//              Log.e(TAG, "IoExc", e);
//            }
//            mWebSocketInstance.send(bytes);
//          }
//        }
//      }
//    });
//
//    mMediaRecorder.setMaxDuration(1000);
//
//    // Step 6: Prepare configured MediaRecorder
//    try {
//      mMediaRecorder.prepare();
//    } catch (IllegalStateException e) {
//      Log.e(TAG, "IllegalStateException preparing MediaRecorder", e);
//      releaseMediaRecorder();
//      return false;
//    } catch (IOException e) {
//      Log.e(TAG, "IOException preparing MediaRecorder", e);
//      releaseMediaRecorder();
//      return false;
//    }
//    return true;
//  }



    //MARKER: above new, below old
    /** Check if this device has a camera */
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
