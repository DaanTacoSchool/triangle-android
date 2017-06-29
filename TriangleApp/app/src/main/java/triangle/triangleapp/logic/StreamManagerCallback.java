package triangle.triangleapp.logic;

import android.support.annotation.NonNull;

import triangle.triangleapp.domain.ChatAction;

/**
 * Created by marco on 20-6-2017.
 */

public interface StreamManagerCallback {
    /**
     * Called when a ChatAction is received
     *
     * @param message The message that is received
     */
    void chatMessageReceived(@NonNull ChatAction message);

    void streamConnected();

    /**
     * Called when an error occurs during streaming
     * @param exception The exception that occurred.
     * @param fatal Whether the exception is fatal and should abort the stream entirely.
     */
    void streamError(@NonNull Exception exception, boolean fatal);

    void chatConnected();

    void chatError(@NonNull Exception exception);

    /**
     * Get the amount of viewers
     * @param viewersAmount Count of viewers
     */
    void setViewersCount(int viewersAmount);
}
