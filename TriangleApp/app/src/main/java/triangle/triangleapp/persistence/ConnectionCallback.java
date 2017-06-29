package triangle.triangleapp.persistence;

import android.support.annotation.NonNull;

/**
 * Created by danie on 15-6-2017.
 */

public interface ConnectionCallback {
    /**
     * Called when the adapter is connected
     */
    void onConnected();

    /**
     * Called when an error occurs
     *
     * @param ex The error that occurred
     */
    void onError(@NonNull Exception ex);
}
