package triangle.triangleapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
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

public class MainActivity extends AppCompatActivity {
  private String TAG = "MainActivity";
  private Camera mCamera;
  private CameraPreview mPreview;
  private MediaRecorder mMediaRecorder;
  private boolean isRecording = false;
  private Button captureButton;
  private boolean websocketConnected = false;
  private WebSocket mWebSocketInstance;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (CameraHelper.checkCameraHardware(this)) {
      // Create an instance of Camera
      mCamera = CameraHelper.getCameraInstance();

      // Create our Preview view and set it as the content of our activity.
      mPreview = new CameraPreview(this, mCamera);
      FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
      preview.addView(mPreview);

      captureButton = (Button) findViewById(R.id.button_capture);
      captureButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          record();
        }
      });
      initWebsocket();
    }
  }

  @Override protected void onPause() {
    super.onPause();
    releaseMediaRecorder();       // if you are using MediaRecorder, release it first
    releaseCamera();              // release the camera immediately on pause event
  }

  private void record() {
    if (isRecording) {

      // inform the user that recording has stopped
      captureButton.setText("Capture");
      isRecording = false;
      stopStreaming(true);
    } else {
      captureButton.setText("Stop");
      // Start the stream
      startStreaming(true);
    }
  }

  private void initializeVideoRecorder(boolean firstInit) {
    if (firstInit) {
      mCamera = CameraHelper.getCameraInstance();
      mMediaRecorder = new MediaRecorder();

      // Step 1: Unlock and set camera to MediaRecorder
      mCamera.unlock();
      mMediaRecorder.setCamera(mCamera);
    }

    // Step 2: Set sources
    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

    // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
    mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

    // Step 4: Set output file
    final String fileName = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
    mMediaRecorder.setOutputFile(fileName);

    // Step 5: Set the preview output
    mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
    mMediaRecorder.setOnInfoListener(new OnInfoListener() {
      @Override public void onInfo(MediaRecorder mr, int what, int extra) {
        // Handle the on duration exceeded
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
          stopStreaming(false);
          startStreaming(true);

          if (websocketConnected) {
            File file = new File(fileName);

            int size = (int) file.length();
            byte bytes[] = new byte[size];
            byte tmpBuff[] = new byte[size];
            try {
              FileInputStream fis = new FileInputStream(file);

              int read = fis.read(bytes, 0, size);
              if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                  read = fis.read(tmpBuff, 0, remain);
                  System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                  remain -= read;
                }
              }
            } catch (IOException e) {
              Log.e(TAG, "IoExc", e);
            }
            mWebSocketInstance.send(bytes);
            file.delete();
          }
        }
      }
    });

    mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
      @Override public void onError(MediaRecorder mediaRecorder, int what, int extra) {
        Log.d(TAG, "Error in mediarecorder, " + what + ", " + extra);
      }
    });

    mMediaRecorder.setMaxDuration(5000);
  }

  private boolean prepareVideoRecorder() {
    // Step 6: Prepare configured MediaRecorder
    try {
      mMediaRecorder.prepare();
    } catch (IllegalStateException e) {
      Log.e(TAG, "IllegalStateException preparing MediaRecorder", e);
      releaseMediaRecorder();
      return false;
    } catch (IOException e) {
      Log.e(TAG, "IOException preparing MediaRecorder", e);
      releaseMediaRecorder();
      return false;
    }
    return true;
  }

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

  private void initWebsocket() {
    // Send the file
    String url = "ws://145.49.35.215:1234/send";
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

  private void startStreaming(boolean firstStart) {
    initializeVideoRecorder(firstStart);

    if (!prepareVideoRecorder()) {
      releaseMediaRecorder();
      return;
    }

    try {
      mMediaRecorder.start();
    } catch (Exception ex) {
      Log.e(TAG, "Error during start", ex);
    }
    // inform the user that recording has started
    isRecording = true;
  }

  private void stopStreaming(boolean fullStop) {
    // stop recording and release camera
    mMediaRecorder.stop();  // stop the recording
    mMediaRecorder.reset();
    if (fullStop) {
      releaseMediaRecorder(); // release the MediaRecorder object
    }
    mCamera.lock();         // take camera access back from MediaRecorder
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
