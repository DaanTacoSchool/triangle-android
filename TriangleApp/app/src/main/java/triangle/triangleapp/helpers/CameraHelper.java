package triangle.triangleapp.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;

/**
 * Created by Kevin Ly on 6/12/2017.
 */

public class CameraHelper {
    private static final String TAG = "CameraHelper";

    /**
     * Open the camera and get the camera instance.
     *
     * @return Opened camera instance
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception ex) {
            Log.e(TAG, "Error occured while open camera.", ex);
        }
        return c;
    }
}
