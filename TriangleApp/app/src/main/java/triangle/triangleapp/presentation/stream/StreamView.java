package triangle.triangleapp.presentation.stream;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.SurfaceView;

import triangle.triangleapp.domain.ChatAction;
import triangle.triangleapp.helpers.AdapterType;

/**
 * Created by Kevin Ly on 6/15/2017.
 */

public interface StreamView {
    /**
     * Called when the stream is started
     */
    void streamStarted();

    /**
     * Called when the stream is stopped
     */
    void streamStopped();

    /**
     * Called when the preview surface should be shown
     */
    void showPreview();

    /**
     * Called when the preview surface should be hide
     */
    void hidePreview();

    /**
     * Gets the surface to be used for preview
     *
     * @return Surface used for preview.
     */
    Surface getPreviewSurface();

    /**
     * Called when the connection is changed.
     *
     * @param state The newState of the connection
     */
    void networkConnectivityChanged(@ConnectionState int state, @Nullable String reason);

    /**
     * Called when an adapter has connected
     *
     * @param type The type of adapter that has connected
     */
    void connected(@AdapterType int type);

    /**
     * Called when an adapter occurred an error during connecting
     *
     * @param type      The type of adapter
     * @param exception The exception that occurred
     * @param fatal Whether the stream should be aborted.
     */
    void errorOccurred(@AdapterType int type, @NonNull Exception exception, boolean fatal);

    /**
     * Called when a message should be shown.
     *
     * @param message The message to show.
     */
    void showMessage(@NonNull ChatAction message);

    void showViewersCount(@NonNull int viewersCount);
}
