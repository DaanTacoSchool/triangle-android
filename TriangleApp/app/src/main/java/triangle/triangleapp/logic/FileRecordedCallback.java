package triangle.triangleapp.logic;

import android.support.annotation.NonNull;

/**
 * Created by Kevin Ly on 6/15/2017.
 */

public interface FileRecordedCallback {
    /**
     * Occurs when an recording has been completed
     *
     * @param fileInBytes File in byte array
     */
    void recordCompleted(@NonNull byte[] fileInBytes);
}
