package triangle.triangleapp.helpers;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kevin Ly on 6/13/2017.
 */

public class FileHelper {
    private static final String TAG = "FileHelper";
    private static final String SAVE_LOCATION = ConfigHelper.getInstance().get(ConfigHelper.KEY_FILE_HELPER_SAVE_LOCATION);

    /**
     * Obtain media file from storage.
     *
     * @return The file of directory
     */
    public static File getOutputMediaFile() {
        File mediaFile = null;
        try {
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), SAVE_LOCATION);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } catch (Exception ex) {
            Log.e(TAG, "Error occured while get output media file.", ex);
        }
        return mediaFile;
    }

    /**
     * Convert filename to byte array.
     *
     * @param fileName path to filename.
     * @return fileName in String
     */
    public static byte[] getBytesFromFile(@NonNull String fileName) {
        byte[] bytes = null;
        try {
            File file = new File(fileName);
            int size = (int) file.length();
            bytes = new byte[size];

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
            file.delete();
        } catch (Exception ex) {
            Log.e(TAG, "Error occured while get bytes from file.", ex);
        }
        return bytes;
    }
}
