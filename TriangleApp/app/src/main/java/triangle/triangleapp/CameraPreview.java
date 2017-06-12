package triangle.triangleapp;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

/**
 * Created by marco on 8-6-2017.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
  private String TAG = "CameraPreview";
  private SurfaceHolder mHolder;
  private Camera mCamera;

  public CameraPreview(Context context, Camera camera) {
    super(context);
    mCamera = camera;

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    mHolder = getHolder();
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    mHolder.addCallback(this);
    // deprecated setting, but required on Android versions prior to 3.0

  }

  public void surfaceCreated(SurfaceHolder holder) {
    // The Surface has been created, now tell the camera where to draw the preview.
    try {
      mCamera.setDisplayOrientation(90);
      mCamera.setPreviewDisplay(holder);
      mCamera.startPreview();
    } catch (IOException e) {
      Log.e(TAG, "Error setting camera preview", e);
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    // empty. Take care of releasing the Camera preview in your activity.
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    // If your preview can change or rotate, take care of those events here.
    // Make sure to stop the preview before resizing or reformatting it.
    refreshCamera(mCamera);
  }

  public void setCamera(Camera camera) {
    //method to set a camera instance
    mCamera = camera;
  }

  public void refreshCamera(Camera camera) {
    if (mHolder.getSurface() == null) {
      // preview surface does not exist
      return;
    }
    // stop preview before making changes
    try {
      mCamera.stopPreview();
    } catch (Exception e) {
      // ignore: tried to stop a non-existent preview
    }
    // set preview size and make any resize, rotate or
    // reformatting changes here
    // start preview with new settings
    setCamera(camera);
    try {
      mCamera.setDisplayOrientation(90);
      mCamera.setPreviewDisplay(mHolder);
      mCamera.startPreview();
    } catch (Exception e) {
      Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
    }
  }
}