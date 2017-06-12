package triangle.triangleapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
  private String TAG = "MainActivity";
  private Camera mCamera;
  private CameraPreview mPreview;
  private MediaRecorder mMediaRecorder;
  private boolean isRecording = false;
  private Button captureButton,switchCamera;
  private boolean websocketConnected = false;
  private WebSocket mWebSocketInstance;
  private int camId;
  private Context myContext;
  private boolean cameraFront = false;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initMain();

  }

  private void initMain(){
    if (checkCameraHardware(this)) {

      // Create our Preview view and set it as the content of our activity.
      mPreview = new CameraPreview(this, mCamera);
      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
      preview.addView(mPreview);

      captureButton = (Button) findViewById(R.id.button_capture);
      captureButton.setOnClickListener(captureListener);

      switchCamera = (Button) findViewById(R.id.button_switchCamera);
      switchCamera.setOnClickListener(switchCameraListener);
      myContext = this;
      initWebsocket();
    }
  }

  private int findFrontFacingCamera() {
    int cameraId = -1;
    // Search for the front facing camera
    int numberOfCameras = Camera.getNumberOfCameras();
    for (int i = 0; i < numberOfCameras; i++) {
      Camera.CameraInfo info = new Camera.CameraInfo();
      Camera.getCameraInfo(i, info);
      if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        cameraId = i;
        cameraFront = true;
        break;
      }
    }
    camId=cameraId;
    return cameraId;
  }

  private int findBackFacingCamera() {
    int cameraId = -1;
    // Search for the back facing camera
    // get the number of cameras
    int numberOfCameras = Camera.getNumberOfCameras();
    // for every camera check
    for (int i = 0; i < numberOfCameras; i++) {
      CameraInfo info = new CameraInfo();
      Camera.getCameraInfo(i, info);
      if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
        cameraId = i;
        cameraFront = false;
        break;
      }
    }
    camId=cameraId;
    return cameraId;
  }

  //FIXME
  public void onResume() {
    super.onResume();
    if (!hasCamera(myContext)) {
      Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
      toast.show();
      finish();
    }
    if (mCamera == null) {
      // if the front facing camera does not exist
      if (findFrontFacingCamera() < 0) {
        Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
        switchCamera.setVisibility(View.GONE);
      }
      mCamera = Camera.open(findBackFacingCamera());
      mPreview.refreshCamera(mCamera);
    }
  }

  OnClickListener switchCameraListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      // get the number of cameras
      if (!recording) {
        int camerasNumber = Camera.getNumberOfCameras();
        if (camerasNumber > 1) {
          // release the old camera instance
          // switch camera, from the front and the back and vice versa
          releaseCamera();
          chooseCamera();
        } else {
          Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
          toast.show();
        }
      }
    }
  };

  public void chooseCamera() {
    // if the camera preview is the front
    if (cameraFront) {
      int cameraId = findBackFacingCamera();
      if (cameraId >= 0) {
        // open the backFacingCamera
        // set a picture callback
        // refresh the preview

        mCamera = Camera.open(cameraId);
        // mPicture = getPictureCallback();
        mPreview.refreshCamera(mCamera);
      }
    } else {
      int cameraId = findFrontFacingCamera();
      if (cameraId >= 0) {
        // open the backFacingCamera
        // set a picture callback
        // refresh the preview

        mCamera = Camera.open(cameraId);
        // mPicture = getPictureCallback();
        mPreview.refreshCamera(mCamera);
      }
    }
  }

  private boolean hasCamera(Context context) {
    // check if the device has camera
    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
      return true;
    } else {
      return false;
    }
  }

  @Override protected void onPause() {
    super.onPause();
    releaseMediaRecorder();       // if you are using MediaRecorder, release it first
    releaseCamera();              // release the camera immediately on pause event
  }

  //MARKER: replacement code
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private boolean prepareMediaRecorder() {

    mMediaRecorder = new MediaRecorder();

    mCamera.unlock();
    mMediaRecorder.setCamera(mCamera);

    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


    if(CamcorderProfile.hasProfile(camId,1)){
      mMediaRecorder.setProfile(CamcorderProfile.get(camId,1));

    }else{
      mMediaRecorder.setProfile(CamcorderProfile.get(camId,0));
      //shouldnt work

    }
    //mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));


    try{
      final String fileName = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
      mMediaRecorder.setOutputFile(fileName);
    }catch(Exception e){
      mMediaRecorder.setOutputFile("/sdcard/myvideo.mp4");
      Log.e(TAG,"reverting to default output");
    }

    mMediaRecorder.setMaxDuration(600000); // Set max duration 60 sec.
    mMediaRecorder.setMaxFileSize(50000000); // Set max file size 50M

    try {
      mMediaRecorder.prepare();
    } catch (IllegalStateException e) {
      releaseMediaRecorder();
      return false;
    } catch (IOException e) {
      releaseMediaRecorder();
      return false;
    }
    return true;

  }

  boolean recording = false;
  OnClickListener captureListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      //TODO: catching exceptions
      if (recording) {
        // stop recording and release camera
        mMediaRecorder.stop(); // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        Toast.makeText(myContext, "Video captured!", Toast.LENGTH_LONG).show();
        recording = false;
      } else {
        if (!prepareMediaRecorder()) {
          Toast.makeText(myContext, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
          finish();
        }
        // work on UiThread for better performance
        runOnUiThread(new Runnable() {
          public void run() {
            // If there are stories, add them to the table

            try {
              mMediaRecorder.start();
            } catch (final Exception ex) {
              // Log.i("---","Exception in thread");
            }
          }
        });

        recording = true;
      }
    }
  };

  //MARKER: old code

//  private boolean prepareVideoRecorder() {
//
//   // mCamera = CameraHelper.getCameraInstance();
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

  private void releaseMediaRecorder() {
    if (mMediaRecorder != null) {
      mMediaRecorder.reset();   // clear recorder configuration
      mMediaRecorder.release(); // release the recorder object
      mMediaRecorder = null;
      mCamera.lock();           // lock camera for later use
    }
  }


  private void releaseCamera() {
    if (mCamera != null) {
      mCamera.release();        // release the camera for other applications
      mCamera = null;
    }
  }

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

  public static final int MEDIA_TYPE_IMAGE = 1;
  public static final int MEDIA_TYPE_VIDEO = 2;

  /** Create a file Uri for saving an image or video */
  private static Uri getOutputMediaFileUri(int type) {
    return Uri.fromFile(getOutputMediaFile(type));
  }

  /** Create a File for saving an image or video */
  private static File getOutputMediaFile(int type) {
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    File mediaStorageDir =
        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "MyCameraApp");
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        Log.d("MyCameraApp", "failed to create directory");
        return null;
      }
    }

    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File mediaFile;
    if (type == MEDIA_TYPE_IMAGE) {
      mediaFile =
          new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    } else if (type == MEDIA_TYPE_VIDEO) {
      mediaFile =
          new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
    } else {
      return null;
    }

    return mediaFile;
  }
}
