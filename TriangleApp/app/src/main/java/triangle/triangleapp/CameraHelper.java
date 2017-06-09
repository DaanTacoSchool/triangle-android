package triangle.triangleapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

/**
 * Created by marco on 8-6-2017.
 */

public class CameraHelper {
  public static Camera getCameraInstance() {
    Camera c = null;
    try {
      c = Camera.open(); // attempt to get a Camera instance
    } catch (Exception e) {
      // Camera is not available (in use or does not exist)
    }
    return c; // returns null if camera is unavailable
  }

  /** Check if this device has a camera */
  public static boolean checkCameraHardware(Context context) {
    // this device has a camera
    // no camera on this device
    return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
  }
}
