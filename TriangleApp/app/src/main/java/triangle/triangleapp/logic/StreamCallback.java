package triangle.triangleapp.logic;

/**
 * Created by Hugo on 21-6-2017.
 */

public interface StreamCallback {

    /**
     * Called when the connection could not be opened.
     * @param e The exception to pass.
     * @param fatal Whether the stream should be aborted.
     */
    void onConnectError(Exception e, boolean fatal);

    /**
     * Called when the stream could not be sent.
     * @param e The exception to pass.
     * @param fatal Whether the stream should be aborted.
     */
    void onSendError(Exception e, boolean fatal);
}
