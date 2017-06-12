package triangle.triangleapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.koushikdutta.async.http.WebSocket;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by D2110175 on 12-6-2017.
 */

public class CameraHelper {
    private static String TAG = "MainActivity";
    private static Camera mCamera;
    private static CameraPreview mPreview;
    private static MediaRecorder mMediaRecorder;
    private static boolean recording = false;
    private static int camId;
    private static boolean cameraFront = false;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static void setCameraPreview(CameraPreview cp){
        mPreview = cp;
    }
    private static int findFrontFacingCamera() {
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
    private static int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        camId=cameraId;
        return cameraId;
    }
    public static void resume(Context c){
        if (!hasCamera(c)) {
            Toast toast = Toast.makeText(c, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
           // c.finish();
        }
        if (mCamera == null) {
            // if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(c, "No front facing camera found.", Toast.LENGTH_LONG).show();

            }
            mCamera = Camera.open(findBackFacingCamera());
            mPreview.refreshCamera(mCamera);
        }
    }
    public static void chooseCamera() {
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
    public static void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }
    public static void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    private static boolean hasCamera(Context context) {
        // check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean prepareMediaRecorder() {

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
    public static void startStopRecording(Context c){
        if (recording) {
            // stop recording and release camera
            mMediaRecorder.stop(); // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            Toast.makeText(c, "Video captured!", Toast.LENGTH_LONG).show();
            recording = false;
        } else {
            if (!prepareMediaRecorder()) {
                Toast.makeText(c, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
              //  finish();
            }
            // work on UiThread for better performance
          /*  runOnUiThread(new Runnable() {
                public void run() {
                    // If there are stories, add them to the table

                    try {
                        mMediaRecorder.start();
                    } catch (final Exception ex) {
                        // Log.i("---","Exception in thread");
                    }
                }
            });*/
            mMediaRecorder.start();
            recording = true;
        }
    }

    //TODO: this COULD be placed in own helper.. if you are nitpicky that is.
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }
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
