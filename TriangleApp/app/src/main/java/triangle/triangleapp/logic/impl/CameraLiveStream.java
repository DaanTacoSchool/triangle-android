package triangle.triangleapp.logic.impl;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import triangle.triangleapp.helpers.ConfigHelper;
import triangle.triangleapp.helpers.CameraHelper;
import triangle.triangleapp.helpers.FileHelper;
import triangle.triangleapp.logic.FileRecordedCallback;
import triangle.triangleapp.logic.LiveStream;

/**
 * Created by Kevin Ly on 6/15/2017.
 */

public class CameraLiveStream implements LiveStream {
    private static final String TAG = "CameraStream";
    /*
    private static final int MAX_RECORD_DURATION = 5000;
    private static final int SET_ORIENTATION = 90;
    */
    private static final int MAX_RECORD_DURATION = ConfigHelper.getInstance().getInt(ConfigHelper.KEY_MAX_RECORD_DURATION);
    private static final int SET_ORIENTATION = ConfigHelper.getInstance().getInt(ConfigHelper.KEY_DISPLAY_ORIENTATION);
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private FileRecordedCallback mCallback;
    private Surface mPreviewSurface;
    private RandomAccessFile raf;
    private File tmpFile;

    @Override
    public void start(@NonNull FileRecordedCallback fileRecordedCallback) {
        mCallback = fileRecordedCallback;
        startStreaming(true);
    }

    @Override
    public void stop() {
        stopStreaming(true);
    }

    @Override
    public void setPreviewView(@NonNull Surface surface) {
        mPreviewSurface = surface;
    }

    /**
     * Initializing mediarecorder, set settings
     *
     * @param firstInit Check initializing on first time.
     */
    private void initializeVideoRecorder(boolean firstInit) {
        if (firstInit) {
            mCamera = CameraHelper.getCameraInstance();
            mMediaRecorder = new MediaRecorder();
            mCamera.setDisplayOrientation(SET_ORIENTATION);

            // Step 1: Unlock and set camera to MediaRecorder
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            try {
                this.tmpFile = File.createTempFile("tmpvid_livestream_345853", "tmp");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));

        // Step 4: Set output file
        final String fileName = FileHelper.getOutputMediaFile().toString();
        try {
            raf = new RandomAccessFile(tmpFile, "rw");
            mMediaRecorder.setOutputFile(raf.getFD());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mMediaRecorder.setOutputFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            mMediaRecorder.setOutputFile(fileName);
        }


        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreviewSurface);
        mMediaRecorder.setOrientationHint(SET_ORIENTATION);

        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                // Handle the on duration exceeded
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopStreaming(false);
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                byte[] streamBytes = new byte[(int) raf.length()];
                                raf.seek(0);
                                raf.readFully(streamBytes);
                                if (streamBytes != null) {
                                    mCallback.recordCompleted(streamBytes);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    startStreaming(true);
                }
            }
        });

        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mediaRecorder, int what, int extra) {
                Log.d(TAG, "Error in mediarecorder, " + what + ", " + extra);

            }
        });

        mMediaRecorder.setMaxDuration(MAX_RECORD_DURATION);
    }

    /**
     * Start streaming from camera to server.
     *
     * @param firstStart Check if videorecorder is started for the first time.
     */
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
    }

    /**
     * Stop streaming from camera to server.
     *
     * @param fullStop To release mediarecorder when it is stopped.
     */
    private void stopStreaming(boolean fullStop) {
        // stop recording
        try {
            mMediaRecorder.stop();
        } catch(RuntimeException e){

            //Clean up output file
            try {
                raf.close();
            } catch (IOException rafEx) {
                Log.e(TAG, "Error while closing output file", rafEx);
            }

        }
        mMediaRecorder.reset();

        // release the MediaRecorder object
        if (fullStop) {
            releaseMediaRecorder();
            mCamera.stopPreview();
        }

        // take camera access back from MediaRecorder
        mCamera.lock();
    }

    /**
     * Prepare media recorder to record.
     *
     * @return return state of preparation.
     */
    private boolean prepareVideoRecorder() {
        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            Log.e(TAG, "Error preparing MediaRecorder", e);
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    /**
     * Release and reset the media recorder. Locking the camera.
     */
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();

            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}
