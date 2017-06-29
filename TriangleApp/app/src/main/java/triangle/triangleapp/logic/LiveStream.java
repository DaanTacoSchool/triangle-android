package triangle.triangleapp.logic;

import android.support.annotation.NonNull;
import android.view.Surface;

/**
 * Created by Kevin Ly on 6/15/2017.
 */

public interface LiveStream {
    /**
     * Called when the stream should start recording
     *
     * @param fileRecordedCallback Callback that is called when a new file is recorded
     */
    void start(@NonNull FileRecordedCallback fileRecordedCallback);

    /**
     * Called when the stream should stop
     */
    void stop();

    /**
     * Called when the preview view is available.
     *
     * @param surface The surface to use for preview
     */
    void setPreviewView(@NonNull Surface surface);
}
