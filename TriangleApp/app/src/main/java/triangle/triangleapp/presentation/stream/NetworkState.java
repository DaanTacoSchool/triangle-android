package triangle.triangleapp.presentation.stream;

/**
 * Created by Koen on 21-06-17.
 */

public interface NetworkState {

    /**
     * Called when the connection is established
     */
    void connected();

    /**
     * Called when the connection is lost
     */
    void disconnected();
}
