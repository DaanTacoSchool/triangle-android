package triangle.triangleapp.persistence.stream;

import android.support.annotation.NonNull;

import java.security.PrivateKey;
import java.security.PublicKey;

import triangle.triangleapp.persistence.ConnectionCallback;
import triangle.triangleapp.persistence.ViewersCallback;

/**
 * Created by Kevin Ly on 6/15/2017.
 */

public interface StreamAdapter {
    /**
     * Sends the public key to the stream
     *
     * @param publicKey The public key to send
     */
    void sendPublicKey(@NonNull PublicKey publicKey);

    /**
     * Starts connecting to the stream adapter
     *
     * @param callback The callback on which returns are called.
     */
    void connect(@NonNull ConnectionCallback callback);

    /**
     * Called when a new file should be sent
     *
     * @param fileInBytes File in bytes
     * @param privateKey The private key to use for signing
     */
    void sendFile(@NonNull byte[] fileInBytes, @NonNull PrivateKey privateKey);

    /**
     * Sends text to the stream.
     *
     * @param text The text to send.
     */
    void sendText(@NonNull String text);

    /**
     * Get the obtained id
     * @return Obtained id from the server
     */
    String getId();

    /**
     *  Get viewersAmount via callback.
     */
    void getViewers(ViewersCallback viewersCallback);
}