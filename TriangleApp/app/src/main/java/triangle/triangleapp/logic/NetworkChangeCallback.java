package triangle.triangleapp.logic;

import android.support.annotation.Nullable;

/**
 * Created by Koen on 21-06-17.
 */

public interface NetworkChangeCallback {

    void onConnected();

    void onDisconnected(@Nullable String reason);
}
